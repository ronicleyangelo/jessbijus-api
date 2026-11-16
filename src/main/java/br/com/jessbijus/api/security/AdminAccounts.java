package br.com.jessbijus.api.security;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAccounts implements UserDetailsService {
    private final AdminUserRepository users;
    private final PasswordEncoder encoder;
    private final EntityManager entityManager;

    public AdminAccounts(AdminUserRepository users, PasswordEncoder encoder, EntityManager entityManager) {
        this.users = users;
        this.encoder = encoder;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        return new AdminPrincipal(users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials")));
    }

    @Transactional
    public void bootstrap(String username, String password) {
        if (username == null || !username.matches("[A-Za-z0-9._-]{3,80}")) {
            throw new IllegalArgumentException("Use um login de 3 a 80 caracteres: letras, números, ponto, _ ou -.");
        }
        validatePassword(password);
        // Serializes initial setup even when two operators run the command concurrently.
        entityManager.createNativeQuery("LOCK TABLE jessbijus_auth.admin_users IN EXCLUSIVE MODE").executeUpdate();
        if (users.count() != 0) throw new IllegalStateException("O administrador inicial já foi criado.");
        users.save(AdminUser.create(username, encoder.encode(password)));
    }

    @Transactional
    public void changePassword(AdminPrincipal principal, String currentPassword, String newPassword) {
        validatePassword(newPassword);
        AdminUser user = users.findForUpdate(principal.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!user.active || user.credentialVersion != principal.credentialVersion()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (!encoder.matches(currentPassword, user.passwordHash)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta.");
        }
        if (encoder.matches(newPassword, user.passwordHash)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escolha uma senha diferente da atual.");
        }
        user.passwordHash = encoder.encode(newPassword);
        user.credentialVersion++;
        user.updatedAt = Instant.now();
    }

    @Transactional
    public void resetPassword(String username, String newPassword) {
        validatePassword(newPassword);
        AdminUser account = users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Administrador não encontrado."));
        AdminUser locked = users.findForUpdate(account.id)
                .orElseThrow(() -> new IllegalArgumentException("Administrador não encontrado."));
        if (!locked.active) throw new IllegalStateException("Administrador desativado.");
        if (encoder.matches(newPassword, locked.passwordHash)) {
            throw new IllegalArgumentException("Escolha uma senha diferente da atual.");
        }
        locked.passwordHash = encoder.encode(newPassword);
        locked.credentialVersion++;
        locked.updatedAt = Instant.now();
    }

    static void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 128 ||
                password.codePoints().noneMatch(character -> !Character.isLetterOrDigit(character) && !Character.isWhitespace(character))) {
            throw new IllegalArgumentException("Use de 8 a 128 caracteres e pelo menos um símbolo.");
        }
    }
}
