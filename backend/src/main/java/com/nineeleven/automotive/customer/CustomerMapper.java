package com.nineeleven.automotive.customer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface CustomerMapper {
    @Mapping(target="id", source="userId") @Mapping(target="phone", source="user.phone")
    @Mapping(target="email", source="user.email") @Mapping(target="status", source="user.status")
    CustomerModels.Summary summary(CustomerProfileEntity entity);
}
