package com.cafe.inventory.controller.api;

import com.cafe.inventory.dto.ForecastDtos.ForecastResponse;
import com.cafe.inventory.service.ForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Forecast / AI")
@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class ForecastApiController {

    private final ForecastService forecastService;

    @Operation(summary = "Inventory forecast with days-to-stockout, reorder suggestion and AI advice")
    @GetMapping
    public ForecastResponse forecast(
            @RequestParam(defaultValue = "30") int lookbackDays,
            @RequestParam(defaultValue = "14") int horizonDays,
            @RequestParam(defaultValue = "true") boolean ai) {
        int lb = Math.max(1, Math.min(lookbackDays, 365));
        int hz = Math.max(1, Math.min(horizonDays, 180));
        return forecastService.forecast(lb, hz, ai);
    }
}
