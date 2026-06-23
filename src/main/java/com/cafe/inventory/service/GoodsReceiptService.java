package com.cafe.inventory.service;

import com.cafe.inventory.dto.GoodsReceiptDtos.*;
import com.cafe.inventory.entity.GoodsReceipt;
import com.cafe.inventory.entity.GoodsReceiptDetail;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.repository.GoodsReceiptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final InventoryService inventoryService;

    @Transactional
    public ReceiptResponse create(ReceiptRequest request, String user) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new BusinessException("Goods receipt must contain at least one line");
        }

        GoodsReceipt gr = new GoodsReceipt();
        gr.setReceiptNo(nextReceiptNo());
        gr.setReceiptDate(request.receiptDate());
        gr.setSupplierId(request.supplierId());
        gr.setNote(request.note());
        gr.setCreatedBy(user);

        BigDecimal total = BigDecimal.ZERO;
        for (ReceiptLine line : request.lines()) {
            BigDecimal amount = line.quantity().multiply(line.unitPrice());
            GoodsReceiptDetail d = new GoodsReceiptDetail();
            d.setMaterialId(line.materialId());
            d.setQuantity(line.quantity());
            d.setUnitPrice(line.unitPrice());
            d.setAmount(amount);
            gr.addDetail(d);
            total = total.add(amount);
        }
        gr.setTotalAmount(total);
        goodsReceiptRepository.save(gr);

        // Apply to inventory (increase qty + weighted average cost)
        for (ReceiptLine line : request.lines()) {
            inventoryService.receive(line.materialId(), line.quantity(), line.unitPrice(),
                    gr.getReceiptNo(), user);
        }

        log.info("Goods receipt {} created by {} with total {}", gr.getReceiptNo(), user, total);
        return new ReceiptResponse(gr.getId(), gr.getReceiptNo(), gr.getReceiptDate(), gr.getTotalAmount());
    }

    private String nextReceiptNo() {
        String prefix = "GR" + Year.now().getValue();
        long seq = goodsReceiptRepository.countByReceiptNoStartingWith(prefix) + 1;
        return String.format("%s%06d", prefix, seq);
    }
}
