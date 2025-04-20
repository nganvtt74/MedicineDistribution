package com.example.medicinedistribution.DTO;

import javafx.scene.control.Button;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentInfo {
    private Button button;
    private String permission;
    private String fxmlPath; // Đổi "fxml" thành "fxmlPath" để rõ ràng hơn
    private Object controller;
}
