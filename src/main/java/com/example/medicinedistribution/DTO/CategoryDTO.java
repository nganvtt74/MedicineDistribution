package com.example.medicinedistribution.DTO;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NotNull(message = "Danh mục không được để trống")
public class CategoryDTO {
    private Integer categoryId;
    
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String categoryName;

    @Override
    public String toString() {
        return categoryName;
    }
}
