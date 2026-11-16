package br.com.jessbijus.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JessbijusApiApplication {

	public static void main(String[] args) throws Exception {
		if (java.util.Arrays.asList(args).contains("--bootstrap-admin")) {
			br.com.jessbijus.api.security.AdminBootstrap.main(args);
			return;
		}
		if (java.util.Arrays.asList(args).contains("--reset-admin-password")) {
			br.com.jessbijus.api.security.AdminPasswordReset.main(args);
			return;
		}
		SpringApplication.run(JessbijusApiApplication.class, args);
	}

}
