package com.example.medicinedistribution.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {
    private Integer employeeId;
    
    @NotBlank(message = "Họ không được để trống")
    private String firstName;
    
    @NotBlank(message = "Tên không được để trống")
    private String lastName;
    
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate birthday;
    
    @NotBlank(message = "Giới tính không được để trống")
    private String gender;
    
    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phone;
    
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotNull(message = "Ngày thuê không được để trống")
    private LocalDate hireDate;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
    
    @NotNull(message = "Lương cơ bản không được để trống")
    @DecimalMin(value = "0.0", message = "Lương cơ bản phải lớn hơn 0")
    private BigDecimal basicSalary;
    
    @NotNull(message = "Trạng thái không được để trống")
    private Integer status;
    
    @NotNull(message = "Vị trí không được để trống")
    private Integer positionId;
    
    private Integer accountId;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
