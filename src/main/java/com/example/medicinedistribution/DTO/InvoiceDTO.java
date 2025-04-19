package com.example.medicinedistribution.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    private Integer invoiceId;
    
    @NotNull(message = "Mã nhân viên không được để trống")
    private Integer employeeId;
    
    @NotNull(message = "Mã khách hàng không được để trống")
    private Integer customerId;
    
    @NotNull(message = "Ngày không được để trống")
    private LocalDate date;
    
    @NotNull(message = "Tổng tiền không được để trống")
    @Min(value = 0, message = "Tổng tiền không được âm")
    private BigDecimal total;
    
    @NotEmpty(message = "Chi tiết hóa đơn không được để trống")
    @Valid
    private List<InvoiceDetailDTO> details;
}
