package com.cafe.inventory.repository;

import com.cafe.inventory.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    long countByVoucherNoStartingWith(String prefix);

    @Query("SELECT v FROM Voucher v WHERE v.voucherDate BETWEEN :from AND :to " +
           "AND (:type IS NULL OR v.voucherType = :type) " +
           "ORDER BY v.voucherDate DESC, v.voucherNo DESC")
    List<Voucher> query(@Param("from") LocalDate from,
                        @Param("to") LocalDate to,
                        @Param("type") String type);
}
