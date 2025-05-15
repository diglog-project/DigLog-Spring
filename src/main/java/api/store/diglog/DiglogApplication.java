package api.store.diglog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DiglogApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiglogApplication.class, args);
	}

}
