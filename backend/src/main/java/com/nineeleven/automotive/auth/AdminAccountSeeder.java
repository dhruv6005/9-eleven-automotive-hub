package com.nineeleven.automotive.auth;

import com.nineeleven.automotive.user.Role;
import com.nineeleven.automotive.user.UserAccountStatus;
import com.nineeleven.automotive.user.UserEntity;
import com.nineeleven.automotive.user.UserRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminAccountSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminAccountSeeder.class);
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final PasswordPolicy policy;
    private final String email;
    private final String initialPassword;
    public AdminAccountSeeder(UserRepository users, PasswordEncoder encoder, PasswordPolicy policy,
            @Value("${app.admin.email:}") String email, @Value("${app.admin.initial-password:}") String initialPassword) {
        this.users = users; this.encoder = encoder; this.policy = policy;
        this.email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        this.initialPassword = initialPassword == null ? "" : initialPassword;
    }
    @Override @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() && initialPassword.isBlank()) {
            log.info("No first-admin credentials configured; skipping admin account seeding."); return;
        }
        if (email.isBlank() || initialPassword.isBlank()) throw new IllegalStateException(
                "Configure both ADMIN_EMAIL and ADMIN_INITIAL_PASSWORD to seed the first admin.");
        policy.requireValid(initialPassword);
        var existing = users.findByEmailIgnoreCaseAndDeletedAtIsNull(email);
        if (existing.isPresent()) {
            if (existing.get().getRole() != Role.ADMIN) throw new IllegalStateException(
                    "ADMIN_EMAIL already belongs to a non-admin account; refusing to change its role.");
            return;
        }
        UserEntity admin = new UserEntity(); admin.setEmail(email); admin.setPasswordHash(encoder.encode(initialPassword));
        admin.setRole(Role.ADMIN); admin.setStatus(UserAccountStatus.ACTIVE); admin.setEmailVerified(true);
        admin.setForcePasswordChange(true); admin.setFailedLoginAttempts(0); users.save(admin);
        log.info("Seeded the configured first admin account; a password change is required at first login.");
    }
}
