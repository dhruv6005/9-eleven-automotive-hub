package com.nineeleven.automotive.auth;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from PasswordResetTokenEntity t join fetch t.user where t.tokenHash = :hash")
    Optional<PasswordResetTokenEntity> findByTokenHashWithUser(@Param("hash") String hash);
    @Modifying
    @Query("update PasswordResetTokenEntity t set t.consumedAt = CURRENT_TIMESTAMP where t.user.id = :userId and t.consumedAt is null")
    int consumeAllForUser(@Param("userId") java.util.UUID userId);
}
