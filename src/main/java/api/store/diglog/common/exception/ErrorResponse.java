package api.store.diglog.common.exception;

import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.Getter;

@Getter
public class ErrorResponse {

	private final String code;
	private final String message;

	public ErrorResponse(String code, String message) {
		this.code = code;
		this.message = message;
	}

	// Validation 에러 -> ErrorResponse
	public ErrorResponse(MethodArgumentNotValidException e) {
		ObjectError objectError = e.getBindingResult().getAllErrors().getFirst();

		// VALIDATION_필드명 (EMAIL 등)
		String errorCode = "VALIDATION";
		if (objectError instanceof FieldError) {
			String fieldName = ((FieldError)objectError).getField().toUpperCase();
			errorCode = errorCode + "_" + fieldName;
		}

		this.code = errorCode;
		this.message = objectError.getDefaultMessage();
	}

	// 메소드 파라미터 유효성 검증 에러 -> ErrorResponse
	public ErrorResponse(ConstraintViolationException e) {
		ConstraintViolation<?> violation = e.getConstraintViolations().stream().findFirst().orElse(null);

		String paramName = "PARAMETER";
		String message = "Validation failed";

		if (violation != null) {
			String leaf = null;
			for (Path.Node node : violation.getPropertyPath()) {
				leaf = node.getName();
			}
			if (leaf != null && !leaf.isBlank()) {
				paramName = leaf.toUpperCase();
			}

			if (violation.getMessage() != null && !violation.getMessage().isBlank()) {
				message = violation.getMessage();
			}
		}

		this.code = "VALIDATION_" + paramName;
		this.message = message;
	}
}
