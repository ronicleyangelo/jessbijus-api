package br.com.jessbijus.api.security;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "admin_users", schema = "jessbijus_auth")
public class AdminUser {
    @Id UUID id;
    @Column(nullable = false, unique = true, length = 80) String username;
    @Column(name = "password_hash", nullable = false, length = 255) String passwordHash;
    @Column(nullable = false, length = 20) String role;
    @Column(nullable = false) boolean active;
    @Column(name = "credential_version", nullable = false) long credentialVersion;
    @Column(name = "created_at", nullable = false) Instant createdAt;
    @Column(name = "updated_at", nullable = false) Instant updatedAt;

    protected AdminUser() {}

    static AdminUser create(String username, String hash) {
        AdminUser user = new AdminUser();
        user.id = UUID.randomUUID();
        user.username = username;
        user.passwordHash = hash;
        user.role = "ADMIN";
        user.active = true;
        user.createdAt = user.updatedAt = Instant.now();
        return user;
    }
}
