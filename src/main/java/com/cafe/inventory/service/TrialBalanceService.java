package com.cafe.inventory.service;

import com.cafe.inventory.dto.TrialBalanceDtos.*;
import com.cafe.inventory.entity.Account;
import com.cafe.inventory.entity.Voucher;
import com.cafe.inventory.repository.AccountRepository;
import com.cafe.inventory.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bảng cân đối số phát sinh (trial balance): per account, opening / period
 * debit-credit / closing balances derived from voucher debit & credit postings.
 */
@Service
@RequiredArgsConstructor
public class TrialBalanceService {

    private final AccountRepository accountRepository;
    private final VoucherRepository voucherRepository;

    @Transactional(readOnly = true)
    public TrialReport report(LocalDate from, LocalDate to) {
        Map<String, String> names = accountRepository.findAllByOrderByAccountCodeAsc().stream()
                .collect(Collectors.toMap(Account::getAccountCode, a -> a.getAccountName() == null ? "" : a.getAccountName(), (a, b) -> a));

        // acc[code] = [openDr, openCr, perDr, perCr]
        Map<String, BigDecimal[]> acc = new TreeMap<>();
        names.keySet().forEach(code -> acc.put(code, zero()));

        for (Voucher v : voucherRepository.findAll()) {
            LocalDate d = v.getVoucherDate();
            BigDecimal amt = v.getAmount() == null ? BigDecimal.ZERO : v.getAmount();
            post(acc, v.getDebitAccount(), amt, d, from, to, true);
            post(acc, v.getCreditAccount(), amt, d, from, to, false);
        }

        List<TrialRow> rows = new ArrayList<>();
        for (Map.Entry<String, BigDecimal[]> e : acc.entrySet()) {
            BigDecimal[] a = e.getValue();
            BigDecimal openDr = a[0], openCr = a[1], perDr = a[2], perCr = a[3];
            if (allZero(openDr, openCr, perDr, perCr)) continue;

            BigDecimal openNet = openDr.subtract(openCr);
            BigDecimal closeNet = openDr.add(perDr).subtract(openCr).subtract(perCr);

            rows.add(new TrialRow(
                    e.getKey(), names.getOrDefault(e.getKey(), ""),
                    pos(openNet), neg(openNet),
                    perDr, perCr,
                    pos(closeNet), neg(closeNet)));
        }
        return new TrialReport(from, to, rows);
    }

    private void post(Map<String, BigDecimal[]> acc, String code, BigDecimal amt,
                      LocalDate d, LocalDate from, LocalDate to, boolean debit) {
        if (code == null || code.isBlank() || d == null) return;
        BigDecimal[] a = acc.computeIfAbsent(code.trim(), k -> zero());
        if (d.isBefore(from)) {
            a[debit ? 0 : 1] = a[debit ? 0 : 1].add(amt);          // opening
        } else if (!d.isAfter(to)) {
            a[debit ? 2 : 3] = a[debit ? 2 : 3].add(amt);          // in period
        }
    }

    private BigDecimal[] zero() {
        return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
    }

    private boolean allZero(BigDecimal... xs) {
        for (BigDecimal x : xs) if (x.compareTo(BigDecimal.ZERO) != 0) return false;
        return true;
    }

    private BigDecimal pos(BigDecimal net) { return net.compareTo(BigDecimal.ZERO) > 0 ? net : BigDecimal.ZERO; }
    private BigDecimal neg(BigDecimal net) { return net.compareTo(BigDecimal.ZERO) < 0 ? net.negate() : BigDecimal.ZERO; }
}
