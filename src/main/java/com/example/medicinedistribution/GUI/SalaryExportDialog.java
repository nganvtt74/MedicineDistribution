package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.PayrollBUS;
import com.example.medicinedistribution.DTO.DepartmentDTO;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.PayrollDTO;
import com.example.medicinedistribution.DTO.PositionDTO;
import com.example.medicinedistribution.Util.CurrencyUtils;
import com.example.medicinedistribution.Util.NotificationUtil;
import com.example.medicinedistribution.Util.PdfExportUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Slf4j
public class SalaryExportDialog {

    public static void show(BUSFactory busFactory, Window owner) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Thông tin lương");
        dialogStage.initOwner(owner);
        dialogStage.setResizable(false);
        dialogStage.getIcons().add(new Image(Objects.requireNonNull(SalaryExportDialog.class.getResource("../../../../img/logo.png")).toExternalForm()));

        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.getStyleClass().add("content-area");

        // Title and Period Selection Section
        Label titleLabel = new Label("Thông tin lương");
        titleLabel.getStyleClass().add("section-header");

        // Period selection controls
        HBox periodSelectionBox = new HBox(15);
        periodSelectionBox.setAlignment(Pos.CENTER);

        // Month ComboBox with styling
        Label monthLabel = new Label("Tháng:");
        monthLabel.getStyleClass().add("form-label");

        ComboBox<Integer> monthComboBox = new ComboBox<>();
        monthComboBox.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        monthComboBox.setValue(LocalDateTime.now().getMonthValue());
        monthComboBox.setPromptText("Tháng");
        monthComboBox.setPrefWidth(80);
        monthComboBox.getStyleClass().add("combo-box");

        // Year ComboBox with styling
        Label yearLabel = new Label("Năm:");
        yearLabel.getStyleClass().add("form-label");

        ComboBox<Integer> yearComboBox = new ComboBox<>();
        int currentYear = LocalDateTime.now().getYear();
        for (int i = currentYear - 5; i <= currentYear; i++) {
            yearComboBox.getItems().add(i);
        }
        yearComboBox.setValue(currentYear);
        yearComboBox.setPromptText("Năm");
        yearComboBox.setPrefWidth(100);
        yearComboBox.getStyleClass().add("combo-box");

        Button viewButton = new Button("Xem");
        viewButton.getStyleClass().addAll("button", "primary-button");

        periodSelectionBox.getChildren().addAll(monthLabel, monthComboBox, yearLabel, yearComboBox, viewButton);

        // Scroll pane for salary details
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.getStyleClass().add("details-scroll-pane");

        // Container for salary information
        VBox salaryInfoContainer = new VBox(10);
        salaryInfoContainer.setPadding(new Insets(10));
        salaryInfoContainer.getStyleClass().add("form-section");
        scrollPane.setContent(salaryInfoContainer);

        // Initial salary info placeholder
        Label initialInfoLabel = new Label("Chọn tháng và năm để xem thông tin lương");
        initialInfoLabel.getStyleClass().add("info-text");
        salaryInfoContainer.getChildren().add(initialInfoLabel);

        // Action buttons
        HBox actionButtonBox = new HBox(15);
        actionButtonBox.setAlignment(Pos.CENTER);
        actionButtonBox.setPadding(new Insets(10, 0, 0, 0));

        Button exportButton = new Button("Xuất PDF");
        exportButton.getStyleClass().addAll("button", "primary-button");
        exportButton.setFont(javafx.scene.text.Font.font(16));
        exportButton.setDisable(true);

        Button closeButton = new Button("Đóng");
        closeButton.getStyleClass().addAll("button", "danger-button");
        closeButton.setFont(javafx.scene.text.Font.font(16));

        actionButtonBox.getChildren().addAll(exportButton, closeButton);

        // Add all components to main container
        mainContainer.getChildren().addAll(titleLabel, periodSelectionBox, scrollPane, actionButtonBox);

        // View button action
        viewButton.setOnAction(e -> {
            Integer selectedMonth = monthComboBox.getValue();
            Integer selectedYear = yearComboBox.getValue();

            if (selectedMonth != null && selectedYear != null) {
                try {
                    // Get current employee's payroll data
                    int employeeId = busFactory.getUserSession().getEmployee().getEmployeeId();
                    PayrollBUS payrollBUS = busFactory.getPayrollBUS();
                    PayrollDTO payroll = payrollBUS.getPayrollByEmployeeId(employeeId, selectedMonth, selectedYear);

                    if (payroll != null) {
                        // Enable export button
                        exportButton.setDisable(false);

                        // Display salary information
                        updateSalaryDisplay(salaryInfoContainer, payroll, busFactory.getUserSession().getEmployee(), selectedMonth, selectedYear, busFactory);
                    } else {
                        salaryInfoContainer.getChildren().clear();
                        Label noDataLabel = new Label("Không có dữ liệu lương cho tháng " + selectedMonth + "/" + selectedYear);
                        noDataLabel.getStyleClass().add("error-label");
                        salaryInfoContainer.getChildren().add(noDataLabel);
                        exportButton.setDisable(true);
                    }
                } catch (Exception ex) {
                    log.error("Error loading payroll data", ex);
                    NotificationUtil.showErrorNotification("Lỗi", "Không thể tải dữ liệu lương: " + ex.getMessage());
                    exportButton.setDisable(true);
                }
            } else {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn tháng và năm");
            }
        });

        // Export button action
        exportButton.setOnAction(e -> {
            Integer selectedMonth = monthComboBox.getValue();
            Integer selectedYear = yearComboBox.getValue();

            if (selectedMonth != null && selectedYear != null) {
                PdfExportUtils.exportEmployeePayrollToPdf(
                        busFactory.getUserSession().getEmployee().getEmployeeId(),
                        selectedMonth,
                        selectedYear,
                        busFactory);
            } else {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn tháng và năm");
            }
        });

        closeButton.setOnAction(e -> dialogStage.close());

        Scene dialogScene = new Scene(mainContainer, 650, 600);
        dialogScene.getStylesheets().add(SalaryExportDialog.class.getResource("/css/main-style.css").toExternalForm());

        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    private static void updateSalaryDisplay(VBox container, PayrollDTO payroll, EmployeeDTO employee, int month, int year,BUSFactory busFactory) {
        container.getChildren().clear();

        // Header
        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);

        Label periodLabel = new Label("BẢNG LƯƠNG THÁNG " + month + "/" + year);
        periodLabel.getStyleClass().add("section-title");

        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        Label monthInfoLabel = new Label("Tổng số ngày trong tháng: " + daysInMonth);
        monthInfoLabel.getStyleClass().add("info-text");

        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Label dateLabel = new Label("Ngày xuất: " + formattedDate);
        dateLabel.getStyleClass().add("info-text");

        headerBox.getChildren().addAll(periodLabel, monthInfoLabel, dateLabel);

        // Employee info section
        Label employeeInfoTitle = new Label("Thông tin nhân viên");
        employeeInfoTitle.getStyleClass().add("section-subtitle");

        GridPane employeeInfoGrid = new GridPane();
        employeeInfoGrid.setHgap(15);
        employeeInfoGrid.setVgap(10);
        employeeInfoGrid.setPadding(new Insets(10, 0, 15, 0));

        // Employee details
        PositionDTO position = busFactory.getPositionBUS().findById(employee.getPositionId());
        DepartmentDTO department = busFactory.getDepartmentBUS().findById(position.getDepartmentId());
        addLabelPair(employeeInfoGrid, 0, "Mã nhân viên:", employee.getEmployeeId() + "");
        addLabelPair(employeeInfoGrid, 1, "Họ và tên:", employee.getFullName());
        addLabelPair(employeeInfoGrid, 2, "Phòng ban:",department!= null ? department.getDepartmentName() : "");
        addLabelPair(employeeInfoGrid, 3, "Chức vụ:", employee.getPositionName() != null ? employee.getPositionName() : "");

        // Salary details section
        Label salaryDetailsTitle = new Label("Chi tiết lương");
        salaryDetailsTitle.getStyleClass().add("section-subtitle");

        GridPane salaryDetailsGrid = new GridPane();
        salaryDetailsGrid.setHgap(15);
        salaryDetailsGrid.setVgap(10);
        salaryDetailsGrid.setPadding(new Insets(10, 0, 15, 0));

        int row = 0;
        // Income section
        addLabelPair(salaryDetailsGrid, row++, "Lương cơ bản:", formatCurrency(payroll.getBase_salary()));
        addLabelPair(salaryDetailsGrid, row++, "Ngày công thực tế:", payroll.getActual_working_days() + "");
        addLabelPair(salaryDetailsGrid, row++, "Đi trễ:", payroll.getLate_days() + "");
        addLabelPair(salaryDetailsGrid, row++, "Nghỉ không phép:", payroll.getLeave_days() + "");

        // Allowances
        Label allowanceTitle = new Label("Phụ cấp");
        allowanceTitle.getStyleClass().add("form-label");
        allowanceTitle.setStyle("-fx-font-weight: bold;");
        salaryDetailsGrid.add(allowanceTitle, 0, row++, 2, 1);

        addLabelPair(salaryDetailsGrid, row++, "Phụ cấp chức vụ:", formatCurrency(payroll.getPosition_allowance()));
        addLabelPair(salaryDetailsGrid, row++, "Phụ cấp ăn trưa:", formatCurrency(payroll.getMeal_allowance()));
        addLabelPair(salaryDetailsGrid, row++, "Phụ cấp xăng xe:", formatCurrency(payroll.getGas_allowance()));
        addLabelPair(salaryDetailsGrid, row++, "Phụ cấp điện thoại:", formatCurrency(payroll.getPhone_allowance()));
        addLabelPair(salaryDetailsGrid, row++, "Phụ cấp trách nhiệm:", formatCurrency(payroll.getResponsibility_allowance()));
        addLabelPair(salaryDetailsGrid, row++, "Phụ cấp khác:", formatCurrency(payroll.getOther_allowance()));
        addLabelPair(salaryDetailsGrid, row++, "Tổng phụ cấp:", formatCurrency(payroll.getTotal_allowance()));

        // Bonus
        addLabelPair(salaryDetailsGrid, row++, "Thưởng:", formatCurrency(payroll.getBonus_total()));
        addLabelPair(salaryDetailsGrid, row++, "Tổng thu nhập:", formatCurrency(payroll.getTaxable_income()));

        // Deductions section
        Label deductionTitle = new Label("Khấu trừ");
        deductionTitle.getStyleClass().add("form-label");
        deductionTitle.setStyle("-fx-font-weight: bold;");
        salaryDetailsGrid.add(deductionTitle, 0, row++, 2, 1);

        addLabelPair(salaryDetailsGrid, row++, "Lương đóng BHXH:", formatCurrency(payroll.getSocial_insurance_salary()));
        addLabelPair(salaryDetailsGrid, row++, "BHXH (8%):", formatCurrency(payroll.getInsurance_social()));
        addLabelPair(salaryDetailsGrid, row++, "BHYT (1.5%):", formatCurrency(payroll.getInsurance_health()));
        addLabelPair(salaryDetailsGrid, row++, "BHTN (1%):", formatCurrency(payroll.getInsurance_accident()));
        addLabelPair(salaryDetailsGrid, row++, "Tổng BHXH:", formatCurrency(payroll.getTotal_insurance()));
        addLabelPair(salaryDetailsGrid, row++, "Thuế TNCN:", formatCurrency(payroll.getIncome_tax()));
        addLabelPair(salaryDetailsGrid, row++, "Phạt:", formatCurrency(payroll.getPenalty_amount()));
        addLabelPair(salaryDetailsGrid, row++, "Tổng khấu trừ:", formatCurrency(payroll.getDeductible_income()));

        // Net salary with special styling
        Label netSalaryLabel = new Label("Thực lãnh:");
        netSalaryLabel.getStyleClass().add("form-label");
        netSalaryLabel.setStyle("-fx-font-weight: bold;");

        Label netSalaryValue = new Label(formatCurrency(payroll.getNet_income()));
        netSalaryValue.getStyleClass().add("detail-value");
        netSalaryValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745; -fx-font-size: 14px;");

        HBox netSalaryBox = new HBox(15);
        netSalaryBox.getChildren().addAll(netSalaryLabel, netSalaryValue);

        // Add a separator before the final amount
        Separator separator = new Separator();
        separator.getStyleClass().add("divider");

        // Add all components to the container
        container.getChildren().addAll(
            headerBox,
            new Separator(),
            employeeInfoTitle,
            employeeInfoGrid,
            new Separator(),
            salaryDetailsTitle,
            salaryDetailsGrid,
            separator,
            netSalaryBox
        );
    }
private static void addLabelPair(GridPane grid, int row, String labelText, String valueText) {
    Label label = new Label(labelText);
    label.getStyleClass().add("form-label");
    label.setStyle("-fx-text-fill: black;"); // Set text color to black

    Label value = new Label(valueText);
    value.getStyleClass().add("detail-value");

    grid.add(label, 0, row);
    grid.add(value, 1, row);
}
    private static String formatCurrency(BigDecimal amount) {
        return CurrencyUtils.formatVND(amount);
    }
}