package com.prince.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Needs the PostgreSQL + pgvector database to be running
 * (docker compose up -d db). Seeding is disabled so Ollama is not required.
 */
@SpringBootTest(properties = "app.seed.enabled=false")
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
