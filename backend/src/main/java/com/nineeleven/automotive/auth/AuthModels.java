package com.nineeleven.automotive.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthModels {
    private AuthModels() { }
    public record RegisterRequest(@NotBlank @Email @Size(max = 320) String email,
                                  @NotBlank @Size(min = 2, max = 160) String name,
                                  @Size(max = 30) String phone,
                                  @NotBlank @Size(min = 12, max = 72) String password) { }
    public record LoginRequest(@NotBlank @Email @Size(max = 320) String email,
                               @NotBlank String password) { }
    public record TokenRequest(@NotBlank String token) { }
    public record ResetPasswordRequest(@NotBlank String token,
                                       @NotBlank @Size(min = 12, max = 72) String password) { }
    public record ChangePasswordRequest(@NotBlank String currentPassword,
                                        @NotBlank @Size(min = 12, max = 72) String newPassword) { }
    public record RefreshResult(String accessToken, String refreshToken, String role, boolean forcePasswordChange, java.util.UUID tokenId) { }
    public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds,
                               String role, boolean forcePasswordChange) { }
    public record ProfileResponse(String email, String phone, String name, String address) { }
    public record SessionResponse(String email, String role) { }
    public record ProfileUpdateRequest(@NotBlank @Size(min = 2, max = 160) String name,
                                       @Size(max = 30) String phone,
                                       @Size(max = 1000) String address) { }
    public record MessageResponse(String message) { }
}
