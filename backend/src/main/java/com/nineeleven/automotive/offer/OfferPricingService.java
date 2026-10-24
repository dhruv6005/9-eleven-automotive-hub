package com.nineeleven.automotive.offer;

import com.nineeleven.automotive.catalog.ServicePriceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OfferPricingService {
    private final ServicePriceRepository prices;
    public OfferPricingService(ServicePriceRepository prices){this.prices=prices;}
    public BigDecimal discountedPrice(UUID serviceId,String sizeClass,OfferEntity offer,LocalDate date){
        BigDecimal price=prices.findByServiceIdAndVehicleSizeClass(serviceId,sizeClass.toUpperCase(java.util.Locale.ROOT)).map(p->p.getPrice()).orElse(null);
        return OfferPricing.discountedPrice(price,offer,date);
    }
}
