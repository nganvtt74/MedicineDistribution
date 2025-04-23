package com.example.medicinedistribution.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InsuranceDTO {
    private String insuranceId;
    private double percentage;
}