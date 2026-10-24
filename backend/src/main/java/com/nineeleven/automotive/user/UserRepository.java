package com.nineeleven.automotive.user;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where lower(u.email) = lower(:email) and u.deletedAt is null")
    Optional<UserEntity> findByEmailForUpdate(@Param("email") String email);
    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);
}
