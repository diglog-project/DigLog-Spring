package api.store.diglog.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import api.store.diglog.model.constant.Platform;
import api.store.diglog.model.constant.Role;
import api.store.diglog.model.entity.Member;
import api.store.diglog.repository.MemberRepository;
import api.store.diglog.repository.SseEmitterRepository;
import api.store.diglog.supporter.RedisTestSupporter;

@SpringBootTest
@ActiveProfiles("test")
class SseEmitterServiceTest extends RedisTestSupporter {

	@Autowired
	private SseEmitterService sseEmitterService;

	@Autowired
	private SseEmitterRepository sseEmitterRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Member loginMember;

	private final List<Member> users = new ArrayList<>();

	private UUID testUserId;

	@BeforeEach
	void setUp() {
		Authentication auth = new UsernamePasswordAuthenticationToken(
			"loginMember@example.com",
			"password",
			List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);
		SecurityContextHolder.getContext().setAuthentication(auth);

		loginMember = createMember("loginMember");
		memberRepository.save(loginMember);
		testUserId = loginMember.getId();
	}

	@AfterEach
	void tearDown() {
		List<SseEmitter> emittersToCleanup = new ArrayList<>(sseEmitterRepository.findById(testUserId));
		emittersToCleanup.forEach(emitter -> {
			try {
				emitter.complete();
			} catch (Exception ignored) {
			}
			sseEmitterRepository.deleteBy(testUserId, emitter);
		});

		for (Member user : users) {
			List<SseEmitter> userEmitters = new ArrayList<>(sseEmitterRepository.findById(user.getId()));
			userEmitters.forEach(emitter -> {
				try {
					emitter.complete();
				} catch (Exception ignored) {
				}
				sseEmitterRepository.deleteBy(user.getId(), emitter);
			});
		}

		SecurityContextHolder.clearContext();
		users.clear();
		memberRepository.deleteAllInBatch();
	}

	@Test
	@DisplayName("사용자가 SSE 구독 시 정상적으로 연결되고 emitter가 저장된다")
	void subscribe_ShouldReturnSseEmitter() {
		// When
		SseEmitter emitter = sseEmitterService.subscribe();

		// Then
		List<SseEmitter> savedEmitters = sseEmitterRepository.findById(loginMember.getId());
		assertThat(savedEmitters).hasSize(1)
			.containsExactly(emitter);
	}

	@Test
	@DisplayName("동일 사용자가 여러 디바이스에서 구독하면 여러 개의 emitter가 생성된다")
	void subscribe_MultipleDevices_ShouldCreateMultipleEmitters() {
		// Given, When
		SseEmitter chromeEmitter = sseEmitterService.subscribe();
		SseEmitter mobileEmitter = sseEmitterService.subscribe();
		SseEmitter webEmitter = sseEmitterService.subscribe();
		SseEmitter iosEmitter = sseEmitterService.subscribe();

		// Then
		List<SseEmitter> savedEmitters = new ArrayList<>(sseEmitterRepository.findById(testUserId));
		assertThat(savedEmitters).hasSize(4)
			.containsExactlyInAnyOrder(chromeEmitter, mobileEmitter, webEmitter, iosEmitter);
	}

	@Test
	@DisplayName("메시지 전송 시 해당 사용자의 emitter에게 전달된다")
	void send() throws IOException {
		// Given
		SseEmitter mockSseEmitter = mock(SseEmitter.class);
		sseEmitterRepository.save(testUserId, mockSseEmitter);
		String testMessage = "testMessage";

		// When
		sseEmitterService.send(testUserId, testMessage);

		// Then
		verify(mockSseEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
	}

	@Test
	@DisplayName("메시지 전송 시 해당 사용자의 모든 emitter에게 전달된다")
	void send_ShouldSendMessageToAllUserEmitters() throws IOException {
		// Given
		int deviceCount = 5;
		List<SseEmitter> mockEmitters = new ArrayList<>();
		for (int i = 0; i < deviceCount; i++) {
			SseEmitter mockEmitter = mock(SseEmitter.class);
			sseEmitterRepository.save(testUserId, mockEmitter);
			mockEmitters.add(mockEmitter);
		}
		String testMessage = "testMessage";

		// When
		sseEmitterService.send(testUserId, testMessage);

		// Then
		for (SseEmitter mockEmitter : mockEmitters) {
			verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
		}
	}

	@Test
	@DisplayName("존재하지 않는 사용자 ID로 메시지 전송 시 아무 동작하지 않는다")
	void send_NonExistentUser_ShouldDoNothing() {
		// Given
		UUID nonExistentUserId = UUID.randomUUID();
		String testMessage = "Test message";

		// When, Then
		assertThatCode(() -> sseEmitterService.send(nonExistentUserId, testMessage))
			.doesNotThrowAnyException();
		assertThat(sseEmitterRepository.findById(nonExistentUserId)).isEmpty();
	}

	@Test
	@DisplayName("일부 emitter에서 에러 발생 시 해당 emitter만 제거된다")
	void send_PartialFailure_ShouldRemoveOnlyFailedEmitters() throws IOException {
		// Given
		SseEmitter workingEmitter = sseEmitterService.subscribe();
		SseEmitter faultyEmitter = mock(SseEmitter.class);
		sseEmitterRepository.save(testUserId, faultyEmitter);
		doThrow(new IOException("send failed"))
			.when(faultyEmitter)
			.send(any(SseEmitter.SseEventBuilder.class));

		// When
		sseEmitterService.send(testUserId, "test message");

		// Then
		List<SseEmitter> remainingEmitters = sseEmitterRepository.findById(testUserId);
		assertThat(remainingEmitters).hasSize(1)
			.doesNotContain(faultyEmitter)
			.containsExactly(workingEmitter);
	}

	@Test
	@DisplayName("여러 사용자가 동시에 구독해도 안전하게 처리된다")
	void subscribe_MultipleUsersConcurrently_ShouldHandleSafely() throws InterruptedException {
		// Given
		int userCount = 5;
		try (ExecutorService executor = Executors.newFixedThreadPool(userCount)) {
			CountDownLatch readyLatch = new CountDownLatch(userCount);
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch doneLatch = new CountDownLatch(userCount);

			for (int i = 0; i < userCount; i++) {
				Member user = createMember("username" + i);
				memberRepository.save(user);
				users.add(user);
			}

			// When - 각 사용자별로 별도 스레드에서 구독
			for (int i = 0; i < userCount; i++) {
				final int index = i;
				executor.submit(() -> {
					try {
						Authentication auth = new UsernamePasswordAuthenticationToken(
							users.get(index).getEmail(),
							"password",
							List.of(new SimpleGrantedAuthority("ROLE_USER"))
						);
						SecurityContextHolder.getContext().setAuthentication(auth);

						readyLatch.countDown();
						startLatch.await();

						sseEmitterService.subscribe();
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} finally {
						SecurityContextHolder.clearContext();
						doneLatch.countDown();
					}
				});
			}

			readyLatch.await();
			startLatch.countDown();
			doneLatch.await();
			executor.shutdown();
		}

		// Then
		for (Member user : users) {
			assertThat(sseEmitterRepository.findById(user.getId())).hasSize(1);
		}
	}

	@Test
	@DisplayName("동일 사용자가 여러 디바이스에서 동시 구독해도 안전하게 처리된다")
	void subscribe_SameUserMultipleDevices_ShouldHandleSafely() throws InterruptedException {
		// Given
		int deviceCount = 4;
		List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
		try (ExecutorService executor = Executors.newFixedThreadPool(deviceCount)) {
			CountDownLatch readyLatch = new CountDownLatch(deviceCount);
			CountDownLatch startLatch = new CountDownLatch(1);
			CountDownLatch doneLatch = new CountDownLatch(deviceCount);

			// When
			for (int i = 0; i < deviceCount; i++) {
				executor.submit(() -> {
					try {
						Authentication auth = new UsernamePasswordAuthenticationToken(
							"loginMember@example.com",
							"password",
							List.of(new SimpleGrantedAuthority("ROLE_USER"))
						);
						SecurityContextHolder.getContext().setAuthentication(auth);

						readyLatch.countDown();
						startLatch.await();

						SseEmitter emitter = sseEmitterService.subscribe();
						emitters.add(emitter);
					} catch (Exception e) {
						Thread.currentThread().interrupt();
						throw new RuntimeException(e);
					} finally {
						SecurityContextHolder.clearContext();
						doneLatch.countDown();
					}
				});
			}

			readyLatch.await();
			startLatch.countDown();
			doneLatch.await();
			executor.shutdown();
		}

		// Then
		assertThat(emitters).hasSize(deviceCount);
		assertThat(sseEmitterRepository.findById(testUserId)).hasSize(deviceCount)
			.containsExactlyInAnyOrderElementsOf(emitters);
	}

	private Member createMember(String userName) {
		return Member.builder()
			.email(userName + "@example.com")
			.username(userName)
			.password(userName + "Password")
			.roles(Set.of(Role.ROLE_USER))
			.platform(Platform.SERVER)
			.createdAt(LocalDateTime.of(2022, 2, 22, 12, 0))
			.updatedAt(LocalDateTime.of(2022, 3, 22, 12, 0))
			.isDeleted(false)
			.build();
	}
}
