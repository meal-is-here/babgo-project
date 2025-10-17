package com.babgo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@EnableJpaAuditing
@EnableFeignClients
@EnableRetry
@SpringBootApplication
public class BabgoApplication {

	public static void main(String[] args) {
		SpringApplication.run(BabgoApplication.class, args);
	}

}