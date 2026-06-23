package com.cafe.inventory.repository;

import com.cafe.inventory.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByWorkDateBetweenOrderByEmployeeNameAscWorkDateAsc(LocalDate from, LocalDate to);
    Optional<AttendanceRecord> findByEmployeeNameAndWorkDate(String employeeName, LocalDate workDate);
}
