package com.nineeleven.automotive.vehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<VehicleEntity, UUID> {
    @Query("select v from VehicleEntity v join fetch v.customer c join fetch c.user where v.id=:id and v.deletedAt is null")
    Optional<VehicleEntity> activeById(@Param("id") UUID id);
    @Query("select v from VehicleEntity v join fetch v.customer c join fetch c.user where c.userId=:customerId and v.deletedAt is null")
    List<VehicleEntity> activeForCustomer(@Param("customerId") UUID customerId);
    @Query("select v from VehicleEntity v join fetch v.customer c join fetch c.user where c.userId=:customerId and v.id=:id and v.deletedAt is null")
    Optional<VehicleEntity> ownedActive(@Param("customerId") UUID customerId, @Param("id") UUID id);
    @Query("select count(v)>0 from VehicleEntity v where lower(v.registrationNumber)=lower(:registration) and v.deletedAt is null and (:id is null or v.id<>:id)")
    boolean registrationExists(@Param("registration") String registration, @Param("id") UUID id);
    @Query(value="select v from VehicleEntity v join fetch v.customer c join fetch c.user where v.deletedAt is null and lower(v.registrationNumber) like lower(concat('%',:search,'%'))",
           countQuery="select count(v) from VehicleEntity v where v.deletedAt is null and lower(v.registrationNumber) like lower(concat('%',:search,'%'))")
    Page<VehicleEntity> search(@Param("search") String search, Pageable pageable);
}
