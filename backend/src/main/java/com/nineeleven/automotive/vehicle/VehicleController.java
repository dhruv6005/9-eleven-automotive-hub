package com.nineeleven.automotive.vehicle;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class VehicleController {
    private final VehicleService service;
    public VehicleController(VehicleService service){this.service=service;}
    @GetMapping("/api/v1/me/vehicles") public Object mine(@AuthenticationPrincipal Jwt jwt){return service.mine(UUID.fromString(jwt.getSubject()));}
    @PostMapping("/api/v1/me/vehicles") @ResponseStatus(HttpStatus.CREATED) public Object createMine(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody VehicleModels.Request r){return service.createMine(UUID.fromString(jwt.getSubject()),r);}
    @GetMapping("/api/v1/me/vehicles/{id}") public Object getMine(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return service.getMine(UUID.fromString(jwt.getSubject()),id);}
    @PutMapping("/api/v1/me/vehicles/{id}") public Object updateMine(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id,@Valid @RequestBody VehicleModels.Request r){return service.updateMine(UUID.fromString(jwt.getSubject()),id,r);}
    @DeleteMapping("/api/v1/me/vehicles/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteMine(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){service.deleteMine(UUID.fromString(jwt.getSubject()),id);}
    @GetMapping("/api/v1/admin/customers/{customerId}/vehicles") @PreAuthorize("hasRole('ADMIN')") public Object list(@PathVariable UUID customerId){return service.forCustomer(customerId);}
    @PostMapping("/api/v1/admin/customers/{customerId}/vehicles") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.CREATED) public Object create(@PathVariable UUID customerId,@Valid @RequestBody VehicleModels.Request r){return service.createForCustomer(customerId,r);}
    @GetMapping("/api/v1/admin/customers/{customerId}/vehicles/{id}") @PreAuthorize("hasRole('ADMIN')") public Object get(@PathVariable UUID customerId,@PathVariable UUID id){return service.getAdmin(customerId,id);}
    @PutMapping("/api/v1/admin/customers/{customerId}/vehicles/{id}") @PreAuthorize("hasRole('ADMIN')") public Object update(@PathVariable UUID customerId,@PathVariable UUID id,@Valid @RequestBody VehicleModels.Request r){return service.updateForCustomer(customerId,id,r);}
    @DeleteMapping("/api/v1/admin/customers/{customerId}/vehicles/{id}") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID customerId,@PathVariable UUID id){service.deleteForCustomer(customerId,id);}
    @GetMapping("/api/v1/admin/vehicles") @PreAuthorize("hasRole('ADMIN')") public Object search(@RequestParam(required=false) String search,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.search(search,PageRequest.of(Math.max(page,0),Math.min(Math.max(size,1),100),Sort.by("registrationNumber")));}
}
