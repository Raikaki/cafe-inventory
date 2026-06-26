package com.cafe.inventory.service;

import com.cafe.inventory.dto.BatchDtos.*;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.entity.MaterialBatch;
import com.cafe.inventory.entity.Supplier;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.exception.ResourceNotFoundException;
import com.cafe.inventory.repository.MaterialBatchRepository;
import com.cafe.inventory.repository.MaterialRepository;
import com.cafe.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Batch + expiry tracking (lô + hạn sử dụng). Lists batches in FEFO order with
 * expiry status; disposing an expired/used batch reduces its remaining quantity
 * and posts an inventory adjustment so stock stays consistent.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final MaterialBatchRepository batchRepository;
    private final MaterialRepository materialRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryService inventoryService;

    private static final int EXPIRING_DAYS = 7;

    @Transactional
    public MaterialBatch create(BatchRequest req, String user) {
        materialRepository.findById(req.materialId())
                .orElseThrow(() -> new ResourceNotFoundException("Material not found: " + req.materialId()));
        MaterialBatch b = new MaterialBatch();
        b.setMaterialId(req.materialId());
        b.setBatchNo(req.batchNo());
        b.setSupplierId(req.supplierId());
        b.setReceivedDate(req.receivedDate() == null ? LocalDate.now() : req.receivedDate());
        b.setExpiryDate(req.expiryDate());
        b.setReceivedQty(req.receivedQty());
        b.setRemainingQty(req.receivedQty());
        b.setNote(req.note());
        b.setCreatedBy(user);
        return batchRepository.save(b);
    }

    @Transactional(readOnly = true)
    public List<BatchView> list() {
        Map<Long, Material> materials = materialRepository.findAll().stream()
                .collect(Collectors.toMap(Material::getId, m -> m));
        Map<Long, String> suppliers = supplierRepository.findAll().stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getSupplierName, (a, b) -> a));
        LocalDate today = LocalDate.now();

        return batchRepository.findActiveFefo().stream().map(b -> {
            Material m = materials.get(b.getMaterialId());
            Long days = b.getExpiryDate() == null ? null : ChronoUnit.DAYS.between(today, b.getExpiryDate());
            String status;
            if (days == null) status = "NO_EXPIRY";
            else if (days < 0) status = "EXPIRED";
            else if (days <= EXPIRING_DAYS) status = "EXPIRING";
            else status = "OK";
            return new BatchView(
                    b.getId(), b.getMaterialId(),
                    m == null ? "?" : m.getMaterialCode(), m == null ? "?" : m.getMaterialName(),
                    m == null ? "" : m.getUnit(),
                    b.getBatchNo(),
                    b.getSupplierId() == null ? null : suppliers.get(b.getSupplierId()),
                    b.getReceivedDate(), b.getExpiryDate(), b.getReceivedQty(), b.getRemainingQty(),
                    days, status);
        }).toList();
    }

    /** Reduce a batch (used or discarded due to expiry) and post a stock adjustment. */
    @Transactional
    public void dispose(Long batchId, BigDecimal qty, String reason, String user) {
        MaterialBatch b = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("Số lượng phải > 0");
        if (qty.compareTo(b.getRemainingQty()) > 0) throw new BusinessException("Vượt quá tồn của lô");

        b.setRemainingQty(b.getRemainingQty().subtract(qty));
        batchRepository.save(b);
        inventoryService.adjust(b.getMaterialId(), qty.negate(), "BATCH-" + b.getId(), user,
                reason == null || reason.isBlank() ? "Huỷ/dùng lô" : reason);
        log.info("Disposed {} from batch {} by {}", qty, batchId, user);
    }
}
