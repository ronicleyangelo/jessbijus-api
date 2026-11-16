package br.com.jessbijus.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

/** Revalidates status/version so password changes revoke every existing session. */
final class AdminSessionFilter extends OncePerRequestFilter {
    private final AdminUserRepository users;
    AdminSessionFilter(AdminUserRepository users) { this.users = users; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminPrincipal principal) {
            var user = users.findById(principal.id());
            if (user.isEmpty() || !user.get().active
                    || user.get().credentialVersion != principal.credentialVersion()
                    || !principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + user.get().role))) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                response.setStatus(401);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
