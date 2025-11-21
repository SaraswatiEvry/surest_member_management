package com.assignment.surest_member_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SurestMemberManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(SurestMemberManagementApplication.class, args);
	}
}
