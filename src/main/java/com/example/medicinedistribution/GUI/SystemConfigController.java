package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.Util.NotificationUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

@Slf4j
public class SystemConfigController implements Initializable {
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private ComboBox<Integer> salaryStartDay;
    @FXML private ComboBox<Integer> salaryEndDay;
    @FXML private Spinner<Integer> maternityRateSpinner;
    @FXML private Spinner<Integer> averageMonthsSpinner;
    @FXML private Spinner<Integer> validLeaveDaysSpinner;
    @FXML private TextField unauthorizedLeaveField;
    @FXML private TextField lateArrivalField;
    @FXML private Button btnSave;
    @FXML private Button btnReset;

    private static final String CONFIG_FILE = "src/main/resources/config/system_config.json";
    private SystemConfig currentConfig;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupControls();
        loadConfig();
        setupEventHandlers();
    }

    private void setupControls() {
        // Setup day ComboBoxes
        IntStream.rangeClosed(1, 15).forEach(day -> {
            salaryStartDay.getItems().add(day);
            salaryEndDay.getItems().add(day);
        });

        // Setup Spinners
        SpinnerValueFactory<Integer> maternityFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 100);
        SpinnerValueFactory<Integer> monthsFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 6);
        SpinnerValueFactory<Integer> leaveDaysFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 365, 15);

        maternityRateSpinner.setValueFactory(maternityFactory);
        averageMonthsSpinner.setValueFactory(monthsFactory);
        validLeaveDaysSpinner.setValueFactory(leaveDaysFactory);

        // Setup numeric validation for text fields
        unauthorizedLeaveField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                unauthorizedLeaveField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        lateArrivalField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                lateArrivalField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void loadConfig() {
        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) {
                currentConfig = new SystemConfig();
                saveConfig();
                return;
            }

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(file));

            Gson gson = new Gson();
            currentConfig = gson.fromJson(jsonObject.toString(), SystemConfig.class);
            updateUIFromConfig();
        } catch (Exception e) {
            log.error("Error loading config: ", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải cài đặt hệ thống: " + e.getMessage());
            currentConfig = new SystemConfig();
        }
    }

    private void updateUIFromConfig() {
        startTimeField.setText(currentConfig.getStartTime());
        endTimeField.setText(currentConfig.getEndTime());
        salaryStartDay.setValue(currentConfig.getSalaryStartDay());
        salaryEndDay.setValue(currentConfig.getSalaryEndDay());
        maternityRateSpinner.getValueFactory().setValue(currentConfig.getMaternityRate());
        averageMonthsSpinner.getValueFactory().setValue(currentConfig.getAverageMonths());
        validLeaveDaysSpinner.getValueFactory().setValue(currentConfig.getValidLeaveDays());
        unauthorizedLeaveField.setText(String.valueOf(currentConfig.getUnauthorizedLeavePenalty()));
        lateArrivalField.setText(String.valueOf(currentConfig.getLateArrivalPenalty()));
    }

    private void setupEventHandlers() {
        btnSave.setOnAction(e -> saveChanges());
        btnReset.setOnAction(e -> resetToDefault());
    }

    private void saveChanges() {
        try {
            currentConfig = new SystemConfig(
                startTimeField.getText().trim(),
                endTimeField.getText().trim(),
                salaryStartDay.getValue(),
                salaryEndDay.getValue(),
                maternityRateSpinner.getValue(),
                averageMonthsSpinner.getValue(),
                validLeaveDaysSpinner.getValue(),
                Integer.parseInt(unauthorizedLeaveField.getText().trim()),
                Integer.parseInt(lateArrivalField.getText().trim())
            );
            validateConfig(currentConfig);
            saveConfig();
            NotificationUtil.showSuccessNotification("Thành công", "Đã lưu cài đặt thành công");
        }catch (IllegalArgumentException e){
            log.error("Validation error: {}", e.getMessage());
            NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
        }
        catch (Exception e) {
            log.error("Error saving config: ", e);
            NotificationUtil.showErrorNotification("Error", "Failed to save configuration: " + e.getMessage());
        }
    }
private void validateConfig(SystemConfig config) {
    // Validate time format (HH:mm)
        String timePattern = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$";
        if (!config.getStartTime().matches(timePattern)) {
            throw new IllegalArgumentException("Giờ bắt đầu phải đúng định dạng (VD: 08:30)");
        }
        if (!config.getEndTime().matches(timePattern)) {
            throw new IllegalArgumentException("Giờ kết thúc phải đúng định dạng (VD: 15:30)");
        }

        // Validate salary days range (1-15)
        if (config.getSalaryStartDay() < 1 || config.getSalaryStartDay() > 15) {
            throw new IllegalArgumentException("Ngày bắt đầu tính lương phải từ 1-15");
        }
        if (config.getSalaryEndDay() < 1 || config.getSalaryEndDay() > 15) {
            throw new IllegalArgumentException("Ngày kết thúc tính lương phải từ 1-15");
        }
        if (config.getSalaryStartDay() > config.getSalaryEndDay()) {
            throw new IllegalArgumentException("Ngày bắt đầu lương không thể lớn hơn ngày kết thúc lương");
        }

        // Validate maternity rate (0-100)
        if (config.getMaternityRate() < 0 || config.getMaternityRate() > 100) {
            throw new IllegalArgumentException("Tỷ lệ thai sản phải từ 0-100%");
        }

        // Validate average months (1-12)
        if (config.getAverageMonths() < 1 || config.getAverageMonths() > 12) {
            throw new IllegalArgumentException("Số tháng tính trung bình phải từ 1-12 tháng");
        }

        // Validate leave days (0-365)
        if (config.getValidLeaveDays() < 0 || config.getValidLeaveDays() > 365) {
            throw new IllegalArgumentException("Số ngày nghỉ phép phải từ 0-365 ngày");
        }

        // Validate penalties (must be positive)
        if (config.getUnauthorizedLeavePenalty() <= 0) {
            throw new IllegalArgumentException("Tiền phạt nghỉ không phép phải lớn hơn 0");
        }
        if (config.getLateArrivalPenalty() <= 0) {
            throw new IllegalArgumentException("Tiền phạt đi trễ phải lớn hơn 0");
        }
    }

    private void saveConfig() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(currentConfig);

        File file = new File(CONFIG_FILE);
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonString);
        }
    }

    private void resetToDefault() {
        currentConfig = new SystemConfig();
        updateUIFromConfig();
        NotificationUtil.showSuccessNotification("Thành công", "Đã khôi phục cài đặt mặc định");
    }

}

@Getter
class SystemConfig {
    // Getters
    private String startTime;
    private String endTime;
    private int salaryStartDay;
    private int salaryEndDay;
    private int maternityRate;
    private int averageMonths;
    private int validLeaveDays;
    private int unauthorizedLeavePenalty;
    private int lateArrivalPenalty;

    public SystemConfig() {
        // Default values
        this.startTime = "08:30";
        this.endTime = "15:30";
        this.salaryStartDay = 1;
        this.salaryEndDay = 1;
        this.maternityRate = 100;
        this.averageMonths = 6;
        this.validLeaveDays = 15;
        this.unauthorizedLeavePenalty = 200000;
        this.lateArrivalPenalty = 50000;
    }

    public SystemConfig(String startTime, String endTime, int salaryStartDay,
                       int salaryEndDay, int maternityRate, int averageMonths,
                       int validLeaveDays, int unauthorizedLeavePenalty,
                       int lateArrivalPenalty) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.salaryStartDay = salaryStartDay;
        this.salaryEndDay = salaryEndDay;
        this.maternityRate = maternityRate;
        this.averageMonths = averageMonths;
        this.validLeaveDays = validLeaveDays;
        this.unauthorizedLeavePenalty = unauthorizedLeavePenalty;
        this.lateArrivalPenalty = lateArrivalPenalty;
    }

}