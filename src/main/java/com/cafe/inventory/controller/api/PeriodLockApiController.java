package com.cafe.inventory.controller.api;

import com.cafe.inventory.entity.PeriodLock;
import com.cafe.inventory.service.PeriodLockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Period Lock")
@RestController
@RequestMapping("/api/period-locks")
@RequiredArgsConstructor
public class PeriodLockApiController {

    private final PeriodLockService periodLockService;

    @GetMapping
    public List<PeriodLock> list() {
        return periodLockService.list();
    }

    @Operation(summary = "Lock a period (year, month)")
    @PostMapping("/lock")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> lock(@RequestParam int year, @RequestParam int month, Principal principal) {
        periodLockService.lock(year, month, principal == null ? "system" : principal.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Unlock a period (year, month)")
    @PostMapping("/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> unlock(@RequestParam int year, @RequestParam int month) {
        periodLockService.unlock(year, month);
        return ResponseEntity.ok().build();
    }
}
