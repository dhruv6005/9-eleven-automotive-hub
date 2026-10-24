package com.nineeleven.automotive.catalog;

import com.nineeleven.automotive.common.persistence.AuditedEntity;
import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "services")
public class ServiceEntity extends AuditedEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ServiceCategoryEntity category;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(nullable = false, unique = true, length = 160)
    private String slug;
    private String description;
    @Column(name = "short_benefit", length = 300)
    private String shortBenefit;
    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;
    @Column(name = "base_duration_hours", precision = 7, scale = 2)
    private BigDecimal baseDurationHours;
    @Column(nullable = false)
    private boolean active = true;

    public ServiceCategoryEntity getCategory() { return category; }
    public void setCategory(ServiceCategoryEntity category) { this.category = category; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getShortBenefit() { return shortBenefit; }
    public void setShortBenefit(String shortBenefit) { this.shortBenefit = shortBenefit; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public BigDecimal getBaseDurationHours() { return baseDurationHours; }
    public void setBaseDurationHours(BigDecimal baseDurationHours) { this.baseDurationHours = baseDurationHours; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
