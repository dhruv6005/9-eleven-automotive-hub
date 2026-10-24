package com.nineeleven.automotive.catalog;
import java.util.*;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ServicePriceRepository extends JpaRepository<ServicePriceEntity, UUID> {
    Optional<ServicePriceEntity> findByServiceIdAndVehicleSizeClass(UUID serviceId, String vehicleSizeClass);
    List<ServicePriceEntity> findAllByServiceId(UUID serviceId);
    void deleteByServiceIdAndVehicleSizeClass(UUID serviceId, String vehicleSizeClass);
}
