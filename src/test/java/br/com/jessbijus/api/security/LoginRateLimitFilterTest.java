package br.com.jessbijus.api.security;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginRateLimitFilterTest {
    private final Clock clock = mock(Clock.class);
    private final LoginRateLimitFilter filter = new LoginRateLimitFilter(clock);

    private MockHttpServletResponse login(String name, int status) throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setParameter("username", name);
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> response.setStatus(status));
        return response;
    }

    @Test void successResetsFailuresAndBlockExpires() throws Exception {
        assertEquals(401, login("one", 401).getStatus());
        assertEquals(204, login("one", 204).getStatus());
        for (int i = 0; i < 2; i++) assertEquals(401, login("one", 401).getStatus());
        assertEquals("900", login("one", 401).getHeader("Retry-After"));
        assertEquals(204, login("another", 204).getStatus());
        when(clock.millis()).thenReturn(60_000L);
        assertEquals("840", login("one", 204).getHeader("Retry-After"));
        when(clock.millis()).thenReturn(900_000L);
        assertEquals(204, login("one", 204).getStatus());
    }

    @Test void parallelRequestsReserveOnlyThreeChecksPerAccount() throws Exception {
        var entered = new java.util.concurrent.CountDownLatch(3);
        var release = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(3)) {
            try {
                for (int i = 0; i < 3; i++) executor.submit(() -> {
                    var request = new MockHttpServletRequest("POST", "/api/auth/login");
                    request.setParameter("username", "same");
                    var response = new MockHttpServletResponse();
                    filter.doFilter(request, response, (req, res) -> {
                        entered.countDown();
                        try {
                            release.await();
                        } catch (InterruptedException error) {
                            Thread.currentThread().interrupt();
                        }
                        response.setStatus(401);
                    });
                    return null;
                });
                assertTrue(entered.await(5, java.util.concurrent.TimeUnit.SECONDS));
                assertEquals(429, login("same", 204).getStatus());
            } finally {
                release.countDown();
            }
        }
    }
    @Test void ipBudgetSpansAccounts() throws Exception {
        for (int i = 0; i < 11; i++) assertEquals(401, login("user" + i, 401).getStatus());
        assertEquals(429, login("other", 401).getStatus());
        assertEquals(429, login("another", 204).getStatus());
    }
}
