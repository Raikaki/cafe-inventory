package com.cafe.inventory.service;

import com.cafe.inventory.dto.BatchDtos.BatchRequest;
import com.cafe.inventory.dto.GoodsReceiptDtos.*;
import com.cafe.inventory.dto.VoucherDtos.VoucherRequest;
import com.cafe.inventory.entity.GoodsReceipt;
import com.cafe.inventory.entity.GoodsReceiptDetail;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.repository.GoodsReceiptRepository;
import com.cafe.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final InventoryService inventoryService;
    private final VoucherService voucherService;
    private final SupplierRepository supplierRepository;
    private final PeriodLockService periodLockService;
    private final BatchService batchService;

    @Transactional
    public ReceiptResponse create(ReceiptRequest request, String user) {
        if (request.lines() == null || request.lines().isEmpty()) {
            throw new BusinessException("Goods receipt must contain at least one line");
        }
        periodLockService.checkNotLocked(request.receiptDate());

        GoodsReceipt gr = new GoodsReceipt();
        gr.setReceiptNo(nextReceiptNo());
        gr.setReceiptDate(request.receiptDate());
        gr.setSupplierId(request.supplierId());
        gr.setNote(request.note());
        gr.setCreatedBy(user);

        BigDecimal total = BigDecimal.ZERO;
        for (ReceiptLine line : request.lines()) {
            BigDecimal amount = line.amount();
            // unit price derived from amount / quantity
            BigDecimal unitPrice = line.quantity().compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(line.quantity(), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            GoodsReceiptDetail d = new GoodsReceiptDetail();
            d.setMaterialId(line.materialId());
            d.setQuantity(line.quantity());
            d.setUnitPrice(unitPrice);
            d.setAmount(amount);
            gr.addDetail(d);
            total = total.add(amount);
        }
        gr.setTotalAmount(total);
        goodsReceiptRepository.save(gr);

        // Apply to inventory: increase qty + record line unit cost on the ledger.
        // Average cost is NOT updated here — it is computed at monthly close.
        // A batch (lô + HSD) is auto-created when the line has an expiry/batch no.
        int idx = 1;
        for (ReceiptLine line : request.lines()) {
            BigDecimal unitPrice = line.quantity().compareTo(BigDecimal.ZERO) > 0
                    ? line.amount().divide(line.quantity(), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            inventoryService.receive(line.materialId(), line.quantity(), unitPrice, gr.getReceiptNo(), user);

            boolean wantBatch = line.expiryDate() != null || (line.batchNo() != null && !line.batchNo().isBlank());
            if (wantBatch) {
                String bno = (line.batchNo() == null || line.batchNo().isBlank())
                        ? gr.getReceiptNo() + "/" + idx : line.batchNo();
                batchService.create(new BatchRequest(line.materialId(), bno, gr.getSupplierId(),
                        gr.getReceiptDate(), line.expiryDate(), line.quantity(),
                        "Từ phiếu nhập " + gr.getReceiptNo()), user);
            }
            idx++;
        }

        // auto-generate a goods-receipt voucher (Phiếu nhập kho)
        try {
            String supplierName = gr.getSupplierId() == null ? null
                    : supplierRepository.findById(gr.getSupplierId()).map(s -> s.getSupplierName()).orElse(null);
            voucherService.create(new VoucherRequest(
                    "PHIEU_NHAP_KHO", gr.getReceiptDate(), null, null, supplierName, null,
                    "Nhập kho NVL - phiếu " + gr.getReceiptNo(), null, null, total,
                    null, "Tự động sinh từ phiếu nhập " + gr.getReceiptNo(), null, "152", "331"), user);
        } catch (Exception ex) {
            log.warn("Auto voucher for receipt {} failed: {}", gr.getReceiptNo(), ex.getMessage());
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
