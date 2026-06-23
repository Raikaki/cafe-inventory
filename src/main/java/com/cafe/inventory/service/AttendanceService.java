package com.cafe.inventory.service;

import com.cafe.inventory.dto.AttendanceDtos.*;
import com.cafe.inventory.entity.AttendanceLog;
import com.cafe.inventory.entity.AttendanceQr;
import com.cafe.inventory.entity.StoreLocation;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.repository.AttendanceLogRepository;
import com.cafe.inventory.repository.AttendanceQrRepository;
import com.cafe.inventory.repository.StoreLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final StoreLocationRepository storeLocationRepository;

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

        // ----- Geofence: only enforce when the store location is configured & active -----
        StoreLocation loc = storeLocationRepository.findFirstByOrderByIdAsc().orElse(null);
        if (loc != null && Boolean.TRUE.equals(loc.getActiveFlag())
                && loc.getLatitude() != null && loc.getLongitude() != null) {
            if (req.latitude() == null || req.longitude() == null) {
                throw new BusinessException("Cần bật định vị (GPS) để chấm công tại quán");
            }
            double dist = distanceMeters(
                    loc.getLatitude().doubleValue(), loc.getLongitude().doubleValue(),
                    req.latitude().doubleValue(), req.longitude().doubleValue());
            if (dist > loc.getRadiusMeters()) {
                throw new BusinessException(String.format(
                        "Bạn đang cách quán ~%.0fm (chỉ cho phép trong %dm). Không thể chấm công ngoài khu vực quán.",
                        dist, loc.getRadiusMeters()));
            }
        }

        String name = req.employeeName().trim();
        // Allow multiple scans per day. The first scan is the check-in (VAO);
        // any later scan is a check-out (RA). The report takes MIN time as giờ vào
        // and MAX time as giờ ra.
        List<AttendanceLog> existing =
                logRepository.findByQrDateAndEmployeeNameIgnoreCaseOrderByScanTimeAsc(today, name);
        String checkType = existing.isEmpty() ? "VAO" : "RA";

        AttendanceLog logEntry = new AttendanceLog();
        logEntry.setQrDate(today);
        logEntry.setToken(req.token());
        logEntry.setEmployeeName(name);
        logEntry.setCheckType(checkType);
        logEntry.setIpAddress(ip);
        logEntry.setLatitude(req.latitude());
        logEntry.setLongitude(req.longitude());
        logEntry.setUserAgent(userAgent);
        logEntry.setScanTime(LocalDateTime.now());
        logRepository.save(logEntry);

        log.info("Attendance {} : {} from IP {} at {}", checkType, name, ip, logEntry.getScanTime());
        return new CheckinResponse(name, checkType, logEntry.getScanTime().toString(),
                ip, logEntry.getLatitude(), logEntry.getLongitude());
    }

    @Transactional(readOnly = true)
    public List<AttendanceLog> logs(LocalDate date) {
        return logRepository.findByQrDateOrderByScanTimeDesc(date);
    }

    // ---------- Store location settings ----------
    @Transactional
    public StoreLocation getStoreLocation() {
        return storeLocationRepository.findFirstByOrderByIdAsc().orElseGet(() -> {
            StoreLocation l = new StoreLocation();
            l.setRadiusMeters(200);
            l.setActiveFlag(false);
            return storeLocationRepository.save(l);
        });
    }

    @Transactional
    public StoreLocation saveStoreLocation(LocationSetting s, String user) {
        StoreLocation l = getStoreLocation();
        l.setLatitude(s.latitude());
        l.setLongitude(s.longitude());
        l.setRadiusMeters(s.radiusMeters() == null ? 200 : s.radiusMeters());
        l.setActiveFlag(Boolean.TRUE.equals(s.activeFlag()));
        l.setUpdatedBy(user);
        return storeLocationRepository.save(l);
    }

    // ---------- Retention: keep only current + previous month ----------
    @Transactional
    public long purgeOld() {
        LocalDate cutoff = LocalDate.now().withDayOfMonth(1).minusMonths(1); // start of previous month
        long deletedLogs = logRepository.deleteByQrDateBefore(cutoff);
        qrRepository.deleteByQrDateBefore(cutoff);
        if (deletedLogs > 0) {
            log.info("Attendance retention: deleted {} log(s) before {}", deletedLogs, cutoff);
        }
        return deletedLogs;
    }

    /** Haversine distance in metres. */
    private double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
