package com.cafe.inventory.controller.api;

import com.cafe.inventory.entity.StockPeriodSummary;
import com.cafe.inventory.service.StockSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Reports")
@RestController
@RequestMapping("/api/reports/stock-summary")
@RequiredArgsConstructor
public class StockSummaryApiController {

    private final StockSummaryService stockSummaryService;

    @Operation(summary = "Read the stored monthly stock summary")
    @GetMapping
    public List<StockPeriodSummary> get(@RequestParam int year, @RequestParam int month) {
        return stockSummaryService.get(year, month);
    }

    @Operation(summary = "Aggregate the month from the ledger and store it into the summary table")
    @PostMapping("/aggregate")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<StockPeriodSummary> aggregate(@RequestParam int year, @RequestParam int month, Principal principal) {
        return stockSummaryService.aggregate(year, month, principal == null ? "system" : principal.getName());
    }
}
