package api.store.diglog.supporter;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class RedisTestSupporter {

	private static final GenericContainer<?> redisContainer;

	static {
		redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2"))
			.withExposedPorts(6379)
			.withReuse(true);

		redisContainer.start();
	}

	@DynamicPropertySource
	static void overrideRedisProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.redis.host", redisContainer::getHost);
		registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
	}
}
