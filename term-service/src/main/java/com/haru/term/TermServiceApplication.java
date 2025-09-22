package com.haru.term;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class TermServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TermServiceApplication.class, args);
	}

}
