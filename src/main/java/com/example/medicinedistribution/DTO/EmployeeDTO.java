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
public EmployeeDTO(EmployeeDTO employeeDTO) {
    this.employeeId = employeeDTO.getEmployeeId();
    this.firstName = employeeDTO.getFirstName();
    this.lastName = employeeDTO.getLastName();
    this.birthday = employeeDTO.getBirthday();
    this.gender = employeeDTO.getGender();
    this.phone = employeeDTO.getPhone();
    this.email = employeeDTO.getEmail();
    this.hireDate = employeeDTO.getHireDate();
    this.address = employeeDTO.getAddress();
    this.basicSalary = employeeDTO.getBasicSalary();
    this.status = employeeDTO.getStatus();
    this.positionId = employeeDTO.getPositionId();
    this.accountId = employeeDTO.getAccountId();
    this.positionName = employeeDTO.getPositionName();
    this.dependentCount = employeeDTO.getDependentCount();
}


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

    @Pattern(
            regexp = "^(?:(?:0|\\+84)\\d{9}|\\+\\d{1,3}(?:[ \\-]\\d{1,4}){2,4})$",
            message = "Số điện thoại không hợp lệ"
    )    private String phone;
    
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

    private String positionName;
    private Integer dependentCount;

}
