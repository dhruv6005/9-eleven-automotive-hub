package com.nineeleven.automotive.catalog;

import com.nineeleven.automotive.common.persistence.AuditedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name="service_prices", uniqueConstraints=@UniqueConstraint(name="ux_service_prices_service_size", columnNames={"service_id","vehicle_size_class"}))
public class ServicePriceEntity extends AuditedEntity {
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="service_id", nullable=false) private ServiceEntity service;
    @Column(name="vehicle_size_class", nullable=false, length=32) private String vehicleSizeClass;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal price;
    public ServiceEntity getService(){return service;} public void setService(ServiceEntity v){service=v;}
    public String getVehicleSizeClass(){return vehicleSizeClass;} public void setVehicleSizeClass(String v){vehicleSizeClass=v;}
    public BigDecimal getPrice(){return price;} public void setPrice(BigDecimal v){price=v;}
}
