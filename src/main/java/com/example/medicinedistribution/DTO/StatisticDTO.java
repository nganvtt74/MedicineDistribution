package com.example.medicinedistribution.DTO;

import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
public class StatisticDTO {
    // Getters and setters
    private String period;
    private BigDecimal amount;
    private int count;
    private BigDecimal revenue;
    private BigDecimal expense;
    private BigDecimal profit;

    public StatisticDTO() {
        this.amount = BigDecimal.ZERO;
        this.count = 0;
        this.revenue = BigDecimal.ZERO;
        this.expense = BigDecimal.ZERO;
        this.profit = BigDecimal.ZERO;
    }

    public StatisticDTO(String period, BigDecimal amount, int count) {
        this.period = period;
        this.amount = amount;
        this.count = count;
    }

    public StatisticDTO(String period, BigDecimal revenue, BigDecimal expense) {
        this.period = period;
        this.revenue = revenue;
        this.expense = expense;
        this.profit = revenue.subtract(expense);
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
        updateProfit();
    }

    public void setExpense(BigDecimal expense) {
        this.expense = expense;
        updateProfit();
    }


    private void updateProfit() {
        if (revenue != null && expense != null) {
            this.profit = revenue.subtract(expense);
        }
    }
}