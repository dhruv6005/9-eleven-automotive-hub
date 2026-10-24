package com.nineeleven.automotive.catalog;

import com.nineeleven.automotive.offer.*;
import org.mapstruct.*;

@Mapper(componentModel="spring")
public interface CatalogMapper {
    @Mapping(target="sortOrder",source="displayOrder") CatalogModels.CategoryResponse category(ServiceCategoryEntity source);
    @Mapping(target="categoryId",source="category.id") @Mapping(target="categoryName",source="category.name") @Mapping(target="categorySlug",source="category.slug") @Mapping(target="price",ignore=true) CatalogModels.ServiceResponse service(ServiceEntity source);
    @Mapping(target="serviceIds",expression="java(source.getServices().stream().filter(s -> s.isActive() && s.getDeletedAt() == null && s.getCategory().isActive() && s.getCategory().getDeletedAt() == null).map(com.nineeleven.automotive.catalog.ServiceEntity::getId).collect(java.util.stream.Collectors.toSet()))") OfferModels.OfferResponse offer(OfferEntity source);
    @BeanMapping(nullValuePropertyMappingStrategy=NullValuePropertyMappingStrategy.IGNORE) @Mapping(target="id",ignore=true) @Mapping(target="deletedAt",ignore=true) @Mapping(target="version",ignore=true) @Mapping(target="createdAt",ignore=true) @Mapping(target="updatedAt",ignore=true) @Mapping(target="createdBy",ignore=true) @Mapping(target="displayOrder",source="sortOrder") void update(CatalogModels.CategoryRequest input,@MappingTarget ServiceCategoryEntity target);
    @BeanMapping(nullValuePropertyMappingStrategy=NullValuePropertyMappingStrategy.IGNORE) @Mapping(target="id",ignore=true) @Mapping(target="deletedAt",ignore=true) @Mapping(target="version",ignore=true) @Mapping(target="createdAt",ignore=true) @Mapping(target="updatedAt",ignore=true) @Mapping(target="createdBy",ignore=true) @Mapping(target="category",ignore=true) void update(CatalogModels.ServiceRequest input,@MappingTarget ServiceEntity target);
    @BeanMapping(nullValuePropertyMappingStrategy=NullValuePropertyMappingStrategy.IGNORE) @Mapping(target="id",ignore=true) @Mapping(target="deletedAt",ignore=true) @Mapping(target="version",ignore=true) @Mapping(target="createdAt",ignore=true) @Mapping(target="updatedAt",ignore=true) @Mapping(target="createdBy",ignore=true) @Mapping(target="services",ignore=true) void update(OfferModels.OfferRequest input,@MappingTarget OfferEntity target);
}
