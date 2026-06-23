package com.cafe.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public class ForecastDtos {

    public record ForecastItem(
            Long materialId,
            String materialCode,
            String materialName,
            String unit,
            BigDecimal currentQty,
            BigDecimal minimumQty,
            BigDecimal avgDailyUsage,
            Double daysToStockout,      // null = no consumption / effectively infinite
            BigDecimal projectedUsage,  // over the horizon window
            BigDecimal recommendedReorderQty,
            String status               // CRITICAL / WARNING / OK / IDLE
    ) {}

    public record ForecastResponse(
            int horizonDays,
            int lookbackDays,
            List<ForecastItem> items,
            String aiAdvice,
            boolean aiEnabled
    ) {}
}
