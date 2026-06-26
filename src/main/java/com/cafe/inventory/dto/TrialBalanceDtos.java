package com.cafe.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TrialBalanceDtos {

    public record TrialRow(
            String accountCode,
            String accountName,
            BigDecimal openingDebit,
            BigDecimal openingCredit,
            BigDecimal periodDebit,
            BigDecimal periodCredit,
            BigDecimal closingDebit,
            BigDecimal closingCredit
    ) {}

    public record TrialReport(
            LocalDate fromDate,
            LocalDate toDate,
            List<TrialRow> rows
    ) {}
}
