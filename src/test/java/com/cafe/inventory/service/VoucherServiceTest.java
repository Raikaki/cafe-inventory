package com.cafe.inventory.service;

import com.cafe.inventory.dto.VoucherDtos.VoucherRequest;
import com.cafe.inventory.entity.Voucher;
import com.cafe.inventory.exception.BusinessException;
import com.cafe.inventory.repository.VoucherRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VoucherServiceTest {

    @Mock VoucherRepository voucherRepository;
    @InjectMocks VoucherService service;

    @BeforeEach
    void setUp() {
        when(voucherRepository.countByVoucherNoStartingWith(anyString())).thenReturn(0L);
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void create_generatesNumberAndAmountInWords() {
        VoucherRequest req = new VoucherRequest("PHIEU_THU", LocalDate.now(),
                "Quán Cafe", null, "Khách A", null, "Thu tiền bán hàng",
                null, null, new BigDecimal("1500000"), "Quản lý", null, null);

        Voucher v = service.create(req, "user");

        String year = String.valueOf(LocalDate.now().getYear());
        assertThat(v.getVoucherNo()).startsWith("PT" + year);
        assertThat(v.getAmountInWords()).isEqualTo("Một triệu năm trăm nghìn đồng");
        assertThat(v.getVoucherType()).isEqualTo("PHIEU_THU");
    }

    @Test
    void create_rejectsInvalidType() {
        VoucherRequest req = new VoucherRequest("XYZ", LocalDate.now(),
                null, null, null, null, null, null, null, new BigDecimal("1000"), null, null, null);
        assertThatThrownBy(() -> service.create(req, "user"))
                .isInstanceOf(BusinessException.class);
    }
}
