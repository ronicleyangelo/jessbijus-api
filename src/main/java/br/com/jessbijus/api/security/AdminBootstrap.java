package br.com.jessbijus.api.security;

import br.com.jessbijus.api.JessbijusApiApplication;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.Arrays;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.Environment;

/** Explicit offline setup command; never exposed over HTTP or run at normal startup. */
public final class AdminBootstrap {
    private AdminBootstrap() {}

    public static void main(String[] args) throws Exception {
        try (var context = new SpringApplicationBuilder(JessbijusApiApplication.class)
                .profiles("bootstrap-admin").web(WebApplicationType.NONE)
                .initializers(application -> validateDatabaseConfiguration(application.getEnvironment()))
                .run(args)) {
            createAdministrator(context.getBean(AdminAccounts.class));
        }
    }

    private static void createAdministrator(AdminAccounts accounts) throws Exception {
        Console console = System.console();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Login do administrador: ");
        String username = console != null ? console.readLine() : reader.readLine();
        if (console == null) System.out.println("Console da IDE: a senha pode ficar visível. Não salve o conteúdo do console.");
        char[] password = readPassword(console, reader, "Senha (8 a 128 caracteres, com um símbolo): ");
        char[] confirmation = readPassword(console, reader, "Confirme a senha: ");
        try {
            if (!Arrays.equals(password, confirmation)) throw new IllegalArgumentException("As senhas não conferem.");
            AdminAccounts.validatePassword(new String(password));
            accounts.bootstrap(username == null ? null : username.trim(), new String(password));
            System.out.println("Administrador criado. Inicie a API normalmente e entre no painel.");
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
        }
    }

    static void validateDatabaseConfiguration(Environment environment) {
        String url = property(environment, "spring.datasource.url");
        if (url == null || !url.startsWith("jdbc:postgresql://")) {
            throw new IllegalStateException("Configure NEON_JDBC_URL na execução AdminBootstrap: "
                    + "jdbc:postgresql://HOST/BANCO?sslmode=require. "
                    + "No IntelliJ: Edit Configurations > AdminBootstrap > Environment variables. "
                    + "As variáveis da execução da API não são herdadas automaticamente.");
        }
        if (property(environment, "spring.datasource.username") == null
                || property(environment, "spring.datasource.password") == null) {
            throw new IllegalStateException("Configure NEON_DB_USER e NEON_DB_PASSWORD na execução AdminBootstrap.");
        }
    }

    private static String property(Environment environment, String name) {
        try {
            String value = environment.getProperty(name);
            return value == null || value.isBlank() || value.contains("${") ? null : value;
        } catch (IllegalArgumentException exception) {
            return null;
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
