module com.example.medicinedistribution {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.medicinedistribution to javafx.fxml;
    exports com.example.medicinedistribution;
}