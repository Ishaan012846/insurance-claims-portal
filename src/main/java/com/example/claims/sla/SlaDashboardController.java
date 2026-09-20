package com.example.claims.sla;

import com.example.claims.sla.dto.SlaMetricsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class SlaDashboardController {

    private final SlaService slaService;

    public SlaDashboardController(SlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping("/sla")
    @PreAuthorize("hasAnyRole('MANAGER', 'HANDLER')")
    public ResponseEntity<SlaMetricsDto> getSlaMetrics() {
        SlaMetricsDto metrics = slaService.getSlaMetrics();
        return ResponseEntity.ok(metrics);
    }
}
