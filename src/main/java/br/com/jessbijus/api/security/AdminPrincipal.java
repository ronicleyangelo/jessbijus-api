package br.com.jessbijus.api.security;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public final class AdminPrincipal extends User {
    private final UUID id;
    private final long credentialVersion;

    AdminPrincipal(AdminUser user) {
        super(user.username, user.passwordHash, user.active, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.role)));
        id = user.id;
        credentialVersion = user.credentialVersion;
    }

    public UUID id() { return id; }
    public long credentialVersion() { return credentialVersion; }
}
