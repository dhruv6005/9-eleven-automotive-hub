package com.nineeleven.automotive.auth;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nineeleven.automotive.user.Role;
import com.nineeleven.automotive.user.UserAccountStatus;
import com.nineeleven.automotive.user.UserEntity;
import com.nineeleven.automotive.user.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthIntegrationTest {
    @Container static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    @MockBean AuthMailService mail;
    final AtomicReference<String> verification = new AtomicReference<>();
    final AtomicReference<String> reset = new AtomicReference<>();
    private static final String EMAIL = "customer@example.test";
    private static final String PASSWORD = "Workshop!Pass123";

    @BeforeEach void cleanDatabase() {
        jdbc.execute("TRUNCATE TABLE users CASCADE");
        verification.set(null); reset.set(null);
        doAnswer(invocation -> { verification.set(invocation.getArgument(1)); return null; }).when(mail).sendVerification(anyString(), anyString());
        doAnswer(invocation -> { reset.set(invocation.getArgument(1)); return null; }).when(mail).sendPasswordReset(anyString(), anyString());
    }

    @Test void registrationEmailVerificationAndLoginIssueShortLivedAccessAndSecureRefreshCookie() throws Exception {
        register();
        assertNotNull(verification.get());
        mvc.perform(get("/api/v1/auth/verify-email").param("token", verification.get()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Email verified. You can now sign in."));
        MvcResult result = login(EMAIL, PASSWORD);
        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        assertEquals("CUSTOMER", body.get("role").asText());
        assertEquals(900, body.get("expiresInSeconds").asInt());
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertNotNull(setCookie); assertTrue(setCookie.contains("HttpOnly")); assertTrue(setCookie.contains("Secure"));
        assertTrue(setCookie.contains("SameSite=Strict"));
        String refreshValue = result.getResponse().getCookie("refresh_token").getValue();
        assertNotEquals(refreshValue, jdbc.queryForObject("select token_hash from refresh_tokens", String.class));
        assertEquals("$2a$12$", users.findByEmailIgnoreCaseAndDeletedAtIsNull(EMAIL).orElseThrow().getPasswordHash().substring(0, 7));
    }

    @Test void refreshRotatesTokenAndReuseRevokesTheWholeFamily() throws Exception {
        register(); verifyCustomer();
        MvcResult loggedIn = login(EMAIL, PASSWORD);
        String original = loggedIn.getResponse().getCookie("refresh_token").getValue();
        MvcResult rotated = mvc.perform(post("/api/v1/auth/refresh").cookie(loggedIn.getResponse().getCookie("refresh_token")))
                .andExpect(status().isOk()).andReturn();
        var newCookie = rotated.getResponse().getCookie("refresh_token");
        assertNotNull(newCookie); assertNotEquals(original, newCookie.getValue());
        mvc.perform(post("/api/v1/auth/refresh").cookie(loggedIn.getResponse().getCookie("refresh_token")))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/refresh").cookie(newCookie)).andExpect(status().isUnauthorized());
    }

    @Test void logoutRevokesRefreshSession() throws Exception {
        register(); verifyCustomer();
        var cookie = login(EMAIL, PASSWORD).getResponse().getCookie("refresh_token");
        mvc.perform(post("/api/v1/auth/logout").cookie(cookie)).andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/refresh").cookie(cookie)).andExpect(status().isUnauthorized());
    }

    @Test void forgotAndResetPasswordInvalidatesOldPasswordAndSessions() throws Exception {
        register(); verifyCustomer();
        var oldSession = login(EMAIL, PASSWORD).getResponse().getCookie("refresh_token");
        mvc.perform(post("/api/v1/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("If the account exists, password reset instructions have been sent."));
        assertNotNull(reset.get());
        mvc.perform(post("/api/v1/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(new AuthModels.ResetPasswordRequest(reset.get(), "Changed!Password456"))))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/auth/refresh").cookie(oldSession)).andExpect(status().isUnauthorized());
        login(EMAIL, "Changed!Password456");
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
        assertEquals(1, jdbc.queryForObject("select count(*) from audit_log where action='PASSWORD_RESET'", Integer.class));
    }

    @Test void fifthFailedLoginLocksAccountForFifteenMinutes() throws Exception {
        register(); verifyCustomer();
        for (int i = 0; i < 5; i++) mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\",\"password\":\"Wrong!Password123\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + EMAIL + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
        assertEquals(5, jdbc.queryForObject("select failed_login_attempts from users where email=?", Integer.class, EMAIL));
        assertNotNull(jdbc.queryForObject("select locked_until from users where email=?", java.time.OffsetDateTime.class, EMAIL));
        assertEquals(6, jdbc.queryForObject("select count(*) from audit_log where action='LOGIN_FAILED'", Integer.class));
    }

    @Test void customerCannotAccessAdminRouteAndCanReadOwnProfile() throws Exception {
        register(); verifyCustomer();
        String access = mapper.readTree(login(EMAIL, PASSWORD).getResponse().getContentAsString()).get("accessToken").asText();
        mvc.perform(get("/api/v1/admin/session").header("Authorization", "Bearer " + access)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(EMAIL));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/me")
                        .header("Authorization", "Bearer " + access).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Customer\",\"phone\":\"9123456780\",\"address\":\"Test address\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated Customer"));
        mvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
    }

    @Test void seededAdminMustChangePasswordAndIsAuthorizedByRole() throws Exception {
        UserEntity admin = new UserEntity(); admin.setEmail("admin@example.test"); admin.setPasswordHash(encoder.encode(PASSWORD));
        admin.setRole(Role.ADMIN); admin.setStatus(UserAccountStatus.ACTIVE); admin.setEmailVerified(true);
        admin.setForcePasswordChange(true); admin.setFailedLoginAttempts(0); users.saveAndFlush(admin);
        JsonNode body = mapper.readTree(login("admin@example.test", PASSWORD).getResponse().getContentAsString());
        assertTrue(body.get("forcePasswordChange").asBoolean());
        String access = body.get("accessToken").asText();
        mvc.perform(get("/api/v1/admin/session").header("Authorization", "Bearer " + access)).andExpect(status().isForbidden());
        MvcResult changed = mvc.perform(post("/api/v1/auth/change-password").header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"Changed!Password456\"}"))
                .andExpect(status().isOk()).andReturn();
        String changedAccess = mapper.readTree(changed.getResponse().getContentAsString()).get("accessToken").asText();
        mvc.perform(get("/api/v1/admin/session").header("Authorization", "Bearer " + changedAccess))
                .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"));
        assertFalse(users.findByEmailIgnoreCaseAndDeletedAtIsNull("admin@example.test").orElseThrow().isForcePasswordChange());
    }

    @Test void registrationRejectsWeakPasswordAndNeverAllowsCallerToChooseAdminRole() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Customer\",\"email\":\"weak@example.test\",\"password\":\"weakpassword123\",\"role\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest());
        assertTrue(users.findByEmailIgnoreCaseAndDeletedAtIsNull("weak@example.test").isEmpty());
    }

    private void register() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Customer\",\"phone\":\"9876543210\",\"email\":\"" + EMAIL
                        + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isAccepted());
    }
    private void verifyCustomer() throws Exception {
        mvc.perform(get("/api/v1/auth/verify-email").param("token", verification.get())).andExpect(status().isOk());
    }
    private MvcResult login(String email, String password) throws Exception {
        return mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new AuthModels.LoginRequest(email, password))))
                .andExpect(status().isOk()).andReturn();
    }
}
