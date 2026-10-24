package com.nineeleven.automotive.offer;

import com.nineeleven.automotive.catalog.ServiceEntity;
import com.nineeleven.automotive.common.persistence.AuditedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity @Table(name="offers")
public class OfferEntity extends AuditedEntity {
    @Column(nullable=false,length=180) private String title;
    private String description;
    @Column(name="discount_type",nullable=false,length=16) @Enumerated(EnumType.STRING) private DiscountType discountType;
    @Column(name="discount_value",nullable=false,precision=12,scale=2) private BigDecimal discountValue;
    @Column(name="valid_from",nullable=false) private LocalDate validFrom;
    @Column(name="valid_to",nullable=false) private LocalDate validTo;
    @Column(name="applies_to_all",nullable=false) private boolean appliesToAll;
    @Column(nullable=false) private boolean active;
    @ManyToMany @JoinTable(name="offer_services",joinColumns=@JoinColumn(name="offer_id"),inverseJoinColumns=@JoinColumn(name="service_id")) private Set<ServiceEntity> services=new LinkedHashSet<>();
    public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public DiscountType getDiscountType(){return discountType;} public void setDiscountType(DiscountType v){discountType=v;} public BigDecimal getDiscountValue(){return discountValue;} public void setDiscountValue(BigDecimal v){discountValue=v;}
    public LocalDate getValidFrom(){return validFrom;} public void setValidFrom(LocalDate v){validFrom=v;} public LocalDate getValidTo(){return validTo;} public void setValidTo(LocalDate v){validTo=v;}
    public boolean isAppliesToAll(){return appliesToAll;} public void setAppliesToAll(boolean v){appliesToAll=v;} public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
    public Set<ServiceEntity> getServices(){return services;} public void setServices(Set<ServiceEntity> v){services=v;}
}
