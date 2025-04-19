package com.example.medicinedistribution.DTO;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Integer customerId;
    private String customerName;
    private String phone;
    private String email;
    private String address;
}
