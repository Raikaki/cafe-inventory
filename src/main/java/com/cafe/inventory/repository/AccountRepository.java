package com.cafe.inventory.repository;

import com.cafe.inventory.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountCode(String accountCode);
    List<Account> findAllByOrderByAccountCodeAsc();
}
