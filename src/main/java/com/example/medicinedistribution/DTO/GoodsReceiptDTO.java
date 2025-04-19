package com.example.medicinedistribution.DTO;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceiptDTO {
    private Integer goodsReceiptId;
    
    @NotNull(message = "ID nhân viên không được để trống")
    private Integer employeeId;
    
    @NotNull(message = "ID nhà sản xuất không được để trống")
    private Integer manufacturerId;
    
    @NotNull(message = "Ngày không được để trống")
    private LocalDate date;
    
    @NotNull(message = "Tổng tiền không được để trống")
    @DecimalMin(value = "0.0", message = "Tổng tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal total;
    
    @NotEmpty(message = "Chi tiết phiếu nhập không được để trống")
    @Valid
    private List<GoodsReceiptDetailDTO> details;
}
