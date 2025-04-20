package com.example.medicinedistribution.Util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {

    private static final NumberFormat vnFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    static {
        vnFormat.setMaximumFractionDigits(0);
        vnFormat.setCurrency(java.util.Currency.getInstance("VND"));
    }

    public static String formatVND(BigDecimal amount) {
        if (amount == null) {
            return "0₫";
        }
        return vnFormat.format(amount).replace("VND", "₫");
    }
}