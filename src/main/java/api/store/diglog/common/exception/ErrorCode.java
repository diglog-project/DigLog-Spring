package api.store.diglog.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

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

    // S3
    S3_WRONG_FILE(BAD_REQUEST, "이미지 Byte를 얻는데 실패했습니다. 이미지를 다시 확인해주세요."),
    S3_IMAGE_UPLOAD_FAILED(INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    S3_IMAGE_DELETE_FAILED(INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),

    // Post
    POST_NOT_FOUND(BAD_REQUEST, "해당 게시글이 없습니다."),
    POST_INVALID_SORT(BAD_REQUEST, "정렬 조건이 올바르지 않습니다. (정렬 옵션 : createdAt, updatedAt)"),
    POST_DELETE_FAILED(FORBIDDEN, "게시글 삭제가 완료되지 않았습니다."),
    POST_NO_PERMISSION(FORBIDDEN, "게시글 수정 권한이 없습니다."),

    ;

    private final HttpStatus status;
    private final String message;

    public String getErrorCode() {
        return this.name();
    }
}
