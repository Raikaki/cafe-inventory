package com.cafe.inventory.service;

import com.cafe.inventory.entity.PeriodLock;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.repository.PeriodLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PeriodLockService {

    private final PeriodLockRepository repository;

    @Transactional(readOnly = true)
    public boolean isLocked(LocalDate date) {
        if (date == null) return false;
        return repository.existsByPeriodYearAndPeriodMonth(date.getYear(), date.getMonthValue());
    }

    /** Throws if the date falls in a locked period. Call before writing dated documents. */
    public void checkNotLocked(LocalDate date) {
        if (isLocked(date)) {
            throw new BusinessException(String.format(
                    "Kỳ %02d/%d đã khóa sổ — không thể thêm/sửa giao dịch trong kỳ này.",
                    date.getMonthValue(), date.getYear()));
        }
    }

    @Transactional(readOnly = true)
    public List<PeriodLock> list() {
        return repository.findAllByOrderByPeriodYearDescPeriodMonthDesc();
    }

    @Transactional
    public void lock(int year, int month, String user) {
        if (repository.existsByPeriodYearAndPeriodMonth(year, month)) return;
        PeriodLock l = new PeriodLock();
        l.setPeriodYear(year);
        l.setPeriodMonth(month);
        l.setLockedBy(user);
        repository.save(l);
    }

    @Transactional
    public void unlock(int year, int month) {
        repository.deleteByPeriodYearAndPeriodMonth(year, month);
    }
}
