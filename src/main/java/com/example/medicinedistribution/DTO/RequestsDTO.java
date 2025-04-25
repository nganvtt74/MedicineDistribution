package com.example.medicinedistribution.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestsDTO {
    private Integer request_id;
    private Integer type_id;
    private LocalDate start_date;
    private LocalDate end_date;
    private Integer duration;
    private Integer employee_id;
    private String reason;
    private String status;
    private LocalDate created_at;
    private Integer approved_by;
    private LocalDate approved_at;
}
