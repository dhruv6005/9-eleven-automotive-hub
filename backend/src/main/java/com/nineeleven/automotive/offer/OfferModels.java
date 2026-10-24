package com.nineeleven.automotive.offer;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public final class OfferModels {
    private OfferModels() {}
    public record OfferRequest(@NotBlank @Size(max=180) String title,@Size(max=2000) String description,@NotNull DiscountType discountType,@NotNull @DecimalMin("0") BigDecimal discountValue,@NotNull LocalDate validFrom,@NotNull LocalDate validTo,@NotNull Boolean appliesToAll,@NotNull Boolean active,@NotNull Set<UUID> serviceIds) {}
    public record OfferResponse(UUID id,String title,String description,DiscountType discountType,BigDecimal discountValue,LocalDate validFrom,LocalDate validTo,boolean appliesToAll,boolean active,Set<UUID> serviceIds) {}
}
