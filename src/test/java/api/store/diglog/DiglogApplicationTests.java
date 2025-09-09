package api.store.diglog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import api.store.diglog.supporter.RedisTestSupporter;

@ActiveProfiles("test")
@SpringBootTest
class DiglogApplicationTests extends RedisTestSupporter {

	@Test
	void contextLoads() {
	}

}
