package com.example.medicinedistribution.DTO;
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
    private Integer employeeId;
    private Integer customerId;
    private LocalDate date;
    private BigDecimal total;
    private List<InvoiceDetailDTO> details;
}