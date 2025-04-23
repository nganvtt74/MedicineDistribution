package com.example.medicinedistribution.DTO;

import lombok.Getter;

@Getter
public class SystemConfig {
    // Getters
    private String startTime;
    private String endTime;
    private int salaryStartDay;
    private int salaryEndDay;
    private int maternityRate;
    private int averageMonths;
    private int validLeaveDays;
    private int unauthorizedLeavePenalty;
    private int lateArrivalPenalty;

    public SystemConfig() {
        // Default values
        this.startTime = "08:30";
        this.endTime = "15:30";
        this.salaryStartDay = 1;
        this.salaryEndDay = 1;
        this.maternityRate = 100;
        this.averageMonths = 6;
        this.validLeaveDays = 15;
        this.unauthorizedLeavePenalty = 200000;
        this.lateArrivalPenalty = 50000;
    }

    public SystemConfig(String startTime, String endTime, int salaryStartDay,
                        int salaryEndDay, int maternityRate, int averageMonths,
                        int validLeaveDays, int unauthorizedLeavePenalty,
                        int lateArrivalPenalty) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.salaryStartDay = salaryStartDay;
        this.salaryEndDay = salaryEndDay;
        this.maternityRate = maternityRate;
        this.averageMonths = averageMonths;
        this.validLeaveDays = validLeaveDays;
        this.unauthorizedLeavePenalty = unauthorizedLeavePenalty;
        this.lateArrivalPenalty = lateArrivalPenalty;
    }

}