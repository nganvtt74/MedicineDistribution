package com.example.medicinedistribution.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManufacturerDTO {
    private Integer manufacturerId;
    
    @NotBlank(message = "Tên nhà sản xuất không được để trống")
    @Size(max = 100, message = "Tên nhà sản xuất không được vượt quá 100 ký tự")
    private String manufacturerName;
    
    private String description;
    
    @NotBlank(message = "Quốc gia không được để trống")
    private String country;
    
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
}
