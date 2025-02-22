package api.store.diglog.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(final ErrorCode errorCode, final Object... args) {
        errorCode.formatMessage(args);
        this.errorCode = errorCode;
    }
}
