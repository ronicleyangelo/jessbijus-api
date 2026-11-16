package br.com.jessbijus.api.security;

import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AdminAuthController {
    private final AdminAccounts accounts;
    public AdminAuthController(AdminAccounts accounts) { this.accounts = accounts; }

    @GetMapping("/me")
    public CurrentUser me(Authentication authentication) {
        return new CurrentUser(authentication.getName(), "ADMIN");
    }

    @PostMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void password(@Valid @RequestBody PasswordChange input, Authentication authentication,
                         HttpServletRequest request, HttpServletResponse response) {
        accounts.changePassword((AdminPrincipal) authentication.getPrincipal(), input.currentPassword(), input.newPassword());
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        var cookie = new Cookie("JSESSIONID", "");
        cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public record CurrentUser(String username, String role) {}
    public record PasswordChange(@NotBlank @Size(max = 128) String currentPassword,
                                 @NotNull @Size(min = 8, max = 128) String newPassword) {}
}
