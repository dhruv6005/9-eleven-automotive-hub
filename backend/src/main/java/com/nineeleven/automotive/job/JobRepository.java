package com.nineeleven.automotive.job;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<JobEntity, UUID> {
    boolean existsByVehicleIdAndDeletedAtIsNull(UUID vehicleId);
}
