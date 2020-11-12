package com.agile.image;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;

@SpringBootApplication
@IntegrationComponentScan
public class ImageApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageApplication.class, args);
	}

}
