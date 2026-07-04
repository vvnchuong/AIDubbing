package com.tool.aidubbing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AidubbingApplication {

	public static void main(String[] args) {
		SpringApplication.run(AidubbingApplication.class, args);
	}

}
