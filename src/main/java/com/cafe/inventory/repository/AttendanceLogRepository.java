package com.cafe.inventory.repository;

import com.cafe.inventory.entity.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {
    List<AttendanceLog> findByQrDateOrderByScanTimeDesc(LocalDate qrDate);
    List<AttendanceLog> findByQrDateAndEmployeeNameIgnoreCaseOrderByScanTimeAsc(LocalDate qrDate, String employeeName);
    long deleteByQrDateBefore(LocalDate date);
}
