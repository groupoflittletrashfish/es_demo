package com.noname;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class EsDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(EsDemoApplication.class, args);
	}

}
