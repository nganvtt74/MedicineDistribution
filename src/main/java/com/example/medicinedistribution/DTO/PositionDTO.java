package com.example.medicinedistribution.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionDTO {
    private int positionId;
    
    @NotBlank(message = "Tên chức vụ không được để trống")
    @Size(max = 100, message = "Tên chức vụ không được vượt quá 100 ký tự")
    private String positionName;
    
    @NotNull(message = "Mã phòng ban không được để trống")
    private int departmentId;
}
