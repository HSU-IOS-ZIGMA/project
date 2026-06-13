package org.hansung.zigma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ZigmaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZigmaApplication.class, args);
	}

}
