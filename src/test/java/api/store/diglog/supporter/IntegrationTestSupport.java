package api.store.diglog.supporter;

import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;

import api.store.diglog.common.auth.JWTUtil;
import api.store.diglog.repository.CommentRepository;
import api.store.diglog.repository.EmailVerificationRepository;
import api.store.diglog.repository.FolderRepository;
import api.store.diglog.repository.MemberRepository;
import api.store.diglog.repository.NotificationRepository;
import api.store.diglog.repository.PostRepository;
import api.store.diglog.repository.PostViewBatchRepository;
import api.store.diglog.repository.RefreshRepository;
import api.store.diglog.repository.SseEmitterRepository;
import api.store.diglog.repository.SubscriptionRepository;
import api.store.diglog.repository.TagRepository;
import api.store.diglog.service.FolderService;
import api.store.diglog.service.SseEmitterService;
import api.store.diglog.service.SubscriptionService;
import api.store.diglog.service.notification.CommentCreationNotificationStrategy;
import api.store.diglog.service.notification.NotificationPublisher;
import api.store.diglog.service.notification.NotificationStrategyFactory;
import api.store.diglog.service.notification.NotificationSubscriber;
import api.store.diglog.service.notification.NotificationTransactionService;
import api.store.diglog.service.notification.PostCreationNotificationStrategy;
import api.store.diglog.service.notification.UnsupportedNotificationStrategy;
import api.store.diglog.service.post.PostAsyncWorker;
import api.store.diglog.service.post.PostService;
import api.store.diglog.service.post.RedisPostViewLoader;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
public abstract class IntegrationTestSupport {

	protected static final GenericContainer<?> redisContainer;
	protected static final ObjectMapper objectMapper = new ObjectMapper();

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

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected EntityManager entityManager;

	@Autowired
	protected JWTUtil jwtUtil;

	@Autowired
	protected PasswordEncoder passwordEncoder;

	@Autowired
	protected StringRedisTemplate redisTemplate;

	@Mock
	protected Appender<ILoggingEvent> mockAppender;

	@Autowired
	protected MemberRepository memberRepository;

	@Autowired
	protected FolderRepository folderRepository;

	@Autowired
	protected PostRepository postRepository;

	@MockitoSpyBean
	protected PostRepository spyPostRepository;

	@Autowired
	protected CommentRepository commentRepository;

	@Autowired
	protected EmailVerificationRepository emailVerificationRepository;

	@Autowired
	protected RefreshRepository refreshRepository;

	@Autowired
	protected NotificationRepository notificationRepository;

	@Autowired
	protected SubscriptionRepository subscriptionRepository;

	@Autowired
	protected TagRepository tagRepository;

	@MockitoSpyBean
	protected PostViewBatchRepository postViewBatchRepository;

	@Autowired
	protected SseEmitterRepository sseEmitterRepository;

	@Autowired
	protected PostService postService;

	@Autowired
	protected SubscriptionService subscriptionService;

	@Autowired
	protected FolderService folderService;

	@MockitoSpyBean
	protected SseEmitterService spySseEmitterService;

	@Autowired
	protected SseEmitterService sseEmitterService;

	@Autowired
	protected NotificationTransactionService notificationTransactionService;

	@Autowired
	protected ChannelTopic notificationChannelTopic;

	@Autowired
	protected PostAsyncWorker postAsyncWorker;

	@Autowired
	protected RedisPostViewLoader redisPostViewLoader;

	@Autowired
	protected NotificationPublisher notificationPublisher;

	@MockitoSpyBean
	protected NotificationSubscriber notificationSubscriber;

	@Autowired
	protected NotificationStrategyFactory notificationStrategyFactory;

	@Autowired
	protected CommentCreationNotificationStrategy commentCreationNotificationStrategy;

	@Autowired
	protected PostCreationNotificationStrategy postCreationNotificationStrategy;

	@Autowired
	protected UnsupportedNotificationStrategy unsupportedNotificationStrategy;

}
