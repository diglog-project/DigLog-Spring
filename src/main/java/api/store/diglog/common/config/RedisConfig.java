package api.store.diglog.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import api.store.diglog.service.notification.NotificationSubscriber;

@Configuration
public class RedisConfig {

	@Value("${spring.redis.host}")
	private String host;

	@Value("${spring.redis.port}")
	private int port;

	@Bean
	@Primary
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
		return new LettuceConnectionFactory(config);
	}

	@Bean
	@Primary
	public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<?, ?> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);

		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new StringRedisSerializer());

		template.setEnableTransactionSupport(false);
		template.afterPropertiesSet();

		return template;
	}

	@Bean
	public RedisMessageListenerContainer container(
		RedisConnectionFactory connectionFactory,
		NotificationSubscriber notificationSubscriber
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(notificationSubscriber, new ChannelTopic("notification-channel"));
		return container;
	}

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		config.useSingleServer()
			.setAddress("redis://" + host + ":" + port);
		return Redisson.create(config);
	}

}
