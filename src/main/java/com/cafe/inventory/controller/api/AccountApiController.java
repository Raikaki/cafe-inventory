package com.cafe.inventory.controller.api;

import com.cafe.inventory.entity.Account;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.exception.ResourceNotFoundException;
import com.cafe.inventory.repository.AccountRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Accounts")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountApiController {

    private final AccountRepository accountRepository;

    @GetMapping
    public List<Account> list() {
        return accountRepository.findAllByOrderByAccountCodeAsc();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Account create(@RequestBody Account body) {
        if (body.getAccountCode() == null || body.getAccountCode().isBlank())
            throw new BusinessException("Số hiệu tài khoản không được trống");
        if (accountRepository.existsByAccountCode(body.getAccountCode().trim()))
            throw new BusinessException("Tài khoản đã tồn tại: " + body.getAccountCode());
        Account a = new Account();
        a.setAccountCode(body.getAccountCode().trim());
        a.setAccountName(body.getAccountName());
        a.setActiveFlag(true);
        return accountRepository.save(a);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Account update(@PathVariable Long id, @RequestBody Account body) {
        Account a = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + id));
        a.setAccountCode(body.getAccountCode());
        a.setAccountName(body.getAccountName());
        if (body.getActiveFlag() != null) a.setActiveFlag(body.getActiveFlag());
        return accountRepository.save(a);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
