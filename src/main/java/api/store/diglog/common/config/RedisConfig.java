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
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import api.store.diglog.service.notification.NotificationSubscriber;

@Configuration
public class RedisConfig {

	private static final String NOTIFICATION_CHANNEL = "notification-channel";
	private static final String ADDRESS_PREFIX = "redis://";
	private static final String ADDRESS_DELIMITER = ":";

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
	public RedisMessageListenerContainer container(
		RedisConnectionFactory connectionFactory,
		NotificationSubscriber notificationSubscriber,
		ChannelTopic notificationTopic
	) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.addMessageListener(notificationSubscriber, notificationTopic);
		return container;
	}

	@Bean
	public RedissonClient redissonClient() {
		Config config = new Config();
		config.useSingleServer()
			.setAddress(ADDRESS_PREFIX + host + ADDRESS_DELIMITER + port);
		return Redisson.create(config);
	}

	@Bean
	public ChannelTopic notificationTopic() {
		return new ChannelTopic(NOTIFICATION_CHANNEL);
	}

}
