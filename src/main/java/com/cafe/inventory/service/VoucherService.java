package com.cafe.inventory.service;

import com.cafe.inventory.dto.VoucherDtos.VoucherRequest;
import com.cafe.inventory.entity.Voucher;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.exception.ResourceNotFoundException;
import com.cafe.inventory.repository.VoucherRepository;
import com.cafe.inventory.util.NumberToWordsVi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final PeriodLockService periodLockService;

    private static final Map<String, String> PREFIX = Map.of(
            "PHIEU_THU", "PT",
            "PHIEU_CHI", "PC",
            "PHIEU_NHAP_KHO", "PNK",
            "PHIEU_XUAT_KHO", "PXK",
            "HOA_DON", "HD",
            "KHAC", "CT"
    );

    @Transactional(readOnly = true)
    public List<Voucher> query(LocalDate from, LocalDate to, String type) {
        String t = (type == null || type.isBlank()) ? null : type;
        return voucherRepository.query(from, to, t);
    }

    @Transactional(readOnly = true)
    public Voucher get(Long id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found: " + id));
    }

    @Transactional
    public Voucher create(VoucherRequest req, String user) {
        if (!PREFIX.containsKey(req.voucherType())) {
            throw new BusinessException("Loại chứng từ không hợp lệ: " + req.voucherType());
        }
        periodLockService.checkNotLocked(req.voucherDate());
        Voucher v = new Voucher();
        v.setVoucherType(req.voucherType());
        v.setVoucherNo(nextNo(req.voucherType()));
        v.setVoucherDate(req.voucherDate());
        v.setCreatorName(req.creatorName());
        v.setCreatorAddress(req.creatorAddress());
        v.setPartnerName(req.partnerName());
        v.setPartnerAddress(req.partnerAddress());
        v.setContent(req.content());
        v.setQuantity(req.quantity());
        v.setUnit(req.unit());
        v.setAmount(req.amount());
        v.setAmountInWords(NumberToWordsVi.toVietnamese(req.amount()));
        v.setDebitAccount(req.debitAccount());
        v.setCreditAccount(req.creditAccount());
        v.setApproverName(req.approverName());
        v.setNote(req.note());
        v.setAttachmentUrl(req.attachmentUrl());
        v.setCreatedBy(user);
        Voucher saved = voucherRepository.save(v);
        log.info("Voucher {} created by {}", saved.getVoucherNo(), user);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        voucherRepository.delete(get(id));
    }

    private String nextNo(String type) {
        String prefix = PREFIX.get(type) + LocalDate.now().getYear();
        long seq = voucherRepository.countByVoucherNoStartingWith(prefix) + 1;
        return String.format("%s%05d", prefix, seq);
    }
}
