package api.store.diglog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import api.store.diglog.model.dto.healthcheck.HealthCheckResponse;

@RestController
public class HealthController {

	@GetMapping({"/api/health-check", "/health-check"})
	public ResponseEntity<HealthCheckResponse> healthCheck() {
		return ResponseEntity.ok().body(HealthCheckResponse.ok());
	}

}
