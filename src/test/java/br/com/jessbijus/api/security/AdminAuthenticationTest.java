package br.com.jessbijus.api.security;

import br.com.jessbijus.api.port.CatalogUseCase;
import br.com.jessbijus.api.domain.ProductInput;
import java.util.List;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=update", "server.servlet.session.cookie.secure=false"
})
@AutoConfigureMockMvc
class AdminAuthenticationTest {
    private static final String PASSWORD = "uma senha longa de teste";
    private static final String NEW_PASSWORD = "outra senha longa de teste!";
    private static final AtomicInteger IPS = new AtomicInteger();
    @Autowired MockMvc mvc;
    @Autowired AdminUserRepository users;
    @Autowired PasswordEncoder encoder;
    private AdminUser user;
    private String ip;

    @Test void productSubcategoriesSurviveReloadAndIndividualRemoval(@Autowired CatalogUseCase catalog) {
        var category = catalog.createCategory("Conjuntos", "conjuntos");
        var product = catalog.saveProduct(null, new ProductInput(category.id(), "Dueto", "", BigDecimal.TEN,
                2, true, false, null, List.of(), List.of("Aurora", "Sereno")));
        assertEquals(List.of("Aurora", "Sereno"), catalog.product(product.id()).subcategories());
        catalog.saveProduct(product.id(), new ProductInput(category.id(), "Dueto", "", BigDecimal.TEN,
                2, true, false, null, List.of(), List.of("Sereno")));
        assertEquals(List.of("Sereno"), catalog.product(product.id()).subcategories());
    }
    @BeforeEach void prepare() {
        users.deleteAll();
        user = users.saveAndFlush(AdminUser.create("admin", encoder.encode(PASSWORD)));
        ip = "192.0.2." + IPS.incrementAndGet();
    }

    private MockHttpServletRequestBuilder loginRequest(String username, String password) {
        return post("/api/auth/login").param("username", username).param("password", password)
                .with(request -> { request.setRemoteAddr(ip); return request; });
    }

    private MockHttpSession login() throws Exception {
        return (MockHttpSession) mvc.perform(loginRequest("admin", PASSWORD).with(realCsrf()))
                .andExpect(status().isNoContent()).andReturn().getRequest().getSession(false);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor realCsrf() throws Exception {
        var cookie = mvc.perform(get("/api/csrf")).andExpect(status().isOk())
                .andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);
        return request -> {
            request.setCookies(cookie);
            request.addHeader("X-XSRF-TOKEN", cookie.getValue());
            return request;
        };
    }

    @Test void passwordsUseSaltedArgon2id() {
        assertTrue(user.passwordHash.startsWith("{argon2id}$argon2id$"));
        assertTrue(encoder.matches(PASSWORD, user.passwordHash));
        assertNotEquals(user.passwordHash, encoder.encode(PASSWORD));
    }

    @Test void loginRestoresSessionAndLogoutRevokesIt() throws Exception {
        var session = login();
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        mvc.perform(post("/api/auth/logout").session(session).with(realCsrf())).andExpect(status().isNoContent());
        assertTrue(session.isInvalid());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test void invalidCredentialsAndInactiveUsersHaveSameResponse() throws Exception {
        mvc.perform(loginRequest("admin", "wrong").with(realCsrf())).andExpect(status().isUnauthorized());
        mvc.perform(loginRequest("unknown", PASSWORD).with(realCsrf())).andExpect(status().isUnauthorized());
        user.active = false;
        users.saveAndFlush(user);
        mvc.perform(loginRequest("admin", PASSWORD).with(realCsrf())).andExpect(status().isUnauthorized());
    }

    @Test void csrfRequiredForLoginLogoutAndPassword() throws Exception {
        mvc.perform(loginRequest("admin", PASSWORD)).andExpect(status().isForbidden());
        var session = login();
        mvc.perform(post("/api/auth/logout").session(session)).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/password").session(session).contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"x\",\"newPassword\":\"something long enough\"}"))
                .andExpect(status().isForbidden());
    }

    @Test void csrfCookieWorksWithAngularHeader() throws Exception {
        var result = mvc.perform(get("/api/csrf")).andExpect(status().isOk()).andReturn();
        var cookie = result.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);
        mvc.perform(loginRequest("admin", PASSWORD).cookie(cookie).header("X-XSRF-TOKEN", cookie.getValue()))
                .andExpect(status().isNoContent());
    }

    @Test void changingPasswordInvalidatesAllSessionsAndOldPassword() throws Exception {
        var first = login();
        var second = login();
        mvc.perform(post("/api/auth/password").session(first).with(realCsrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"" + NEW_PASSWORD + "\"}"))
                .andExpect(status().isNoContent());
        assertTrue(first.isInvalid());
        mvc.perform(get("/api/auth/me").session(second)).andExpect(status().isUnauthorized());
        assertTrue(second.isInvalid());
        mvc.perform(loginRequest("admin", PASSWORD).with(realCsrf())).andExpect(status().isUnauthorized());
        ip = "192.0.2." + IPS.incrementAndGet();
        mvc.perform(loginRequest("admin", NEW_PASSWORD).with(realCsrf())).andExpect(status().isNoContent());
    }

    @Test void wrongCurrentPasswordAndShortNewPasswordDoNotChangeCredentials() throws Exception {
        var session = login();
        mvc.perform(post("/api/auth/password").session(session).with(realCsrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"incorrect\",\"newPassword\":\"" + NEW_PASSWORD + "\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/password").session(session).with(realCsrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"" + PASSWORD + "\",\"newPassword\":\"short\"}"))
                .andExpect(status().isBadRequest());
        assertTrue(encoder.matches(PASSWORD, users.findById(user.id).orElseThrow().passwordHash));
    }

    @Test void deactivationRevokesExistingSession() throws Exception {
        var session = login();
        user.active = false;
        users.saveAndFlush(user);
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
    }

    @Test void localPasswordResetRevokesSessions(@Autowired AdminAccounts accounts) throws Exception {
        var session = login();
        accounts.resetPassword("admin", "nova senha forte!");
        mvc.perform(get("/api/auth/me").session(session)).andExpect(status().isUnauthorized());
        assertTrue(session.isInvalid());
        mvc.perform(loginRequest("admin", PASSWORD).with(realCsrf())).andExpect(status().isUnauthorized());
        mvc.perform(loginRequest("admin", "nova senha forte!").with(realCsrf())).andExpect(status().isNoContent());
    }

    @Test void oversizedOrderIsRejectedBeforeJsonParsing() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("x".repeat(32769)))
                .andExpect(status().is(413));
    }

    @Test void tooManyOrderLinesAreRejectedBeforeProductLookup() throws Exception {
        String line = "{\"productId\":\"11111111-1111-1111-1111-111111111111\",\"quantity\":1}";
        String items = String.join(",", java.util.Collections.nCopies(101, line));
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"test\",\"items\":[" + items + "]}"))
                .andExpect(status().isBadRequest());
    }
    @Test void excessiveLoginsReturn429() throws Exception {
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(loginRequest("locked-test", "wrong").with(realCsrf())).andExpect(status().isUnauthorized());
        }
        mvc.perform(loginRequest("locked-test", "wrong").with(realCsrf()))
                .andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After"));
        ip = "192.0.2." + IPS.incrementAndGet();
        mvc.perform(loginRequest("locked-test", PASSWORD).with(realCsrf()).header("X-Forwarded-For", "203.0.113.99"))
                .andExpect(status().isTooManyRequests());
    }

    @Test void httpBasicNoLongerAuthenticates() throws Exception {
        mvc.perform(get("/api/auth/me").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic("admin", PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test void eachAccountReadsAndUpdatesOnlyItsOwnProfile() throws Exception {
        var first = login();
        mvc.perform(get("/api/admin/profile").session(first)).andExpect(status().isNotFound());
        mvc.perform(put("/api/admin/profile").session(first).with(realCsrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Primeiro administrador\",\"nickname\":\"Primeiro\",\"photoUrl\":null}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nickname").value("Primeiro"));

        users.saveAndFlush(AdminUser.create("second-admin", encoder.encode(PASSWORD)));
        var second = (MockHttpSession) mvc.perform(loginRequest("second-admin", PASSWORD).with(realCsrf()))
                .andExpect(status().isNoContent()).andReturn().getRequest().getSession(false);
        mvc.perform(get("/api/admin/profile").session(second)).andExpect(status().isNotFound());
        mvc.perform(put("/api/admin/profile").session(second).with(realCsrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Segundo administrador\",\"nickname\":\"Segundo\",\"photoUrl\":null}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nickname").value("Segundo"));
        mvc.perform(get("/api/admin/profile").session(first)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("Primeiro"));
        mvc.perform(get("/api/admin/profile").session(second)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("Segundo"));
    }
}
