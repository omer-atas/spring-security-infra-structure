package com.spring.securitytutorial;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5433}/${POSTGRES_DB_TEST:security_test}",
        "app.security.rate-limit-filter.enabled=false"
})
class SecurityTutorialApplicationTests {

	@Test
	void contextLoads() {
	}

}
