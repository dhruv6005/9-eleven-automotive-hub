package com.nineeleven.automotive.auth;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OneTimeTokenRepository extends JpaRepository<OneTimeTokenEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from OneTimeTokenEntity t join fetch t.user where t.tokenHash = :hash")
    Optional<OneTimeTokenEntity> findByTokenHashWithUser(@Param("hash") String hash);
}
