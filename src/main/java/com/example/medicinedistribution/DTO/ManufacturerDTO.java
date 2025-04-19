package com.example.medicinedistribution.DTO;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturerDTO {
    private Integer manufacturerId;
    private String manufacturerName;
    private String description;
    private String country;
    private String email;
    private String phone;
    private String address;
}
