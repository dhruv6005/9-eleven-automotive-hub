package com.nineeleven.automotive.catalog;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public final class CatalogModels {
    private CatalogModels() {}
    public record CategoryRequest(@NotBlank @Size(max=120) String name,@NotBlank @Size(max=140) @Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*") String slug,@Size(max=2000) String description,@Min(0) int sortOrder,String imageUrl,@NotNull Boolean active) {}
    public record CategoryResponse(UUID id,String name,String slug,String description,int sortOrder,String imageUrl,boolean active) {}
    public record ServiceRequest(@NotNull UUID categoryId,@NotBlank @Size(max=160) String name,@NotBlank @Size(max=160) @Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*") String slug,@Size(max=2000) String description,@Size(max=300) String shortBenefit,String imageUrl,@DecimalMin("0") BigDecimal baseDurationHours,@NotNull Boolean active) {}
    public record ServiceResponse(UUID id,UUID categoryId,String categoryName,String categorySlug,String name,String slug,String description,String shortBenefit,String imageUrl,BigDecimal baseDurationHours,BigDecimal price,boolean active) {}
    public record PriceRequest(@NotBlank @Size(max=32) String vehicleSizeClass,@NotNull @DecimalMin(value="0.00") @Digits(integer=10,fraction=2) BigDecimal price) {}
    public record PriceResponse(UUID id,UUID serviceId,String vehicleSizeClass,BigDecimal price) {}
}
