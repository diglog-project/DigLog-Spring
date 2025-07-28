package api.store.diglog.common.exception;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	// Member
	LOGIN_FAILED(BAD_REQUEST, "이메일 또는 비밀번호가 일치하지 않습니다."),
	SIGNUP_MEMBER_EXISTS(CONFLICT, "이미 가입된 회원입니다."),
	SIGNUP_PLATFORM_DUPLICATED(CONFLICT, "다른 방법으로 회원가입 되어있습니다. 다른 로그인 방법으로 시도해주세요."),
	SIGNUP_MAIL_SEND_FAILED(INTERNAL_SERVER_ERROR, "메일 발송 중 오류가 발생하였습니다."),
	SIGNUP_CODE_NOT_EXISTS(BAD_REQUEST, "해당 이메일에 대한 인증 코드가 없습니다."),
	SIGNUP_CODE_NOT_VERIFIED(BAD_REQUEST, "인증되지 않은 코드입니다."),
	SIGNUP_CODE_NOT_MATCHED(BAD_REQUEST, "인증 코드가 일치하지 않습니다."),
	SIGNUP_CODE_EXPIRED(BAD_REQUEST, "코드 유효기간이 만료되었습니다."),
	MEMBER_EMAIL_NOT_FOUND(BAD_REQUEST, "해당 이메일을 가진 회원이 없습니다."),
	MEMBER_USERNAME_NOT_FOUND(BAD_REQUEST, "해당 이름을 가진 회원이 없습니다."),
	MEMBER_ID_NOT_FOUND(BAD_REQUEST, "해당 ID를 가진 회원이 없습니다."),

	// S3
	S3_WRONG_FILE(BAD_REQUEST, "이미지 Byte를 얻는데 실패했습니다. 이미지를 다시 확인해주세요."),
	S3_IMAGE_UPLOAD_FAILED(INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
	S3_IMAGE_DELETE_FAILED(INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),

	// Post
	POST_NOT_FOUND(BAD_REQUEST, "해당 게시글이 없습니다."),
	POST_INVALID_SORT(BAD_REQUEST, "정렬 조건이 올바르지 않습니다. (정렬 옵션 : createdAt, updatedAt)"),
	POST_DELETE_FAILED(FORBIDDEN, "게시글 삭제가 완료되지 않았습니다."),
	POST_NO_PERMISSION(FORBIDDEN, "게시글 수정 권한이 없습니다."),
	POST_INVALID_SEARCH_OPTION(BAD_REQUEST, "올바르지 않은 검색 옵션입니다."),

	// Folder
	FOLDER_OVER_FLOW_DEPTH(BAD_REQUEST, "하위 폴더의 깊이는 %d까지 허용됩니다."),
	FOLDER_OVER_FLOW_ORDER_INDEX(BAD_REQUEST, "최대 폴더 순서(%d번)를 초과했습니다."),
	FOLDER_DUPLICATION_TITLE(BAD_REQUEST, "중복된 폴더 이름이 존재합니다."),
	FOLDER_OVER_FLOW_SIZE(BAD_REQUEST, "최대 폴더의 개수(%d개)를 초과했습니다."),
	FOLDER_DUPLICATION_ORDER_INDEX(BAD_REQUEST, "중복된 폴더 순서가 존재합니다."),
	FOLDER_OVER_FLOW_TITLE_LENGTH(BAD_REQUEST, "폴더 제목은 %d자 까지만 허용됩니다."),
	FOLDER_OWNER_MISMATCH(BAD_REQUEST, "멤버가 갖고 있는 폴더 중 해당 폴더를 찾을 수 없습니다."),
	FOLDER_EXIST_CHILD_FOLDER(BAD_REQUEST, "\"%s\" 폴더 하위에 \"%s\" 폴더가 존재합니다. 먼저 삭제해주세요"),
	FOLDER_CONTAIN_POST(BAD_REQUEST, "\"%s\" 폴더 하위에 \"%s\" 게시글이 존재합니다. 먼저 삭제해주세요"),
	FOLDER_NOT_MATCH_MEMBER(BAD_REQUEST, "로그인 중인 회원 정보와 폴더 회원 정보가 일치하지 않습니다."),

	// Comment
	COMMENT_PARENT_ID_NOT_FOUND(BAD_REQUEST, "대댓글을 달기 위해 지정한 댓글을 찾을 수 없습니다."),
	COMMENT_MAX_DEPTH_EXCEEDED(BAD_REQUEST, "대댓글의 최대 깊이를 초과했습니다."),
	COMMENT_IS_DELETED_NO_CHANGE(BAD_REQUEST, "댓글 삭제 권한이 없거나, 삭제가 완료되지 않았습니다."),
	COMMENT_NOT_FOUND(BAD_REQUEST, "해당 댓글을 찾을 수 없습니다."),
	COMMENT_UPDATE_NO_AUTHORITY(BAD_REQUEST, "해당 댓글을 수정할 수 있는 권한이 없습니다."),

	// Redis
	REDIS_UNAVAILABLE(SERVICE_UNAVAILABLE, "서비스 연결에 실패했습니다. 잠시 후 다시 시도해주세요."),
	INVALID_REDIS_VIEW_COUNT_VALUE(INTERNAL_SERVER_ERROR, "조회수 처리에 실패했습니다. 잠시 후 다시 시도해주세요."),
	REDIS_VIEW_COUNT_VALUE_MISSING(INTERNAL_SERVER_ERROR, "조회수 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요."),

	// Subscription
	SUBSCRIPTION_NOT_FOUND(BAD_REQUEST, "구독 내역이 존재하지 않습니다."),
	SUBSCRIPTION_INACTIVE_AUTHOR(FORBIDDEN, "탈퇴한 회원은 구독할 수 없습니다."),
	SUBSCRIPTION_SELF_SUBSCRIPTION(BAD_REQUEST, "자기 자신은 구독할 수 없습니다."),
	SUBSCRIPTION_ALREADY_SUBSCRIPTION(CONFLICT, "이미 해당 작성자를 구독하고 있습니다."),
	SUBSCRIPTION_EXCEED_SUBSCRIPTION_COUNT(BAD_REQUEST, "구독 가능한 최대 수(%d명)를 초과했습니다."),
	SUBSCRIPTION_MISMATCH_CURRENT_MEMBER_SUBSCRIBER(FORBIDDEN, "구독 수정 권한이 없습니다.");

	private final HttpStatus status;
	private final String message;

	public String getErrorCode() {
		return this.name();
	}
}
