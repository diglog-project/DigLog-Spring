package api.store.diglog.model.entity.notification;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

	COMMENT_CREATION("댓글 생성"),
	POST_CREATION("게시글 생성"),
	INVALID("허용되지 않는 타입");

	private final String description;

	public static NotificationType from(String type) {
		return Arrays.stream(NotificationType.values())
			.filter(notificationType -> notificationType.name().equals(type))
			.findFirst()
			.orElse(INVALID);
	}
}
