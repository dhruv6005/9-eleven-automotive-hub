package com.nineeleven.automotive.vehicle;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class VehicleModels {
    private VehicleModels() { }
    public record Request(@NotBlank @Size(max=100) String make, @NotBlank @Size(max=100) String model,
            @Size(max=100) String variant, @Min(1886) @Max(2100) Integer year, @Size(max=50) String color,
            @NotNull VehicleSizeClass sizeClass, @NotBlank @Size(max=24) String regNumber, @Size(max=64) String vin) { }
    public record Response(java.util.UUID id, java.util.UUID customerId, String make, String model, String variant,
            Integer year, String color, VehicleSizeClass sizeClass, String regNumber, String vin) { }
}
