package com.example.medicinedistribution.GUI.Component;

import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.scene.control.TextField;
import java.math.BigDecimal;

public class CurrencyTextField extends TextField {

    private BigDecimal maxValue = new BigDecimal("1000000000"); // Default max value: 1 billion
    private String maxValueMessage = "Giới hạn";
    private String maxValueDescription = "Tối đa là 1 tỷ VND";

    public CurrencyTextField() {
        super();
    }
    public CurrencyTextField(String text) {
        super(text);
    }

    public void setMaxValue(BigDecimal maxValue, String message, String description) {
        this.maxValue = maxValue;
        this.maxValueMessage = message;
        this.maxValueDescription = description;
    }

    @Override
    public void replaceText(int start, int end, String text) {
        // Only allow digits
        if (text.matches("[0-9]*")) {
            super.replaceText(start, end, text);
            formatCurrency();
        }
    }

    @Override
    public void replaceSelection(String text) {
        // Only allow digits
        if (text.matches("[0-9]*")) {
            super.replaceSelection(text);
            formatCurrency();
        }
    }

    private void formatCurrency() {
        // Store the current text and caret position
        String currentText = getText();
        int currentCaretPosition = getCaretPosition();

        // Count commas before the caret position
        int originalCommaCount = 0;
        for (int i = 0; i < currentCaretPosition; i++) {
            if (i < currentText.length() && currentText.charAt(i) == ',') {
                originalCommaCount++;
            }
        }

        // Extract the raw number without commas
        String rawNumber = currentText.replaceAll("[^0-9]", "");

        if (!rawNumber.isEmpty()) {
            try {
                // Check for maximum value
                BigDecimal value = new BigDecimal(rawNumber);

                if (value.compareTo(maxValue) > 0) {
                    value = maxValue;
                    NotificationUtil.showErrorNotification(maxValueMessage, maxValueDescription);
                }

                // Format the number with commas
                String formatted = String.format("%,d", value.longValue());

                // Count how many digits were before the cursor
                int digitsBeforeCaret = 0;
                for (int i = 0; i < currentCaretPosition; i++) {
                    if (i < currentText.length() && Character.isDigit(currentText.charAt(i))) {
                        digitsBeforeCaret++;
                    }
                }

                // Set the formatted text
                setText(formatted);

                // Calculate new comma count before the same number of digits
                int newCommaCount = 0;
                int digitCount = 0;
                int newCaretPos = 0;

                for (int i = 0; i < formatted.length(); i++) {
                    if (Character.isDigit(formatted.charAt(i))) {
                        digitCount++;
                        if (digitCount > digitsBeforeCaret) {
                            newCaretPos = i;
                            break;
                        }
                    }
                    if (formatted.charAt(i) == ',') {
                        newCommaCount++;
                    }
                    newCaretPos = i + 1;
                }

                // If we've processed all digits but still need to position at the end
                if (digitCount == digitsBeforeCaret) {
                    newCaretPos = formatted.length();
                }

                // Set the caret position
                positionCaret(newCaretPos);

            } catch (NumberFormatException ignored) {}
        }
    }

    // Method to get the actual numeric value (without formatting)
    public BigDecimal getNumericValue() {
        String rawText = getText().replaceAll("[^0-9]", "");
        if (rawText.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(rawText);
    }
}