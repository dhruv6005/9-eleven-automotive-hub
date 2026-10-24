package com.nineeleven.automotive.offer;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class OfferPricingTest {
    @Test void computesPercentDiscountWithPaiseRounding(){
        OfferEntity offer=new OfferEntity();offer.setActive(true);offer.setDiscountType(DiscountType.PERCENT);offer.setDiscountValue(new BigDecimal("12.5"));offer.setValidFrom(LocalDate.parse("2026-01-01"));offer.setValidTo(LocalDate.parse("2026-12-31"));
        assertEquals(new BigDecimal("875.00"),OfferPricing.discountedPrice(new BigDecimal("1000"),offer,LocalDate.parse("2026-10-24")));
    }
    @Test void refusesFlatDiscountAbovePrice(){
        OfferEntity offer=new OfferEntity();offer.setActive(true);offer.setDiscountType(DiscountType.FLAT);offer.setDiscountValue(new BigDecimal("501"));offer.setValidFrom(LocalDate.MIN);offer.setValidTo(LocalDate.MAX);
        assertThrows(IllegalArgumentException.class,()->OfferPricing.discountedPrice(new BigDecimal("500"),offer,LocalDate.now()));
    }
}
