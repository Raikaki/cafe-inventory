package com.cafe.inventory.service;

import com.cafe.inventory.dto.AttendanceDtos.CheckinRequest;
import com.cafe.inventory.dto.AttendanceDtos.CheckinResponse;
import com.cafe.inventory.entity.AttendanceLog;
import com.cafe.inventory.entity.AttendanceQr;
import com.cafe.inventory.entity.StoreLocation;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.repository.AttendanceLogRepository;
import com.cafe.inventory.repository.AttendanceQrRepository;
import com.cafe.inventory.repository.StoreLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AttendanceServiceTest {

    @Mock AttendanceQrRepository qrRepository;
    @Mock AttendanceLogRepository logRepository;
    @Mock StoreLocationRepository storeLocationRepository;
    @InjectMocks AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        AttendanceQr qr = new AttendanceQr();
        qr.setQrDate(LocalDate.now());
        qr.setToken("TOK");
        when(qrRepository.findByQrDate(any())).thenReturn(Optional.of(qr));
        when(logRepository.findByQrDateAndEmployeeNameIgnoreCaseOrderByScanTimeAsc(any(), anyString()))
                .thenReturn(List.of());
        when(logRepository.save(any(AttendanceLog.class))).thenAnswer(i -> i.getArgument(0));
    }

    private StoreLocation store(double lat, double lng, int radius, boolean active) {
        StoreLocation s = new StoreLocation();
        s.setLatitude(BigDecimal.valueOf(lat));
        s.setLongitude(BigDecimal.valueOf(lng));
        s.setRadiusMeters(radius);
        s.setActiveFlag(active);
        return s;
    }

    @Test
    void checkin_outsideRadius_isRejected() {
        when(storeLocationRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(store(10.0, 106.0, 100, true)));
        // ~15 km away
        CheckinRequest req = new CheckinRequest("TOK", "Nhân viên A",
                new BigDecimal("10.1"), new BigDecimal("106.1"));

        assertThatThrownBy(() -> attendanceService.checkin(req, "1.2.3.4", "agent"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void checkin_withinRadius_recordsCheckIn() {
        when(storeLocationRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(store(10.0, 106.0, 1000, true)));
        // ~55 m away
        CheckinRequest req = new CheckinRequest("TOK", "Nhân viên A",
                new BigDecimal("10.0005"), new BigDecimal("106.0"));

        CheckinResponse res = attendanceService.checkin(req, "1.2.3.4", "agent");
        assertThat(res.checkType()).isEqualTo("VAO");
        assertThat(res.employeeName()).isEqualTo("Nhân viên A");
    }

    @Test
    void checkin_noGeofenceConfigured_allowedAnywhere() {
        when(storeLocationRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        CheckinRequest req = new CheckinRequest("TOK", "Nhân viên B", null, null);

        CheckinResponse res = attendanceService.checkin(req, "1.2.3.4", "agent");
        assertThat(res.checkType()).isEqualTo("VAO");
    }
}
