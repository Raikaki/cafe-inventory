package com.cafe.inventory.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a money amount to Vietnamese words ("số tiền bằng chữ"),
 * as required on accounting vouchers. Rounds to whole đồng.
 */
public final class NumberToWordsVi {

    private NumberToWordsVi() {}

    private static final String[] DIGITS = {
            "không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"
    };

    public static String toVietnamese(BigDecimal amount) {
        if (amount == null) return "Không đồng";
        long n = amount.setScale(0, RoundingMode.HALF_UP).longValue();
        boolean negative = n < 0;
        n = Math.abs(n);
        if (n == 0) return "Không đồng";

        List<Integer> groups = new ArrayList<>();
        while (n > 0) { groups.add((int) (n % 1000)); n /= 1000; }

        int g = groups.size();
        StringBuilder sb = new StringBuilder();
        for (int i = g - 1; i >= 0; i--) {
            String read = readTriple(groups.get(i), i < g - 1);
            if (read.isEmpty()) continue;
            sb.append(read);
            String scale = scaleWord(i);
            if (!scale.isEmpty()) sb.append(' ').append(scale);
            sb.append(' ');
        }

        String res = sb.toString().replaceAll("\\s+", " ").trim();
        res = Character.toUpperCase(res.charAt(0)) + res.substring(1) + " đồng";
        return negative ? "Âm " + res : res;
    }

    private static String readTriple(int n, boolean full) {
        int tram = n / 100, chuc = (n % 100) / 10, donvi = n % 10;
        StringBuilder s = new StringBuilder();
        if (tram > 0) {
            s.append(DIGITS[tram]).append(" trăm");
            if (chuc == 0 && donvi > 0) s.append(" lẻ");
        } else if (full && (chuc > 0 || donvi > 0)) {
            s.append("không trăm");
            if (chuc == 0 && donvi > 0) s.append(" lẻ");
        }
        if (chuc > 1) {
            s.append(' ').append(DIGITS[chuc]).append(" mươi");
            if (donvi == 1) s.append(" mốt");
            else if (donvi == 5) s.append(" lăm");
            else if (donvi > 0) s.append(' ').append(DIGITS[donvi]);
        } else if (chuc == 1) {
            s.append(" mười");
            if (donvi == 1) s.append(" một");
            else if (donvi == 5) s.append(" lăm");
            else if (donvi > 0) s.append(' ').append(DIGITS[donvi]);
        } else { // chuc == 0
            if (donvi > 0) s.append(' ').append(DIGITS[donvi]);
        }
        return s.toString().trim();
    }

    private static String scaleWord(int p) {
        if (p == 0) return "";
        int ty = p / 3, r = p % 3;
        StringBuilder s = new StringBuilder(r == 0 ? "" : (r == 1 ? "nghìn" : "triệu"));
        for (int k = 0; k < ty; k++) {
            if (s.length() > 0) s.append(' ');
            s.append("tỷ");
        }
        return s.toString().trim();
    }
}
