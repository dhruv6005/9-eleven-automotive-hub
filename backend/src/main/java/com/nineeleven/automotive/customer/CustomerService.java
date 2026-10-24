package com.nineeleven.automotive.customer;

import com.nineeleven.automotive.auth.AuthService;
import com.nineeleven.automotive.user.Role;
import com.nineeleven.automotive.user.UserAccountStatus;
import com.nineeleven.automotive.user.UserEntity;
import com.nineeleven.automotive.user.UserRepository;
import com.nineeleven.automotive.vehicle.VehicleMapper;
import com.nineeleven.automotive.vehicle.VehicleRepository;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {
    private final CustomerProfileRepository profiles; private final UserRepository users; private final VehicleRepository vehicles;
    private final CustomerMapper mapper; private final VehicleMapper vehicleMapper; private final PasswordEncoder passwords;
    private final AuthService auth; private final JdbcTemplate jdbc;
    public CustomerService(CustomerProfileRepository profiles,UserRepository users,VehicleRepository vehicles,CustomerMapper mapper,
            VehicleMapper vehicleMapper,PasswordEncoder passwords,AuthService auth,JdbcTemplate jdbc){this.profiles=profiles;this.users=users;this.vehicles=vehicles;this.mapper=mapper;this.vehicleMapper=vehicleMapper;this.passwords=passwords;this.auth=auth;this.jdbc=jdbc;}
    @Transactional public CustomerModels.Summary create(CustomerModels.Request r){
        String phone=normalizePhone(r.phone()); checkPhone(phone,null); String email=normalizeEmail(r.email());
        if(email!=null&&users.existsByEmailIgnoreCaseAndDeletedAtIsNull(email))throw conflict("An account with this email already exists.");
        UserEntity u=new UserEntity();u.setPhone(phone);u.setEmail(email);u.setPasswordHash(passwords.encode(UUID.randomUUID().toString()+"Aa!1"));
        u.setRole(Role.CUSTOMER);u.setStatus(UserAccountStatus.NO_LOGIN);u.setEmailVerified(false);u.setFailedLoginAttempts(0);u.setForcePasswordChange(false);users.saveAndFlush(u);
        CustomerProfileEntity c=new CustomerProfileEntity();c.setUser(u);c.setName(r.name().trim());c.setAddress(r.address());c.setNotes(r.notes());profiles.save(c);audit("CUSTOMER_CREATED",c.getUserId());return mapper.summary(c);
    }
    @Transactional(readOnly=true) public Page<CustomerModels.Summary> list(String search,UserAccountStatus status,Pageable p){return profiles.search(search==null||search.isBlank()?null:search.trim(),status,p).map(mapper::summary);}
    @Transactional(readOnly=true) public CustomerModels.Detail get(UUID id){var c=profile(id);return detail(c);}
    @Transactional public CustomerModels.Summary update(UUID id,CustomerModels.Request r){var c=profile(id);String phone=normalizePhone(r.phone());checkPhone(phone,id);String email=normalizeEmail(r.email());
        if(email!=null&&users.existsByEmailIgnoreCaseAndDeletedAtIsNull(email)&&!email.equalsIgnoreCase(c.getUser().getEmail()))throw conflict("An account with this email already exists.");
        c.setName(r.name().trim());c.setAddress(r.address());c.setNotes(r.notes());c.getUser().setPhone(phone);
        // Email invitations are deliberately explicit; editing a profile never activates or changes its login identity.
        if(email!=null&&!email.equalsIgnoreCase(c.getUser().getEmail()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Use the invite endpoint to add or change the customer email.");
        audit("CUSTOMER_UPDATED",id);return mapper.summary(c);}
    @Transactional public void invite(UUID id,String email){var c=profile(id);auth.inviteCustomer(c.getUser(),email);audit("CUSTOMER_INVITED",id);}
    private CustomerModels.Detail detail(CustomerProfileEntity c){return new CustomerModels.Detail(c.getUserId(),c.getName(),c.getUser().getPhone(),c.getUser().getEmail(),c.getUser().getStatus(),c.getAddress(),c.getNotes(),vehicles.activeForCustomer(c.getUserId()).stream().map(vehicleMapper::response).toList());}
    private CustomerProfileEntity profile(UUID id){return profiles.findCustomer(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Customer not found."));}
    private void checkPhone(String phone,UUID except){Integer count=jdbc.queryForObject("select count(*) from users u join customer_profiles c on c.user_id=u.id where u.role='CUSTOMER' and u.deleted_at is null and regexp_replace(coalesce(u.phone,''),'[^0-9]','','g')=regexp_replace(?,'[^0-9]','','g') and (?::uuid is null or u.id<>?)",Integer.class,phone,except,except);if(count!=null&&count>0)throw conflict("A customer with this phone number already exists.");}
    private static String normalizePhone(String s){String n=s.trim().replaceAll("[^0-9+]","");if(n.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Phone number is required.");return n;}
    private static String normalizeEmail(String s){return s==null||s.isBlank()?null:s.trim().toLowerCase(java.util.Locale.ROOT);}
    private static ResponseStatusException conflict(String msg){return new ResponseStatusException(HttpStatus.CONFLICT,msg);}
    private void audit(String action,UUID id){Authentication a=SecurityContextHolder.getContext().getAuthentication();UUID actor=null;try{if(a!=null)actor=UUID.fromString(a.getName());}catch(IllegalArgumentException ignored){}jdbc.update("insert into audit_log (id,user_id,action,entity_type,entity_id,details,correlation_id) values (?, ?, ?, 'CUSTOMER', ?, null, ?)",UUID.randomUUID(),actor,action,id,MDC.get("correlationId"));}
}
