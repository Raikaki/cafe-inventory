package com.cafe.inventory.service;

import com.cafe.inventory.dto.AttendanceDtos.*;
import com.cafe.inventory.entity.AttendanceLog;
import com.cafe.inventory.entity.AttendanceQr;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.repository.AttendanceLogRepository;
import com.cafe.inventory.repository.AttendanceQrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceQrRepository qrRepository;
    private final AttendanceLogRepository logRepository;

    /** Today's random token, generated once per day. */
    @Transactional
    public AttendanceQr getOrCreateTodayToken() {
        LocalDate today = LocalDate.now();
        return qrRepository.findByQrDate(today).orElseGet(() -> {
            AttendanceQr qr = new AttendanceQr();
            qr.setQrDate(today);
            qr.setToken(UUID.randomUUID().toString().replace("-", ""));
            return qrRepository.save(qr);
        });
    }

    @Transactional
    public CheckinResponse checkin(CheckinRequest req, String ip, String userAgent) {
        LocalDate today = LocalDate.now();
        AttendanceQr qr = qrRepository.findByQrDate(today)
                .orElseThrow(() -> new BusinessException("Mã QR chưa được tạo cho hôm nay"));
        if (!qr.getToken().equals(req.token())) {
            throw new BusinessException("Mã QR không hợp lệ hoặc đã hết hạn (không phải mã của hôm nay)");
        }

        AttendanceLog logEntry = new AttendanceLog();
        logEntry.setQrDate(today);
        logEntry.setToken(req.token());
        logEntry.setEmployeeName(req.employeeName().trim());
        logEntry.setIpAddress(ip);
        logEntry.setLatitude(req.latitude());
        logEntry.setLongitude(req.longitude());
        logEntry.setUserAgent(userAgent);
        logEntry.setScanTime(LocalDateTime.now());
        logRepository.save(logEntry);

        log.info("Attendance check-in: {} from IP {} at {}", logEntry.getEmployeeName(), ip, logEntry.getScanTime());
        return new CheckinResponse(logEntry.getEmployeeName(), logEntry.getScanTime().toString(),
                ip, logEntry.getLatitude(), logEntry.getLongitude());
    }

    @Transactional(readOnly = true)
    public List<AttendanceLog> logs(LocalDate date) {
        return logRepository.findByQrDateOrderByScanTimeDesc(date);
    }
}
