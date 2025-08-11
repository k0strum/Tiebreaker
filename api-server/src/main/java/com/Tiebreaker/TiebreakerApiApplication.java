package com.Tiebreaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class TiebreakerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TiebreakerApiApplication.class, args);
	}

}
