package com.repifuzz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.repifuzz")
public class RepifuzzAssignmnetApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepifuzzAssignmnetApplication.class, args);
	}

}
