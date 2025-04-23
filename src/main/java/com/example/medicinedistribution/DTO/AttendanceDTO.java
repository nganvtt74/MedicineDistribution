package com.example.medicinedistribution.DTO;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDTO {
    private Integer employee_id;
    private LocalDate date;
    private LocalDateTime check_in;
    private LocalDateTime check_out;
    private Integer status;
}
