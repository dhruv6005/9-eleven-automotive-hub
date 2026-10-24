package com.nineeleven.automotive.auth;

import com.nineeleven.automotive.customer.CustomerProfileEntity;
import com.nineeleven.automotive.customer.CustomerProfileRepository;
import com.nineeleven.automotive.user.UserEntity;
import com.nineeleven.automotive.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private static final String REFRESH_COOKIE = "refresh_token";
    private static final String COOKIE_PATH = "/api/v1/auth";
    private final AuthService auth;
    private final UserRepository users;
    private final CustomerProfileRepository profiles;
    public AuthController(AuthService auth, UserRepository users, CustomerProfileRepository profiles) {
        this.auth = auth; this.users = users; this.profiles = profiles;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<AuthModels.MessageResponse> register(@Valid @RequestBody AuthModels.RegisterRequest request) {
        auth.register(request); return ResponseEntity.accepted().body(new AuthModels.MessageResponse("Check your email to verify your account."));
    }
    @PostMapping("/auth/login")
    public ResponseEntity<AuthModels.AuthResponse> login(@Valid @RequestBody AuthModels.LoginRequest request,
            HttpServletRequest servletRequest, HttpServletResponse response) {
        AuthModels.RefreshResult result = auth.login(request);
        if (result == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password, or account unavailable.");
        setRefreshCookie(response, result.refreshToken());
        return ResponseEntity.ok(new AuthModels.AuthResponse(result.accessToken(), "Bearer", AuthTokenService.ACCESS_TTL_SECONDS,
                result.role(), result.forcePasswordChange()));
    }
    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthModels.AuthResponse> refresh(@CookieValue(name = REFRESH_COOKIE, required = false) String token,
            HttpServletRequest request, HttpServletResponse response) {
        AuthModels.RefreshResult result = auth.refresh(token);
        if (result == null) { clearRefreshCookie(response); throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh session is invalid."); }
        setRefreshCookie(response, result.refreshToken());
        return ResponseEntity.ok(new AuthModels.AuthResponse(result.accessToken(), "Bearer", AuthTokenService.ACCESS_TTL_SECONDS,
                result.role(), result.forcePasswordChange()));
    }
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = REFRESH_COOKIE, required = false) String token, HttpServletResponse response) {
        auth.logout(token); clearRefreshCookie(response); return ResponseEntity.noContent().build();
    }
    @PostMapping("/auth/forgot-password")
    public AuthModels.MessageResponse forgot(@Valid @RequestBody EmailRequest request) {
        auth.forgotPassword(request.email()); return new AuthModels.MessageResponse("If the account exists, password reset instructions have been sent.");
    }
    @PostMapping("/auth/reset-password")
    public AuthModels.MessageResponse reset(@Valid @RequestBody AuthModels.ResetPasswordRequest request) {
        auth.resetPassword(request); return new AuthModels.MessageResponse("Password reset. You can now sign in.");
    }
    @GetMapping("/auth/verify-email")
    public AuthModels.MessageResponse verify(@RequestParam String token) {
        auth.verifyEmail(token); return new AuthModels.MessageResponse("Email verified. You can now sign in.");
    }
    @PostMapping("/auth/change-password")
    public AuthModels.AuthResponse changePassword(@AuthenticationPrincipal Jwt principal,
            @Valid @RequestBody AuthModels.ChangePasswordRequest request) {
        String token = auth.changePassword(UUID.fromString(principal.getSubject()), request);
        return new AuthModels.AuthResponse(token, "Bearer", AuthTokenService.ACCESS_TTL_SECONDS,
                principal.getClaimAsString("role"), false);
    }

    @GetMapping("/me") @PreAuthorize("hasRole('CUSTOMER')") @Transactional(readOnly = true)
    public AuthModels.ProfileResponse getMe(@AuthenticationPrincipal Jwt principal) {
        UUID id = UUID.fromString(principal.getSubject());
        UserEntity user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        CustomerProfileEntity profile = profiles.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new AuthModels.ProfileResponse(user.getEmail(), user.getPhone(), profile.getName(), profile.getAddress());
    }
    @PutMapping("/me") @PreAuthorize("hasRole('CUSTOMER')") @Transactional
    public AuthModels.ProfileResponse updateMe(@AuthenticationPrincipal Jwt principal,
            @Valid @RequestBody AuthModels.ProfileUpdateRequest request) {
        UUID id = UUID.fromString(principal.getSubject());
        UserEntity user = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        CustomerProfileEntity profile = profiles.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        user.setPhone(request.phone()); profile.setName(request.name().trim()); profile.setAddress(request.address());
        return new AuthModels.ProfileResponse(user.getEmail(), user.getPhone(), profile.getName(), profile.getAddress());
    }

    @GetMapping("/admin/session") @PreAuthorize("hasRole('ADMIN')")
    public AuthModels.SessionResponse getAdminSession(@AuthenticationPrincipal Jwt principal) {
        return new AuthModels.SessionResponse(principal.getClaimAsString("email"), principal.getClaimAsString("role"));
    }

    private void setRefreshCookie(HttpServletResponse response, String value) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, value).httpOnly(true).secure(true).sameSite("Strict")
                .path(COOKIE_PATH).maxAge(Duration.ofDays(30)).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "").httpOnly(true).secure(true).sameSite("Strict")
                .path(COOKIE_PATH).maxAge(Duration.ZERO).build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    public record EmailRequest(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email String email) { }
}
