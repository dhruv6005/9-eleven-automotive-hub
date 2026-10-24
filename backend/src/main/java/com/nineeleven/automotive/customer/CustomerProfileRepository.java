package com.nineeleven.automotive.customer;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfileEntity, UUID> {
    @org.springframework.data.jpa.repository.Query(value="select c from CustomerProfileEntity c join fetch c.user u where u.role=com.nineeleven.automotive.user.Role.CUSTOMER and u.deletedAt is null and (:status is null or u.status=:status) and (:search is null or lower(c.name) like lower(concat('%',:search,'%')) or u.phone like concat('%',:search,'%') or lower(coalesce(u.email,'')) like lower(concat('%',:search,'%')) or exists (select v.id from com.nineeleven.automotive.vehicle.VehicleEntity v where v.customer=c and v.deletedAt is null and lower(v.registrationNumber) like lower(concat('%',:search,'%'))))",
            countQuery="select count(c) from CustomerProfileEntity c join c.user u where u.role=com.nineeleven.automotive.user.Role.CUSTOMER and u.deletedAt is null and (:status is null or u.status=:status) and (:search is null or lower(c.name) like lower(concat('%',:search,'%')) or u.phone like concat('%',:search,'%') or lower(coalesce(u.email,'')) like lower(concat('%',:search,'%')) or exists (select v.id from com.nineeleven.automotive.vehicle.VehicleEntity v where v.customer=c and v.deletedAt is null and lower(v.registrationNumber) like lower(concat('%',:search,'%'))))")
    org.springframework.data.domain.Page<CustomerProfileEntity> search(@org.springframework.data.repository.query.Param("search") String search,
            @org.springframework.data.repository.query.Param("status") com.nineeleven.automotive.user.UserAccountStatus status,
            org.springframework.data.domain.Pageable pageable);
    @org.springframework.data.jpa.repository.Query("select c from CustomerProfileEntity c join fetch c.user u where c.userId=:id and u.role=com.nineeleven.automotive.user.Role.CUSTOMER and u.deletedAt is null")
    java.util.Optional<CustomerProfileEntity> findCustomer(@org.springframework.data.repository.query.Param("id") UUID id);
}
