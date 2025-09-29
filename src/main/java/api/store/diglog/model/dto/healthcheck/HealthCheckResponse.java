package api.store.diglog.model.dto.healthcheck;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthCheckResponse {

	private static final String STATUS = "UP";

	private String status;

	public static HealthCheckResponse ok() {
		return new HealthCheckResponse(STATUS);
	}

}
