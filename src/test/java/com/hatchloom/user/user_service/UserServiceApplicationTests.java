package com.hatchloom.user.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@ActiveProfiles("test")
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
		// Verifies that the application context can be loaded with test configuration
	}

}
