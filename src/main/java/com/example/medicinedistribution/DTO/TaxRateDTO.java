package com.example.medicinedistribution.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaxRateDTO {
    private long min;
    private long max;
    private int taxRate;
    private long fixedDeduction;
}
