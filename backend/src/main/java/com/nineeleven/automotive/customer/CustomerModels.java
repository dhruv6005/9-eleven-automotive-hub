package com.nineeleven.automotive.customer;

import com.nineeleven.automotive.user.UserAccountStatus;
import com.nineeleven.automotive.vehicle.VehicleModels;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public final class CustomerModels {
    private CustomerModels() { }
    public record Request(@NotBlank @Size(max=160) String name, @NotBlank @Size(max=30) String phone,
            @Email @Size(max=320) String email, String address, String notes) { }
    public record InviteRequest(@NotBlank @Email @Size(max=320) String email) { }
    public record Summary(UUID id, String name, String phone, String email, UserAccountStatus status,
            String address, String notes) { }
    public record Detail(UUID id, String name, String phone, String email, UserAccountStatus status,
            String address, String notes, List<VehicleModels.Response> vehicles) { }
}
