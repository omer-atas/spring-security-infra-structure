package com.spring.securitytutorial;

import com.spring.securitytutorial.repository.LoginThrottleJpaRepository;
import com.spring.securitytutorial.repository.SecurityAuditJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.securitytutorial.service.security.LoginThrottleService;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5433}/${POSTGRES_DB_TEST:security_test}",
        "app.security.request-body.max-size=50KB",
        "app.security.rate-limit-filter.enabled=false"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SecurityIntegrationTests {

    private static final String WEBHOOK_SIGNING_SECRET = "webhook-signing-secret";
    private static final String WEBHOOK_AUTH_SECRET = "webhook-secret";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    LoginThrottleJpaRepository loginThrottleRepository;

    @Autowired
    LoginThrottleService loginThrottleService;

    @Autowired
    SecurityAuditJpaRepository auditRepository;

    @BeforeEach
    void clearThrottleState() throws Exception {
        loginThrottleService.clearAll();
        loginThrottleRepository.deleteAll();
    }

    @Test
    void failedLoginsBeyondLimitReturnTooManyRequests() throws Exception {
        String username = "throttle-" + UUID.randomUUID();
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(loginBody(username, "wrong-password")))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username, "wrong-password")))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void refreshTokenReuseRevokesOriginalSession() throws Exception {
        JsonNode login = login("user", "user123", 200);
        String refresh = login.get("refreshToken").asText();
        String access = login.get("accessToken").asText();

        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refresh))))
                .andExpect(status().isOk());

        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refresh))))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isUnauthorized());

        assertTrue(auditRepository.countByEventType("REFRESH_TOKEN_REUSE") >= 1);
    }

    @Test
    void userWithoutAdminPermissionIsForbidden() throws Exception {
        String access = login("user", "user123", 200).get("accessToken").asText();
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + access))
                .andExpect(status().isForbidden());
    }

    @Test
    void webhookClientWithWrongSecretIsRejected() throws Exception {
        mvc.perform(post("/api/webhooks/events")
                        .header("X-Webhook-Key", "demo-webhook")
                        .header("X-Webhook-Secret", "not-the-right-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webhookClientWithValidSignatureAccepted() throws Exception {
        String body = "{\"eventType\":\"order.created\"}";
        long now = System.currentTimeMillis() / 1000;
        mvc.perform(post("/api/webhooks/events")
                        .header("X-Webhook-Key", "demo-webhook")
                        .header("X-Webhook-Secret", WEBHOOK_AUTH_SECRET)
                        .header("X-Webhook-Timestamp", String.valueOf(now))
                        .header("X-Webhook-Signature", hmacSignature(WEBHOOK_SIGNING_SECRET, now, body))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void webhookRequestWithoutSignatureIsRejected() throws Exception {
        mvc.perform(post("/api/webhooks/events")
                        .header("X-Webhook-Key", "demo-webhook")
                        .header("X-Webhook-Secret", "webhook-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"eventType\":\"order.created\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webhookRequestWithExpiredTimestampIsRejected() throws Exception {
        String body = "{\"eventType\":\"order.created\"}";
        long stale = (System.currentTimeMillis() / 1000) - 600;
        mvc.perform(post("/api/webhooks/events")
                        .header("X-Webhook-Key", "demo-webhook")
                        .header("X-Webhook-Secret", "webhook-secret")
                        .header("X-Webhook-Timestamp", String.valueOf(stale))
                        .header("X-Webhook-Signature", hmacSignature(WEBHOOK_SIGNING_SECRET, stale, body))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webhookRequestWithTamperedSignatureIsRejected() throws Exception {
        String body = "{\"eventType\":\"order.created\"}";
        long now = System.currentTimeMillis() / 1000;
        mvc.perform(post("/api/webhooks/events")
                        .header("X-Webhook-Key", "demo-webhook")
                        .header("X-Webhook-Secret", "webhook-secret")
                        .header("X-Webhook-Timestamp", String.valueOf(now))
                        .header("X-Webhook-Signature", hmacSignature(WEBHOOK_SIGNING_SECRET, now, "{\"eventType\":\"tampered\"}"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tenantClientMissingCurrentUserIsRejected() throws Exception {
        mvc.perform(post("/api/v1/orders")
                        .header("X-Tenant-Key", "mobile-bff-key")
                        .header("X-Tenant-Secret", "mobile-bff-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"integration test order\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tenantClientWithCurrentUserCanCreateOrder() throws Exception {
        UUID userId = userIdOf("user");
        mvc.perform(post("/api/v1/orders")
                        .header("X-Tenant-Key", "mobile-bff-key")
                        .header("X-Tenant-Secret", "mobile-bff-secret")
                        .header("X-Current-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"integration test order\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void tenantClientWithUnknownCurrentUserIsRejected() throws Exception {
        mvc.perform(post("/api/v1/orders")
                        .header("X-Tenant-Key", "mobile-bff-key")
                        .header("X-Tenant-Secret", "mobile-bff-secret")
                        .header("X-Current-User-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"integration test order\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tenantCanOnlyReadOwnOrders() throws Exception {
        UUID owner = userIdOf("user");
        UUID other = userIdOf("admin");
        MvcResult created = mvc.perform(post("/api/v1/orders")
                        .header("X-Tenant-Key", "mobile-bff-key")
                        .header("X-Tenant-Secret", "mobile-bff-secret")
                        .header("X-Current-User-Id", owner.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"IDOR ownership test\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String orderId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(get("/api/v1/orders/" + orderId)
                        .header("X-Tenant-Key", "mobile-bff-key")
                        .header("X-Tenant-Secret", "mobile-bff-secret")
                        .header("X-Current-User-Id", owner.toString()))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/orders/" + orderId)
                        .header("X-Tenant-Key", "mobile-bff-key")
                        .header("X-Tenant-Secret", "mobile-bff-secret")
                        .header("X-Current-User-Id", other.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedRequestsAreRejected() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginResponseIsNotCached() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));
    }

    @Test
    void tenantClientWithMalformedCurrentUserGetsBadRequest() throws Exception {
        mvc.perform(post("/api/v1/orders")
                        .header("X-Tenant-Key", "mobile-bff-key")
                        .header("X-Tenant-Secret", "mobile-bff-secret")
                        .header("X-Current-User-Id", "not-a-uuid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"integration test order\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tenantClientCannotImpersonateNonMemberUser() throws Exception {
        UUID outsider = createUserAsAdmin("outsider-" + UUID.randomUUID());
        mvc.perform(post("/api/v1/orders")
                        .header("X-Tenant-Key", "mobile-bff-key")
                        .header("X-Tenant-Secret", "mobile-bff-secret")
                        .header("X-Current-User-Id", outsider.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"impersonation attempt\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void webhookBodyOverLimitIsRejected() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("eventType", "a".repeat(60000)));
        long now = System.currentTimeMillis() / 1000;
        mvc.perform(post("/api/webhooks/events")
                        .header("X-Webhook-Key", "demo-webhook")
                        .header("X-Webhook-Secret", WEBHOOK_AUTH_SECRET)
                        .header("X-Webhook-Timestamp", String.valueOf(now))
                        .header("X-Webhook-Signature", hmacSignature(WEBHOOK_SIGNING_SECRET, now, body))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void tamperedAccessTokenIsRejected() throws Exception {
        String access = login("user", "user123", 200).get("accessToken").asText();
        String[] parts = access.split("\\.");
        char flipped = parts[2].charAt(0) == 'A' ? 'B' : 'A';
        String tampered = parts[0] + "." + parts[1] + "." + flipped + parts[2].substring(1);
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutInvalidatesAccessToken() throws Exception {
        String access = login("user", "user123", 200).get("accessToken").asText();
        mvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + access))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isUnauthorized());
    }

    private String loginBody(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(Map.of("username", username, "password", password));
    }

    private JsonNode login(String username, String password, int expectedStatus) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username, password)))
                .andExpect(status().is(expectedStatus))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private UUID userIdOf(String username) throws Exception {
        String access = login(username, username + "123", 200).get("accessToken").asText();
        String payload = access.split("\\.")[1];
        byte[] decoded = Base64.getUrlDecoder().decode(payload);
        return UUID.fromString(objectMapper.readTree(decoded).get("sub").asText());
    }

    private UUID createUserAsAdmin(String username) throws Exception {
        String access = login("admin", "admin123", 200).get("accessToken").asText();
        MvcResult result = mvc.perform(post("/api/users/create")
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", username, "password", "password123"))))
                .andExpect(status().isOk())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
    }

    private String hmacSignature(String secret, long timestamp, String body) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
        return "sha256=" + HexFormat.of().formatHex(digest);
    }
}