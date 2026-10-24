package com.nineeleven.automotive.offer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class OfferPricing {
    private OfferPricing() {}
    public static BigDecimal discountedPrice(BigDecimal price, OfferEntity offer, LocalDate date) {
        if(price==null) return null;
        if(price.signum()<0) throw new IllegalArgumentException("Price cannot be negative.");
        if(offer.getDiscountType()==DiscountType.FLAT && offer.getDiscountValue().compareTo(price)>0) throw new IllegalArgumentException("Flat discount cannot exceed the service price.");
        if(!offer.isActive() || date.isBefore(offer.getValidFrom()) || date.isAfter(offer.getValidTo())) return price.setScale(2,RoundingMode.HALF_UP);
        BigDecimal discount=offer.getDiscountType()==DiscountType.PERCENT?price.multiply(offer.getDiscountValue()).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP):offer.getDiscountValue();
        return price.subtract(discount).max(BigDecimal.ZERO).setScale(2,RoundingMode.HALF_UP);
    }
}
