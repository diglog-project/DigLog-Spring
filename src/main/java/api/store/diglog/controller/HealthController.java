package api.store.diglog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	@GetMapping({"/api/health-check", "/health-check"})
	public ResponseEntity<String> healthCheck() {
		return ResponseEntity.ok("UP");
	}

}
