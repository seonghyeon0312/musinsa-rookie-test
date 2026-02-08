package com.musinsa.course.controller;

import com.musinsa.course.service.DataInitializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final DataInitializer dataInitializer;

    public HealthController(DataInitializer dataInitializer) {
        this.dataInitializer = dataInitializer;
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        if (!dataInitializer.isReady()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("초기 데이터 생성 중...");
        }
        return ResponseEntity.ok("OK");
    }
}
