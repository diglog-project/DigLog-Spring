package api.store.diglog.model.entity.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

	COMMENT("댓글"),
	SUBSCRIBE("구독"),
	ETC("기타")
	;

	private final String text;
}
