package com.cafe.inventory.repository;

import com.cafe.inventory.entity.AttendanceQr;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceQrRepository extends JpaRepository<AttendanceQr, Long> {
    Optional<AttendanceQr> findByQrDate(LocalDate qrDate);
    long deleteByQrDateBefore(LocalDate date);
}
