package com.example.medicinedistribution.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductStatisticDTO {
    // Getters and setters
    private Integer productId;
    private String productName;
    private String categoryName;
    private int quantity;

    // Constructor
    public ProductStatisticDTO(Integer productId, String productName,
                             String categoryName, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.quantity = quantity;
    }

    // Default constructor
    public ProductStatisticDTO() {
        // Default constructor
    }
    public ProductStatisticDTO(Integer productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }


}