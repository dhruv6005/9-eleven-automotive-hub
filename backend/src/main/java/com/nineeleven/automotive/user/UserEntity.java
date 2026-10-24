package com.nineeleven.automotive.user;

import com.nineeleven.automotive.common.persistence.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class UserEntity extends AuditedEntity {
    @Column(length = 320)
    private String email;
    @Column(length = 30)
    private String phone;
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Role role;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private UserAccountStatus status;
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;
    @Column(name = "failed_login_attempts", nullable = false)
    private int failedLoginAttempts;
    @Column(name = "locked_until")
    private Instant lockedUntil;
    @Column(name = "force_password_change", nullable = false)
    private boolean forcePasswordChange;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public UserAccountStatus getStatus() { return status; }
    public void setStatus(UserAccountStatus status) { this.status = status; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int value) { this.failedLoginAttempts = value; }
    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant value) { this.lockedUntil = value; }
    public boolean isForcePasswordChange() { return forcePasswordChange; }
    public void setForcePasswordChange(boolean value) { this.forcePasswordChange = value; }
}
