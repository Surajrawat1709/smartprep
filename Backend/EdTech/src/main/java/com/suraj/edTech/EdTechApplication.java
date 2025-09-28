package com.suraj.edTech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = { "com.edtech", "com.suraj.edTech" })
@EnableJpaRepositories(basePackages = "com.edtech.repository")
@EntityScan(basePackages = "com.edtech.entity")
public class EdTechApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdTechApplication.class, args);
	}

}
