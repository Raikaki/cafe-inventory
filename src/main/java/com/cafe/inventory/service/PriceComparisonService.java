package com.cafe.inventory.service;

import com.cafe.inventory.dto.PriceComparisonDtos.*;
import com.cafe.inventory.entity.Material;
import com.cafe.inventory.entity.Supplier;
import com.cafe.inventory.entity.SupplierPrice;
import com.cafe.inventory.repository.GoodsReceiptDetailRepository;
import com.cafe.inventory.repository.GoodsReceiptDetailRepository.HistPrice;
import com.cafe.inventory.repository.MaterialRepository;
import com.cafe.inventory.repository.SupplierPriceRepository;
import com.cafe.inventory.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Compares supplier prices per material — combining manual price quotes
 * (báo giá) with the latest actual prices from goods-receipt history — and
 * recommends the cheapest supplier for each material currently in stock.
 */
@Service
@RequiredArgsConstructor
public class PriceComparisonService {

    private final MaterialRepository materialRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierPriceRepository supplierPriceRepository;
    private final GoodsReceiptDetailRepository detailRepository;

    @Transactional(readOnly = true)
    public List<MaterialComparison> compare() {
        Map<Long, String> supplierNames = supplierRepository.findAll().stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getSupplierName, (a, b) -> a));

        // manual quotes: materialId -> (supplierId -> quote)
        Map<Long, Map<Long, SupplierPrice>> quotes = new HashMap<>();
        for (SupplierPrice sp : supplierPriceRepository.findAll()) {
            quotes.computeIfAbsent(sp.getMaterialId(), k -> new HashMap<>()).put(sp.getSupplierId(), sp);
        }

        // history: keep latest unit price per (material, supplier)
        Map<String, HistPrice> latest = new HashMap<>();
        for (HistPrice h : detailRepository.historicalPrices()) {
            if (h.getSupplierId() == null || h.getUnitPrice() == null) continue;
            String key = h.getMaterialId() + "|" + h.getSupplierId();
            HistPrice prev = latest.get(key);
            if (prev == null || nz(h.getReceiptDate()).isAfter(nz(prev.getReceiptDate()))) {
                latest.put(key, h);
            }
        }
        Map<Long, List<HistPrice>> histByMaterial = latest.values().stream()
                .collect(Collectors.groupingBy(HistPrice::getMaterialId));

        List<MaterialComparison> result = new ArrayList<>();
        for (Material m : materialRepository.findByActiveFlagTrue()) {
            Map<Long, SupplierPriceOption> options = new LinkedHashMap<>();

            // 1) history first
            for (HistPrice h : histByMaterial.getOrDefault(m.getId(), List.of())) {
                options.put(h.getSupplierId(), new SupplierPriceOption(
                        null, h.getSupplierId(),
                        supplierNames.getOrDefault(h.getSupplierId(), "NCC#" + h.getSupplierId()),
                        h.getUnitPrice(), "HISTORY"));
            }
            // 2) manual quotes override history for the same supplier
            for (SupplierPrice sp : quotes.getOrDefault(m.getId(), Map.of()).values()) {
                options.put(sp.getSupplierId(), new SupplierPriceOption(
                        sp.getId(), sp.getSupplierId(),
                        supplierNames.getOrDefault(sp.getSupplierId(), "NCC#" + sp.getSupplierId()),
                        sp.getPrice(), "QUOTE"));
            }

            List<SupplierPriceOption> opts = new ArrayList<>(options.values());
            opts.sort(Comparator.comparing(SupplierPriceOption::price));
            SupplierPriceOption cheapest = opts.isEmpty() ? null : opts.get(0);

            result.add(new MaterialComparison(
                    m.getId(), m.getMaterialCode(), m.getMaterialName(), m.getUnit(), m.getCurrentQty(),
                    opts,
                    cheapest == null ? null : cheapest.supplierId(),
                    cheapest == null ? null : cheapest.supplierName(),
                    cheapest == null ? null : cheapest.price()));
        }
        // materials with options first
        result.sort(Comparator.comparing((MaterialComparison c) -> c.options().isEmpty())
                .thenComparing(MaterialComparison::materialCode));
        return result;
    }

    @Transactional
    public SupplierPrice saveQuote(QuoteRequest req, String user) {
        SupplierPrice sp = supplierPriceRepository
                .findByMaterialIdAndSupplierId(req.materialId(), req.supplierId())
                .orElseGet(SupplierPrice::new);
        sp.setMaterialId(req.materialId());
        sp.setSupplierId(req.supplierId());
        sp.setPrice(req.price());
        sp.setNote(req.note());
        if (sp.getCreatedBy() == null) sp.setCreatedBy(user);
        return supplierPriceRepository.save(sp);
    }

    @Transactional
    public void deleteQuote(Long id) {
        supplierPriceRepository.deleteById(id);
    }

    private LocalDate nz(LocalDate d) {
        return d == null ? LocalDate.of(1970, 1, 1) : d;
    }
}
