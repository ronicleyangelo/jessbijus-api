package br.com.jessbijus.api.security;

import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Run this class locally to create the administrator password hash. */
public final class AdminPasswordHashGenerator {
    private AdminPasswordHashGenerator() {}

    public static void main(String[] args) throws Exception {
        Console console = System.console();
        char[] password;
        if (console != null) {
            password = console.readPassword("Senha do administrador: ");
        } else {
            System.out.print("Senha do administrador: ");
            password = new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray();
        }
        if (password.length < 12) {
            throw new IllegalArgumentException("Use uma senha de pelo menos 12 caracteres.");
        }
        try {
            System.out.println("JESSBIJUS_ADMIN_PASSWORD_HASH={bcrypt}" + new BCryptPasswordEncoder(12).encode(new String(password)));
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }
}
