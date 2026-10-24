package com.nineeleven.automotive.auth;

import com.nineeleven.automotive.customer.CustomerProfileEntity;
import com.nineeleven.automotive.customer.CustomerProfileRepository;
import com.nineeleven.automotive.user.Role;
import com.nineeleven.automotive.user.UserAccountStatus;
import com.nineeleven.automotive.user.UserEntity;
import com.nineeleven.automotive.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final CustomerProfileRepository profiles;
    private final RefreshTokenRepository refreshTokens;
    private final OneTimeTokenRepository verificationTokens;
    private final PasswordResetTokenRepository resetTokens;
    private final PasswordEncoder passwords;
    private final PasswordPolicy policy;
    private final AuthTokenService jwt;
    private final AuthMailService mail;
    private final JdbcTemplate jdbc;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, CustomerProfileRepository profiles, RefreshTokenRepository refreshTokens,
            OneTimeTokenRepository verificationTokens, PasswordResetTokenRepository resetTokens,
            PasswordEncoder passwords, PasswordPolicy policy, AuthTokenService jwt, AuthMailService mail, JdbcTemplate jdbc) {
        this.users = users; this.profiles = profiles; this.refreshTokens = refreshTokens;
        this.verificationTokens = verificationTokens; this.resetTokens = resetTokens;
        this.passwords = passwords; this.policy = policy; this.jwt = jwt; this.mail = mail; this.jdbc = jdbc;
    }

    @Transactional
    public void register(AuthModels.RegisterRequest request) {
        policy.requireValid(request.password());
        String email = normalize(request.email());
        if (users.existsByEmailIgnoreCaseAndDeletedAtIsNull(email)) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT, "An account with this email already exists.");
        UserEntity user = new UserEntity();
        user.setEmail(email); user.setPhone(request.phone()); user.setPasswordHash(passwords.encode(request.password()));
        user.setRole(Role.CUSTOMER); user.setStatus(UserAccountStatus.PENDING_VERIFICATION); user.setEmailVerified(false);
        user.setForcePasswordChange(false); user.setFailedLoginAttempts(0);
        users.saveAndFlush(user);
        CustomerProfileEntity profile = new CustomerProfileEntity(); profile.setUser(user); profile.setName(request.name().trim());
        profiles.save(profile);
        OneTimeTokenEntity token = new OneTimeTokenEntity(); token.setId(UUID.randomUUID()); token.setUser(user);
        String raw = newOpaqueToken(); token.setTokenHash(hash(raw)); token.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        token.setCreatedAt(Instant.now()); verificationTokens.save(token);
        mail.sendVerification(email, raw);
    }

    @Transactional
    public AuthModels.RefreshResult login(AuthModels.LoginRequest request) {
        String email = normalize(request.email());
        Optional<UserEntity> candidate = users.findByEmailForUpdate(email);
        if (candidate.isEmpty()) {
            passwords.matches(request.password(), "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
            audit(null, "LOGIN_FAILED");
            return null;
        }
        UserEntity user = candidate.get();
        Instant now = Instant.now();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            audit(user.getId(), "LOGIN_FAILED"); return null;
        }
        if (user.getLockedUntil() != null && !user.getLockedUntil().isAfter(now)) {
            user.setLockedUntil(null); user.setFailedLoginAttempts(0);
        }
        if (!passwords.matches(request.password(), user.getPasswordHash())
                || user.getStatus() != UserAccountStatus.ACTIVE || !user.isEmailVerified()) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) user.setLockedUntil(now.plus(15, ChronoUnit.MINUTES));
            audit(user.getId(), "LOGIN_FAILED"); return null;
        }
        user.setFailedLoginAttempts(0); user.setLockedUntil(null);
        audit(user.getId(), "LOGIN_SUCCESS");
        return createSession(user, UUID.randomUUID());
    }

    @Transactional
    public AuthModels.RefreshResult refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return null;
        Optional<RefreshTokenEntity> found = refreshTokens.findByTokenHashWithUser(hash(rawToken));
        if (found.isEmpty()) return null;
        RefreshTokenEntity old = found.get();
        Instant now = Instant.now();
        if (old.getRevokedAt() != null) {
            refreshTokens.revokeFamily(old.getFamilyId());
            return null;
        }
        if (!old.getExpiresAt().isAfter(now) || old.getUser().getStatus() != UserAccountStatus.ACTIVE) return null;
        AuthModels.RefreshResult next = createSession(old.getUser(), old.getFamilyId());
        old.setRevokedAt(now); old.setReplacedByTokenId(next.tokenId());
        refreshTokens.save(old);
        return next;
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        refreshTokens.findByTokenHashWithUser(hash(rawToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) { token.setRevokedAt(Instant.now()); refreshTokens.save(token); }
        });
    }

    @Transactional
    public void verifyEmail(String raw) {
        OneTimeTokenEntity token = verificationTokens.findByTokenHashWithUser(hash(raw))
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid or expired verification link."));
        if (token.getConsumedAt() != null || !token.getExpiresAt().isAfter(Instant.now())) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid or expired verification link.");
        token.setConsumedAt(Instant.now()); token.getUser().setEmailVerified(true); token.getUser().setStatus(UserAccountStatus.ACTIVE);
    }

    @Transactional
    public void forgotPassword(String emailInput) {
        users.findByEmailIgnoreCaseAndDeletedAtIsNull(normalize(emailInput)).filter(u -> u.getStatus() == UserAccountStatus.ACTIVE).ifPresent(user -> {
            String raw = newOpaqueToken(); PasswordResetTokenEntity token = new PasswordResetTokenEntity();
            token.setId(UUID.randomUUID()); token.setUser(user); token.setTokenHash(hash(raw));
            token.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES)); token.setCreatedAt(Instant.now());
            resetTokens.save(token); mail.sendPasswordReset(user.getEmail(), raw);
        });
    }

    @Transactional
    public void resetPassword(AuthModels.ResetPasswordRequest request) {
        policy.requireValid(request.password());
        PasswordResetTokenEntity token = resetTokens.findByTokenHashWithUser(hash(request.token()))
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid or expired reset link."));
        Instant now = Instant.now();
        if (token.getConsumedAt() != null || !token.getExpiresAt().isAfter(now)) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid or expired reset link.");
        UserEntity user = token.getUser();
        token.setConsumedAt(now); user.setPasswordHash(passwords.encode(request.password()));
        user.setFailedLoginAttempts(0); user.setLockedUntil(null); user.setForcePasswordChange(false);
        resetTokens.consumeAllForUser(user.getId());
        refreshTokens.revokeAllForUser(user.getId()); audit(user.getId(), "PASSWORD_RESET");
    }

    @Transactional
    public String changePassword(UUID userId, AuthModels.ChangePasswordRequest request) {
        UserEntity user = users.findById(userId).orElseThrow();
        if (!passwords.matches(request.currentPassword(), user.getPasswordHash())) throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Current password is incorrect.");
        policy.requireValid(request.newPassword());
        if (passwords.matches(request.newPassword(), user.getPasswordHash())) throw new IllegalArgumentException("Choose a password different from the current password.");
        user.setPasswordHash(passwords.encode(request.newPassword())); user.setForcePasswordChange(false);
        return jwt.issueAccessToken(user);
    }

    private AuthModels.RefreshResult createSession(UserEntity user, UUID family) {
        String raw = newOpaqueToken(); RefreshTokenEntity token = new RefreshTokenEntity(); token.setId(UUID.randomUUID());
        token.setUser(user); token.setFamilyId(family); token.setTokenHash(hash(raw));
        token.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS)); token.setCreatedAt(Instant.now());
        refreshTokens.save(token);
        return new AuthModels.RefreshResult(jwt.issueAccessToken(user), raw, user.getRole().name(), user.isForcePasswordChange(), token.getId());
    }
    private String newOpaqueToken() { byte[] bytes = new byte[32]; random.nextBytes(bytes); return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
    private String hash(String value) {
        try { return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException("SHA-256 is not available", ex); }
    }
    private String normalize(String email) { return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT); }
    private void audit(UUID userId, String action) {
        jdbc.update("insert into audit_log (id,user_id,action,entity_type,entity_id,details,correlation_id) values (?, ?, ?, 'AUTH', ?, null, ?)",
                UUID.randomUUID(), userId, action, userId, MDC.get("correlationId"));
    }
}
