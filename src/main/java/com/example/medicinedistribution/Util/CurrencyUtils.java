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
    public static BigDecimal parseCurrencyToNumber(String currencyStr) {
        // Remove currency symbol and all non-numeric chars except decimal point
        String cleanedStr = currencyStr.replace("₫", "")
                                       .replace(",", "")
                                       .replaceAll("[^0-9.]", "");

        // In case there are multiple decimal points, keep only the last one
        int lastDotIndex = cleanedStr.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String beforeDot = cleanedStr.substring(0, lastDotIndex).replace(".", "");
            String afterDot = cleanedStr.substring(lastDotIndex);
            cleanedStr = beforeDot + afterDot;
        }

        return new BigDecimal(cleanedStr);
    }
}