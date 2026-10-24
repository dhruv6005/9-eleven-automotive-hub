package com.nineeleven.automotive.customer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.nineeleven.automotive.auth.AuthMailService;
import com.nineeleven.automotive.auth.AuthTokenService;
import com.nineeleven.automotive.user.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest @AutoConfigureMockMvc @Testcontainers(disabledWithoutDocker=true)
class CustomerVehicleIntegrationTest {
    @Container static final PostgreSQLContainer<?> db=new PostgreSQLContainer<>("postgres:16-alpine");
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r){r.add("spring.datasource.url",db::getJdbcUrl);r.add("spring.datasource.username",db::getUsername);r.add("spring.datasource.password",db::getPassword);}
    @Autowired MockMvc mvc; @Autowired JdbcTemplate jdbc; @Autowired UserRepository users; @Autowired AuthTokenService tokens;
    @MockBean AuthMailService mail;
    AtomicReference<String> resetToken=new AtomicReference<>();
    @BeforeEach void clean(){jdbc.execute("TRUNCATE TABLE users CASCADE");resetToken.set(null);doAnswer(i->{resetToken.set(i.getArgument(1));return null;}).when(mail).sendSetPassword(anyString(),anyString());}

    @Test void everyMeVehicleEndpointHidesWrongOwnerAndEnforcesAuthenticationAndRole() throws Exception {
        UserEntity owner=user("owner@test.example",Role.CUSTOMER); UserEntity other=user("other@test.example",Role.CUSTOMER); UserEntity admin=user("admin@test.example",Role.ADMIN);
        UUID vehicle=UUID.randomUUID(); jdbc.update("insert into customer_profiles(user_id,name) values (?, 'Owner')",owner.getId());
        jdbc.update("insert into customer_profiles(user_id,name) values (?, 'Other')",other.getId());
        jdbc.update("insert into vehicles(id,customer_id,make,model,reg_number,size_class) values (?,?,'Toyota','Camry','KA01AB1234','MEDIUM')",vehicle,owner.getId());
        var paths=new String[]{"/api/v1/me/vehicles","/api/v1/me/vehicles/"+vehicle};
        mvc.perform(get(paths[0])).andExpect(status().isUnauthorized());
        mvc.perform(post(paths[0]).contentType(MediaType.APPLICATION_JSON).content(vehicleJson("KA02XY0001"))).andExpect(status().isUnauthorized());
        mvc.perform(get(paths[1])).andExpect(status().isUnauthorized());
        mvc.perform(put(paths[1]).contentType(MediaType.APPLICATION_JSON).content(vehicleJson("KA03XY0001"))).andExpect(status().isUnauthorized());
        mvc.perform(delete(paths[1])).andExpect(status().isUnauthorized());
        mvc.perform(get(paths[0]).header("Authorization",bearer(admin))).andExpect(status().isForbidden());
        mvc.perform(post(paths[0]).header("Authorization",bearer(admin)).contentType(MediaType.APPLICATION_JSON).content(vehicleJson("KA02XY0001"))).andExpect(status().isForbidden());
        mvc.perform(get(paths[1]).header("Authorization",bearer(admin))).andExpect(status().isForbidden());
        mvc.perform(put(paths[1]).header("Authorization",bearer(admin)).contentType(MediaType.APPLICATION_JSON).content(vehicleJson("KA03XY0001"))).andExpect(status().isForbidden());
        mvc.perform(delete(paths[1]).header("Authorization",bearer(admin))).andExpect(status().isForbidden());
        mvc.perform(get(paths[0]).header("Authorization",bearer(other))).andExpect(status().isOk()).andExpect(jsonPath("$",org.hamcrest.Matchers.hasSize(0)));
        mvc.perform(get(paths[1]).header("Authorization",bearer(other))).andExpect(status().isNotFound());
        mvc.perform(put(paths[1]).header("Authorization",bearer(other)).contentType(MediaType.APPLICATION_JSON).content(vehicleJson("KA03XY0001"))).andExpect(status().isNotFound());
        mvc.perform(delete(paths[1]).header("Authorization",bearer(other))).andExpect(status().isNotFound());
        mvc.perform(post(paths[0]).header("Authorization",bearer(other)).contentType(MediaType.APPLICATION_JSON).content(vehicleJson("KA02XY0001"))).andExpect(status().isCreated());
    }
    @Test void adminCustomerSearchRegistrationNormalizationDuplicateSoftDeleteAndInviteFlow() throws Exception {
        UserEntity admin=user("admin@test.example",Role.ADMIN); String auth=bearer(admin);
        mvc.perform(post("/api/v1/admin/customers").header("Authorization",auth).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Mina\",\"phone\":\"+91 98765-43210\",\"email\":\"mina@example.test\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("NO_LOGIN"));
        UUID customer=jdbc.queryForObject("select user_id from customer_profiles where name='Mina'",UUID.class);
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"mina@example.test\",\"password\":\"Set!MyPassword123\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.detail").value("Invalid email or password, or account unavailable."));
        mvc.perform(post("/api/v1/admin/customers").header("Authorization",auth).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Duplicate\",\"phone\":\"9876543210\"}")).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/admin/customers/"+customer+"/vehicles").header("Authorization",auth).contentType(MediaType.APPLICATION_JSON).content(vehicleJson("ka-01 ab 1234")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.regNumber").value("KA01AB1234"));
        mvc.perform(post("/api/v1/admin/customers/"+customer+"/vehicles").header("Authorization",auth).contentType(MediaType.APPLICATION_JSON).content(vehicleJson("KA01AB1234"))).andExpect(status().isConflict());
        mvc.perform(get("/api/v1/admin/customers").param("search","KA01AB1234").header("Authorization",auth)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
        UUID vehicle=jdbc.queryForObject("select id from vehicles where customer_id=?",UUID.class,customer);
        mvc.perform(delete("/api/v1/admin/customers/"+customer+"/vehicles/"+vehicle).header("Authorization",auth)).andExpect(status().isNoContent());
        assertNotNull(jdbc.queryForObject("select deleted_at from vehicles where id=?",java.time.OffsetDateTime.class,vehicle));
        mvc.perform(post("/api/v1/admin/customers/"+customer+"/invite").header("Authorization",auth).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"mina@example.test\"}"))
                .andExpect(status().isNoContent()); assertNotNull(resetToken.get());
        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content("{\"token\":\""+resetToken.get()+"\",\"password\":\"Set!MyPassword123\"}"))
                .andExpect(status().isOk());
        assertEquals("ACTIVE",jdbc.queryForObject("select status from users where id=?",String.class,customer));
        mvc.perform(get("/api/v1/admin/customers").header("Authorization",bearer(user("customer@test.example",Role.CUSTOMER)))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/customers")).andExpect(status().isUnauthorized());
    }
    private UserEntity user(String email,Role role){UserEntity u=new UserEntity();u.setEmail(email);u.setPasswordHash("unused");u.setRole(role);u.setStatus(UserAccountStatus.ACTIVE);u.setEmailVerified(true);u.setFailedLoginAttempts(0);u.setForcePasswordChange(false);return users.saveAndFlush(u);}
    private String bearer(UserEntity user){return "Bearer "+tokens.issueAccessToken(user);}
    private String vehicleJson(String reg){return "{\"make\":\"Toyota\",\"model\":\"Camry\",\"sizeClass\":\"MEDIUM\",\"regNumber\":\""+reg+"\"}";}
}
