package com.nineeleven.automotive.vehicle;

import com.nineeleven.automotive.common.persistence.AuditedEntity;
import com.nineeleven.automotive.customer.CustomerProfileEntity;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicles")
public class VehicleEntity extends AuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfileEntity customer;
    @Column(nullable = false, length = 100)
    private String make;
    @Column(nullable = false, length = 100)
    private String model;
    @Column(length = 100)
    private String variant;
    private Integer year;
    @Column(name = "reg_number", nullable = false, length = 24)
    private String registrationNumber;
    @Column(length = 50)
    private String color;
    @Column(name = "size_class", length = 32)
    private String sizeClass;
    @Column(length = 64)
    private String vin;

    public UUID getId() { return super.getId(); }
    public CustomerProfileEntity getCustomer() { return customer; }
    public void setCustomer(CustomerProfileEntity customer) { this.customer = customer; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getSizeClass() { return sizeClass; }
    public void setSizeClass(String sizeClass) { this.sizeClass = sizeClass; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
}
