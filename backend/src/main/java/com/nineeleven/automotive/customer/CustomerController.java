package com.nineeleven.automotive.customer;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerController {
    private final CustomerService service;
    public CustomerController(CustomerService service){this.service=service;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Object create(@Valid @RequestBody CustomerModels.Request r){return service.create(r);}
    @GetMapping public Object list(@RequestParam(required=false)String search,@RequestParam(required=false)com.nineeleven.automotive.user.UserAccountStatus status,
            @RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return service.list(search,status,PageRequest.of(Math.max(0,page),Math.min(Math.max(1,size),100),Sort.by("name").ascending()));}
    @GetMapping("/{id}") public Object get(@PathVariable UUID id){return service.get(id);}
    @PutMapping("/{id}") public Object update(@PathVariable UUID id,@Valid @RequestBody CustomerModels.Request r){return service.update(id,r);}
    @PostMapping("/{id}/invite") @ResponseStatus(HttpStatus.NO_CONTENT) public void invite(@PathVariable UUID id,@Valid @RequestBody CustomerModels.InviteRequest r){service.invite(id,r.email());}
}
