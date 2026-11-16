package br.com.jessbijus.api.security;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfiguration {
    @Bean
    PasswordEncoder passwordEncoder() {
        // Argon2id: 19 MiB, two iterations, one lane. Benchmark before raising these costs.
        return new DelegatingPasswordEncoder("argon2id", Map.of(
                "argon2id", new Argon2PasswordEncoder(16, 32, 1, 19 * 1024, 2)));
    }
}
