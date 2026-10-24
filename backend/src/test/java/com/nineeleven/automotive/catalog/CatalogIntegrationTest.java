package com.nineeleven.automotive.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nineeleven.automotive.auth.AuthTokenService;
import com.nineeleven.automotive.auth.AuthMailService;
import com.nineeleven.automotive.offer.OfferRepository;
import com.nineeleven.automotive.user.*;
import java.time.LocalDate;
import org.junit.jupiter.api.*;
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

@SpringBootTest @AutoConfigureMockMvc @Testcontainers(disabledWithoutDocker=true)
class CatalogIntegrationTest {
    @Container static final PostgreSQLContainer<?> db=new PostgreSQLContainer<>("postgres:16-alpine");
    @DynamicPropertySource static void db(DynamicPropertyRegistry r){r.add("spring.datasource.url",db::getJdbcUrl);r.add("spring.datasource.username",db::getUsername);r.add("spring.datasource.password",db::getPassword);}
    @Autowired MockMvc mvc; @Autowired JdbcTemplate jdbc; @Autowired ObjectMapper json; @Autowired UserRepository users; @Autowired AuthTokenService tokens;
    @MockBean AuthMailService mail;
    private String adminToken,customerToken;
    @BeforeEach void prepare(){jdbc.execute("TRUNCATE TABLE users CASCADE");jdbc.execute("TRUNCATE TABLE offers CASCADE");jdbc.execute("TRUNCATE TABLE service_prices CASCADE");jdbc.execute("update service_categories set active=true where slug='detailing'");jdbc.execute("update services set active=true where slug='exterior-wash-and-wax'");
        adminToken=token("admin@catalog.test",Role.ADMIN);customerToken=token("customer@catalog.test",Role.CUSTOMER);}
    private String token(String email,Role role){UserEntity u=new UserEntity();u.setEmail(email);u.setPasswordHash("unused");u.setRole(role);u.setStatus(UserAccountStatus.ACTIVE);u.setEmailVerified(true);u.setForcePasswordChange(false);u.setFailedLoginAttempts(0);users.saveAndFlush(u);return tokens.issueAccessToken(u);}
    @Test void publicRoutesHideInactiveAndExpiredRecords() throws Exception {
        jdbc.update("update service_categories set active=false where slug='detailing'");
        jdbc.update("update services set active=false where slug='exterior-wash-and-wax'");
        jdbc.update("insert into offers(id,title,discount_type,discount_value,valid_from,valid_to,active) values(gen_random_uuid(),'Expired','PERCENT',10,current_date - 10,current_date - 1,true)");
        jdbc.update("insert into offers(id,title,discount_type,discount_value,valid_from,valid_to,active) values(gen_random_uuid(),'Inactive','PERCENT',10,current_date,current_date + 10,false)");
        jdbc.update("insert into offers(id,title,discount_type,discount_value,valid_from,valid_to,active) values(gen_random_uuid(),'Current','PERCENT',10,current_date,current_date + 10,true)");
        mvc.perform(get("/api/v1/public/categories")).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.slug=='detailing')]").doesNotExist());
        mvc.perform(get("/api/v1/public/services/exterior-wash-and-wax")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/public/offers")).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.title=='Expired')]").doesNotExist())
            .andExpect(jsonPath("$[?(@.title=='Inactive')]").doesNotExist()).andExpect(jsonPath("$[?(@.title=='Current')]").exists());
    }
    @Test void adminCanCreateCategoryAndServiceAndSetPrices() throws Exception {
        String unique=java.util.UUID.randomUUID().toString().substring(0,8);
        String c=mvc.perform(post("/api/v1/admin/categories").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Test\",\"slug\":\"test-"+unique+"\",\"description\":\"Test category\",\"sortOrder\":1,\"active\":true}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String categoryId=json.readTree(c).get("id").asText();
        String s=mvc.perform(post("/api/v1/admin/services").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content("{\"categoryId\":\""+categoryId+"\",\"name\":\"Wash\",\"slug\":\"wash\",\"description\":\"Wash vehicle\",\"shortBenefit\":\"Clean finish\",\"active\":true}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.price").value((Object)null)).andReturn().getResponse().getContentAsString();
        String serviceId=json.readTree(s).get("id").asText();
        mvc.perform(put("/api/v1/admin/services/"+serviceId+"/prices/SMALL").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content("{\"price\":1250.00}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.price").value(1250.0));
        mvc.perform(put("/api/v1/admin/services/"+serviceId+"/prices/SUV").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content("{\"price\":2500.00}"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/v1/public/services/wash")).andExpect(status().isOk()).andExpect(jsonPath("$.price").value((Object)null))
            .andExpect(jsonPath("$.prices.length()").value(2)).andExpect(jsonPath("$.prices[0].sizeClass").value("SMALL")).andExpect(jsonPath("$.fromPrice").value(1250.0));
        mvc.perform(get("/api/v1/public/services?category=test-"+unique)).andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].prices.length()").value(2)).andExpect(jsonPath("$.content[0].fromPrice").value(1250.0));
        mvc.perform(get("/api/v1/public/services/wash?sizeClass=SMALL")).andExpect(status().isOk()).andExpect(jsonPath("$.price").value(1250.0));
        mvc.perform(get("/api/v1/public/vehicle-size-classes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].sizeClass").value("SMALL"));
        mvc.perform(get("/api/v1/public/services/wash?sizeClass=SEDAN")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/admin/services/"+serviceId+"/prices/SEDAN").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content("{\"price\":1250.00}"))
            .andExpect(status().isBadRequest());
    }

    @Test void publicServicesWithoutConfiguredPricesReturnEmptyPriceListAndNullFromPrice() throws Exception {
        mvc.perform(get("/api/v1/public/services/exterior-wash-and-wax")).andExpect(status().isOk())
            .andExpect(jsonPath("$.prices").isEmpty()).andExpect(jsonPath("$.fromPrice").value((Object)null));
        mvc.perform(get("/api/v1/public/services?category=detailing")).andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].prices").isEmpty()).andExpect(jsonPath("$.content[0].fromPrice").value((Object)null));
    }
    @Test void publicOffersReturnServerCalculatedDiscountedPricesForEveryConfiguredSize() throws Exception {
        String serviceId=jdbc.queryForObject("select id::text from services where slug='exterior-wash-and-wax'",String.class);
        jdbc.update("insert into service_prices(id,service_id,vehicle_size_class,price) values(gen_random_uuid(),?::uuid,'SMALL',1000),(gen_random_uuid(),?::uuid,'SUV',2000)",serviceId,serviceId);
        String body="{\"title\":\"Wash discount\",\"discountType\":\"PERCENT\",\"discountValue\":10,\"validFrom\":\"2026-01-01\",\"validTo\":\"2026-12-31\",\"appliesToAll\":false,\"active\":true,\"serviceIds\":[\""+serviceId+"\"]}";
        mvc.perform(post("/api/v1/admin/offers").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mvc.perform(get("/api/v1/public/offers")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].prices.length()").value(2))
            .andExpect(jsonPath("$[0].prices[0].price").value(900.0))
            .andExpect(jsonPath("$[0].prices[1].price").value(1800.0));
    }
    @Test void adminValidationAndAuthorizationAreEnforced() throws Exception {
        mvc.perform(get("/api/v1/admin/categories")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/admin/categories").header("Authorization","Bearer "+customerToken)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/admin/categories").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Bad\",\"slug\":\"Bad Slug\",\"sortOrder\":-1,\"active\":true}"))
            .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }
    @Test void offerRejectsInvertedDatesAndInvalidPercent() throws Exception {
        mvc.perform(post("/api/v1/admin/offers").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Bad\",\"discountType\":\"PERCENT\",\"discountValue\":10,\"validFrom\":\"2026-12-01\",\"validTo\":\"2026-01-01\",\"appliesToAll\":true,\"active\":true,\"serviceIds\":[]}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/admin/offers").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\":\"Bad percent\",\"discountType\":\"PERCENT\",\"discountValue\":101,\"validFrom\":\"2026-01-01\",\"validTo\":\"2026-12-31\",\"appliesToAll\":true,\"active\":true,\"serviceIds\":[]}"))
            .andExpect(status().isBadRequest());
    }
    @Test void adminCanUpdateAndSoftHideOffer() throws Exception {
        String serviceId=jdbc.queryForObject("select id::text from services where slug='exterior-wash-and-wax'",String.class);
        String body="{\"title\":\"Wash offer\",\"discountType\":\"PERCENT\",\"discountValue\":10,\"validFrom\":\"2026-01-01\",\"validTo\":\"2026-12-31\",\"appliesToAll\":false,\"active\":true,\"serviceIds\":[\""+serviceId+"\"]}";
        String created=mvc.perform(post("/api/v1/admin/offers").header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id=json.readTree(created).get("id").asText();
        mvc.perform(put("/api/v1/admin/offers/"+id).header("Authorization","Bearer "+adminToken).contentType(MediaType.APPLICATION_JSON).content(body.replace("Wash offer","Updated wash offer")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated wash offer"));
        mvc.perform(delete("/api/v1/admin/offers/"+id).header("Authorization","Bearer "+adminToken)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/public/offers")).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.title=='Updated wash offer')]").doesNotExist());
    }
}
