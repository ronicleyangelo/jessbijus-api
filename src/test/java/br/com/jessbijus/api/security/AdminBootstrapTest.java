package br.com.jessbijus.api.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminBootstrapTest {
    @Test void missingUrlGivesActionableMessageWithoutCredentials() {
        var error = assertThrows(IllegalStateException.class,
                () -> AdminBootstrap.validateDatabaseConfiguration(new MockEnvironment()));
        assertTrue(error.getMessage().contains("NEON_JDBC_URL"));
        assertTrue(error.getMessage().contains("AdminBootstrap"));
    }

    @Test void unresolvedAndNonJdbcUrlsAreRejectedWithoutEchoingTheirValues() {
        for (String url : new String[] { "${NEON_JDBC_URL}", "postgresql://user:secret@host/db" }) {
            var environment = new MockEnvironment().withProperty("spring.datasource.url", url);
            var error = assertThrows(IllegalStateException.class,
                    () -> AdminBootstrap.validateDatabaseConfiguration(environment));
            assertFalse(error.getMessage().contains(url));
        }
    }

    @Test void validDatabaseConfigurationIsAccepted() {
        var environment = new MockEnvironment()
                .withProperty("spring.datasource.url", "jdbc:postgresql://localhost/test")
                .withProperty("spring.datasource.username", "test")
                .withProperty("spring.datasource.password", "test-password");
        assertDoesNotThrow(() -> AdminBootstrap.validateDatabaseConfiguration(environment));
    }

    @Test void missingDatabaseCredentialsAreRejected() {
        var environment = new MockEnvironment().withProperty("spring.datasource.url", "jdbc:postgresql://localhost/test");
        assertThrows(IllegalStateException.class, () -> AdminBootstrap.validateDatabaseConfiguration(environment));
    }
    private final AdminUserRepository users = mock(AdminUserRepository.class);
    private final PasswordEncoder encoder = new PasswordConfiguration().passwordEncoder();
    private final EntityManager em = mock(EntityManager.class);
    private final AdminAccounts accounts = new AdminAccounts(users, encoder, em);

    @Test void refusesSecondBootstrap() {
        when(em.createNativeQuery(anyString())).thenReturn(mock(Query.class));
        when(users.count()).thenReturn(1L);
        assertThrows(IllegalStateException.class, () -> accounts.bootstrap("admin", "uma senha longa de teste!"));
        verify(users, never()).save(any());
    }

    @Test void validatesBeforeOpeningDatabase() {
        assertThrows(IllegalArgumentException.class, () -> accounts.bootstrap("admin", "short"));
        assertThrows(IllegalArgumentException.class, () -> accounts.bootstrap("a b", "uma senha longa de teste!"));
        verifyNoInteractions(em, users);
    }

    @Test void createsOnlyHashedPassword() {
        when(em.createNativeQuery(anyString())).thenReturn(mock(Query.class));
        accounts.bootstrap("admin", "uma senha longa de teste!");
        verify(users).save(argThat(user -> user.active && user.role.equals("ADMIN")
                && user.passwordHash.startsWith("{argon2id}$argon2id$")
                && encoder.matches("uma senha longa de teste!", user.passwordHash)));
    }
}
