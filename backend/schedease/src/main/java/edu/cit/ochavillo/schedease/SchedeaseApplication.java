package edu.cit.ochavillo.schedease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SchedeaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(SchedeaseApplication.class, args);
	}

}
