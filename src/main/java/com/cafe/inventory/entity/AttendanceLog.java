package com.cafe.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "attendance_log")
public class AttendanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "qr_date", nullable = false)
    private LocalDate qrDate;

    @Column(name = "token", length = 64)
    private String token;

    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "scan_time", nullable = false)
    private LocalDateTime scanTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
