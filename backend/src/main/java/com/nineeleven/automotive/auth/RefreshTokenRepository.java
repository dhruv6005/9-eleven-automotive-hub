package com.nineeleven.automotive.auth;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshTokenEntity t join fetch t.user where t.tokenHash = :hash")
    Optional<RefreshTokenEntity> findByTokenHashWithUser(@Param("hash") String hash);
    @Modifying
    @Query("update RefreshTokenEntity t set t.revokedAt = CURRENT_TIMESTAMP where t.familyId = :familyId and t.revokedAt is null")
    int revokeFamily(@Param("familyId") UUID familyId);
    @Modifying
    @Query("update RefreshTokenEntity t set t.revokedAt = CURRENT_TIMESTAMP where t.user.id = :userId and t.revokedAt is null")
    int revokeAllForUser(@Param("userId") UUID userId);
    List<RefreshTokenEntity> findAllByUser_Id(UUID userId);
}
