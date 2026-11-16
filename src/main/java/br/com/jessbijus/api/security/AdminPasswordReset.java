package br.com.jessbijus.api.security;

import br.com.jessbijus.api.JessbijusApiApplication;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

/** Explicit local recovery command. It starts no HTTP server. */
public final class AdminPasswordReset {
    private AdminPasswordReset() {}

    public static void main(String[] args) throws Exception {
        try (var context = new SpringApplicationBuilder(JessbijusApiApplication.class)
                .profiles("bootstrap-admin").web(WebApplicationType.NONE)
                .initializers(application -> AdminBootstrap.validateDatabaseConfiguration(application.getEnvironment()))
                .run(args)) {
            Console console = System.console();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Login do administrador: ");
            String username = console != null ? console.readLine() : reader.readLine();
            if (console == null) System.out.println("Console da IDE: a senha pode ficar visível. Não salve o conteúdo do console.");
            char[] password = readPassword(console, reader, "Nova senha (8 a 128 caracteres, com um símbolo): ");
            char[] confirmation = readPassword(console, reader, "Confirme a nova senha: ");
            try {
                if (!Arrays.equals(password, confirmation)) throw new IllegalArgumentException("As senhas não conferem.");
                context.getBean(AdminAccounts.class).resetPassword(username == null ? "" : username.trim(), new String(password));
                System.out.println("Senha redefinida. Entre no painel com a nova senha.");
            } finally {
                Arrays.fill(password, '\0');
                Arrays.fill(confirmation, '\0');
            }
        }
    }

    private static char[] readPassword(Console console, BufferedReader reader, String prompt) throws Exception {
        if (console != null) {
            char[] value = console.readPassword("%s", prompt);
            return value == null ? new char[0] : value;
        }
        System.out.print(prompt);
        String value = reader.readLine();
        return value == null ? new char[0] : value.toCharArray();
    }
}
