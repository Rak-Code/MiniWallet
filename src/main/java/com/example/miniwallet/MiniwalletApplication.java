package com.example.miniwallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.example.miniwallet.repository")
@EntityScan("com.example.miniwallet.entity")
public class MiniwalletApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniwalletApplication.class, args);
	}

}
