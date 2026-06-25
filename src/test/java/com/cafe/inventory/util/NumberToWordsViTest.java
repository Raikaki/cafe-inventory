package com.cafe.inventory.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class NumberToWordsViTest {

    private String w(String n) {
        return NumberToWordsVi.toVietnamese(new BigDecimal(n));
    }

    @Test
    void zeroAndNull() {
        assertThat(w("0")).isEqualTo("Không đồng");
        assertThat(NumberToWordsVi.toVietnamese(null)).isEqualTo("Không đồng");
    }

    @Test
    void smallNumbers() {
        assertThat(w("1")).isEqualTo("Một đồng");
        assertThat(w("15")).isEqualTo("Mười lăm đồng");
        assertThat(w("21")).isEqualTo("Hai mươi mốt đồng");
        assertThat(w("105")).isEqualTo("Một trăm lẻ năm đồng");
        assertThat(w("1000")).isEqualTo("Một nghìn đồng");
    }

    @Test
    void thousandsAndMillions() {
        assertThat(w("1500000")).isEqualTo("Một triệu năm trăm nghìn đồng");
        assertThat(w("1000000")).isEqualTo("Một triệu đồng");
        assertThat(w("1000000000")).isEqualTo("Một tỷ đồng");
    }

    @Test
    void roundsDecimalsToDong() {
        assertThat(w("1000.4")).isEqualTo("Một nghìn đồng");
        assertThat(w("999.6")).isEqualTo("Một nghìn đồng");
    }

    @Test
    void largeNumberStructure() {
        String s = w("123456789");
        assertThat(s).startsWith("Một trăm hai mươi ba triệu");
        assertThat(s).contains("bốn trăm năm mươi sáu nghìn");
        assertThat(s).endsWith("bảy trăm tám mươi chín đồng");
    }
}
