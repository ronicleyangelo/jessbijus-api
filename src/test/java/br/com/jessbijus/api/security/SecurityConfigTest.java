package br.com.jessbijus.api.security;

import br.com.jessbijus.api.controller.CsrfController;
import br.com.jessbijus.api.controller.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {HealthController.class, CsrfController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "JESSBIJUS_ADMIN_USERNAME=test-admin",
        "JESSBIJUS_ADMIN_PASSWORD_HASH={bcrypt}test-only"
})
class SecurityConfigTest {
    @Autowired MockMvc mvc;

    @Test void healthIsPublic() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
    }

    @Test void adminRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/admin/products")).andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "CUSTOMER") void customerCannotAccessAdmin() throws Exception {
        mvc.perform(get("/api/admin/products")).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "ADMIN") void adminPassesAuthorization() throws Exception {
        mvc.perform(get("/api/admin/products")).andExpect(status().isNotFound());
    }

    @Test void publicOrderIsNotBlockedByCsrf() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test @WithMockUser(roles = "ADMIN") void adminWriteRequiresCsrf() throws Exception {
        mvc.perform(post("/api/admin/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}
