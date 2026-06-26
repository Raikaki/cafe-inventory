package com.cafe.inventory.service;

import com.cafe.inventory.dto.StockCountDtos.*;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.entity.StockCount;
import com.cafe.inventory.entity.StockCountDetail;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.repository.MaterialRepository;
import com.cafe.inventory.repository.StockCountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Physical stock count (kiểm kê): compares actual counted quantities with the
 * system quantity and posts an ADJUSTMENT for each difference.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockCountService {

    private final StockCountRepository stockCountRepository;
    private final MaterialRepository materialRepository;
    private final InventoryService inventoryService;
    private final PeriodLockService periodLockService;

    @Transactional
    public CountResponse create(CountRequest request, String user) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException("Phiếu kiểm kê phải có ít nhất 1 dòng");
        }
        periodLockService.checkNotLocked(request.countDate());

        StockCount count = new StockCount();
        count.setCountNo(nextNo());
        count.setCountDate(request.countDate());
        count.setNote(request.note());
        count.setCreatedBy(user);

        List<CountResultRow> rows = new ArrayList<>();
        List<BigDecimal[]> toAdjust = new ArrayList<>(); // [materialId, diff]
        List<Long> adjustIds = new ArrayList<>();

        for (CountItem item : request.items()) {
            Material m = materialRepository.findById(item.materialId()).orElse(null);
            if (m == null) continue;
            BigDecimal system = m.getCurrentQty();
            BigDecimal actual = item.actualQty();
            BigDecimal diff = actual.subtract(system);

            StockCountDetail d = new StockCountDetail();
            d.setMaterialId(m.getId());
            d.setSystemQty(system);
            d.setActualQty(actual);
            d.setDiffQty(diff);
            count.addDetail(d);

            rows.add(new CountResultRow(m.getMaterialCode(), m.getMaterialName(), m.getUnit(), system, actual, diff));
            if (diff.compareTo(BigDecimal.ZERO) != 0) {
                adjustIds.add(m.getId());
                toAdjust.add(new BigDecimal[]{BigDecimal.valueOf(m.getId()), diff});
            }
        }
        stockCountRepository.save(count);

        int adjusted = 0;
        for (int i = 0; i < adjustIds.size(); i++) {
            inventoryService.adjust(adjustIds.get(i), toAdjust.get(i)[1], count.getCountNo(), user, "Kiểm kê " + count.getCountNo());
            adjusted++;
        }

        log.info("Stock count {} by {}: {} lines, {} adjustments", count.getCountNo(), user, rows.size(), adjusted);
        return new CountResponse(count.getCountNo(), count.getCountDate(), adjusted, rows);
    }

    private String nextNo() {
        String prefix = "KK" + Year.now().getValue();
        long seq = stockCountRepository.countByCountNoStartingWith(prefix) + 1;
        return String.format("%s%05d", prefix, seq);
    }
}
