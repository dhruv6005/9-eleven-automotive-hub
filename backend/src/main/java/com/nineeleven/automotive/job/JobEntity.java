package com.nineeleven.automotive.job;

import com.nineeleven.automotive.common.persistence.AuditedEntity;
import com.nineeleven.automotive.customer.CustomerProfileEntity;
import com.nineeleven.automotive.user.UserEntity;
import com.nineeleven.automotive.vehicle.VehicleEntity;
import java.math.BigDecimal;
import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "jobs")
public class JobEntity extends AuditedEntity {
    @Column(name = "job_number", nullable = false, unique = true, length = 32)
    private String jobNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfileEntity customer;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleEntity vehicle;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private JobStatus status;
    @Column(name = "intake_notes")
    private String intakeNotes;
    @Column(name = "odometer_in")
    private Integer odometerIn;
    @Column(name = "promised_date")
    private Instant promisedDate;
    @Column(name = "actual_delivery_date")
    private Instant actualDeliveryDate;
    @Column(name = "total_estimate", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEstimate = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;
    @Column(name = "total_final", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFinal = BigDecimal.ZERO;
    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private UserEntity assignedTo;

    public String getJobNumber() { return jobNumber; }
    public void setJobNumber(String jobNumber) { this.jobNumber = jobNumber; }
    public CustomerProfileEntity getCustomer() { return customer; }
    public void setCustomer(CustomerProfileEntity customer) { this.customer = customer; }
    public VehicleEntity getVehicle() { return vehicle; }
    public void setVehicle(VehicleEntity vehicle) { this.vehicle = vehicle; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public String getIntakeNotes() { return intakeNotes; }
    public void setIntakeNotes(String intakeNotes) { this.intakeNotes = intakeNotes; }
    public Integer getOdometerIn() { return odometerIn; }
    public void setOdometerIn(Integer odometerIn) { this.odometerIn = odometerIn; }
    public Instant getPromisedDate() { return promisedDate; }
    public void setPromisedDate(Instant promisedDate) { this.promisedDate = promisedDate; }
    public Instant getActualDeliveryDate() { return actualDeliveryDate; }
    public void setActualDeliveryDate(Instant actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; }
    public BigDecimal getTotalEstimate() { return totalEstimate; }
    public void setTotalEstimate(BigDecimal totalEstimate) { this.totalEstimate = totalEstimate; }
    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public BigDecimal getTotalFinal() { return totalFinal; }
    public void setTotalFinal(BigDecimal totalFinal) { this.totalFinal = totalFinal; }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    public UserEntity getAssignedTo() { return assignedTo; }
    public void setAssignedTo(UserEntity assignedTo) { this.assignedTo = assignedTo; }
}
