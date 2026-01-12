package com.example.user;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class UserCrudApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> {
			if (Objects.isNull(System.getenv(entry.getKey()))
					&&  Objects.isNull(System.getProperty(entry.getKey()))) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		});
		SpringApplication.run(UserCrudApplication.class, args);
	}
}
