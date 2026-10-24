package com.nineeleven.automotive.vehicle;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface VehicleMapper {
    @Mapping(target="customerId", source="customer.userId")
    @Mapping(target="sizeClass", expression="java(entity.getSizeClass() == null ? null : VehicleSizeClass.valueOf(entity.getSizeClass()))")
    @Mapping(target="regNumber", source="registrationNumber")
    VehicleModels.Response response(VehicleEntity entity);
    @Mapping(target="id", ignore=true) @Mapping(target="customer", ignore=true)
    @Mapping(target="registrationNumber", source="regNumber")
    @Mapping(target="sizeClass", expression="java(request.sizeClass().name())")
    void update(VehicleModels.Request request, @org.mapstruct.MappingTarget VehicleEntity entity);
}
