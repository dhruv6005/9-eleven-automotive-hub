package com.nineeleven.automotive.vehicle;

import com.nineeleven.automotive.customer.CustomerProfileEntity;
import com.nineeleven.automotive.customer.CustomerProfileRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VehicleService {
    private final VehicleRepository vehicles; private final CustomerProfileRepository customers;
    private final VehicleMapper mapper; private final JdbcTemplate jdbc;
    public VehicleService(VehicleRepository vehicles, CustomerProfileRepository customers, VehicleMapper mapper, JdbcTemplate jdbc) {
        this.vehicles=vehicles; this.customers=customers; this.mapper=mapper; this.jdbc=jdbc;
    }
    @Transactional(readOnly=true) public List<VehicleModels.Response> mine(UUID id) { return vehicles.activeForCustomer(id).stream().map(mapper::response).toList(); }
    @Transactional(readOnly=true) public VehicleModels.Response getMine(UUID user, UUID id) { return mapper.response(owned(user,id)); }
    @Transactional public VehicleModels.Response createMine(UUID user, VehicleModels.Request request) { return create(customer(user),request); }
    @Transactional public VehicleModels.Response updateMine(UUID user, UUID id, VehicleModels.Request request) { var v=owned(user,id); update(v,request); audit("VEHICLE_UPDATED", "VEHICLE", id); return mapper.response(v); }
    @Transactional public void deleteMine(UUID user, UUID id) { delete(owned(user,id)); }
    @Transactional(readOnly=true) public List<VehicleModels.Response> forCustomer(UUID customerId) { return vehicles.activeForCustomer(customer(customerId).getUserId()).stream().map(mapper::response).toList(); }
    @Transactional public VehicleModels.Response createForCustomer(UUID customerId, VehicleModels.Request request) { return create(customer(customerId),request); }
    @Transactional public VehicleModels.Response updateForCustomer(UUID customerId, UUID id, VehicleModels.Request request) {
        VehicleEntity v=vehicles.ownedActive(customerId,id).orElseThrow(VehicleService::notFound); update(v,request); audit("VEHICLE_UPDATED","VEHICLE",id); return mapper.response(v);
    }
    @Transactional(readOnly=true) public VehicleModels.Response getAdmin(UUID customerId, UUID id) {
        customer(customerId); return mapper.response(vehicles.ownedActive(customerId,id).orElseThrow(VehicleService::notFound));
    }
    @Transactional public void deleteForCustomer(UUID customerId, UUID id) { delete(vehicles.ownedActive(customerId,id).orElseThrow(VehicleService::notFound)); }
    @Transactional(readOnly=true) public org.springframework.data.domain.Page<VehicleModels.Response> search(String query, org.springframework.data.domain.Pageable page) {
        return vehicles.search(query==null?"":normalizeRegistration(query),page).map(mapper::response);
    }
    private VehicleModels.Response create(CustomerProfileEntity c, VehicleModels.Request r) {
        VehicleEntity v=new VehicleEntity(); v.setCustomer(c); update(v,r); var saved=vehicles.saveAndFlush(v); audit("VEHICLE_CREATED","VEHICLE",saved.getId()); return mapper.response(saved);
    }
    private void update(VehicleEntity v, VehicleModels.Request r) {
        String reg=normalizeRegistration(r.regNumber());
        if(reg.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Registration number is required.");
        if(vehicles.registrationExists(reg,v.getId())) throw new ResponseStatusException(HttpStatus.CONFLICT,"An active vehicle already uses this registration number.");
        mapper.update(r,v); v.setRegistrationNumber(reg);
    }
    private void delete(VehicleEntity v) {
        // The FK from jobs is retained; hiding the vehicle keeps historical job data intact.
        v.setDeletedAt(Instant.now()); audit("VEHICLE_DELETED","VEHICLE",v.getId());
    }
    private VehicleEntity owned(UUID user,UUID id){return vehicles.ownedActive(user,id).orElseThrow(VehicleService::notFound);}
    private CustomerProfileEntity customer(UUID id){return customers.findCustomer(id).orElseThrow(VehicleService::notFound);}
    private static ResponseStatusException notFound(){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Resource not found.");}
    public static String normalizeRegistration(String value){return value==null?"":value.toUpperCase(java.util.Locale.ROOT).replaceAll("[\\s-]","");}
    private void audit(String action,String type,UUID id){ Authentication a=SecurityContextHolder.getContext().getAuthentication(); UUID actor=null; try{if(a!=null)actor=UUID.fromString(a.getName());}catch(IllegalArgumentException ignored){} jdbc.update("insert into audit_log (id,user_id,action,entity_type,entity_id,details,correlation_id) values (?, ?, ?, ?, ?, null, ?)",UUID.randomUUID(),actor,action,type,id,MDC.get("correlationId")); }
}
