package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.EmployeeBUS;
import com.example.medicinedistribution.BUS.Interface.RequestBUS;
import com.example.medicinedistribution.BUS.Interface.RequestTypeBUS;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import com.example.medicinedistribution.DTO.RequestTypeDTO;
import com.example.medicinedistribution.DTO.RequestsDTO;
import com.example.medicinedistribution.Util.NotificationUtil;
//import com.example.medicinedistribution.Util.ValidationUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import lombok.extern.slf4j.Slf4j;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class RequestController {
    @FXML private TableView<RequestsDTO> tblRequests;
    @FXML private TableColumn<RequestsDTO, Integer> colId;
    @FXML private TableColumn<RequestsDTO, String> colEmployee;
    @FXML private TableColumn<RequestsDTO, String> colType;
    @FXML private TableColumn<RequestsDTO, LocalDate> colStartDate;
    @FXML private TableColumn<RequestsDTO, LocalDate> colEndDate;
    @FXML private TableColumn<RequestsDTO, Integer> colDuration;
    @FXML private TableColumn<RequestsDTO, String> colStatus;
    @FXML private TableColumn<RequestsDTO, LocalDate> colCreatedDate;
    @FXML private TableColumn<RequestsDTO, Void> colActions;

    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private ComboBox<String> cbStatus;
    @FXML private ComboBox<RequestTypeDTO> cbRequestType1;
    @FXML private Button btnFilter;
    @FXML private Button btnReset;
    @FXML private Button btnCreateRequest;

    @FXML private Label lblTotalRequests;
    @FXML private Label lblPendingRequests;
    @FXML private Label lblApprovedRequests;
    @FXML private Label lblRejectedRequests;

    private final BUSFactory busFactory;
    private final RequestBUS requestBUS;
    private final RequestTypeBUS requestTypeBUS;
    private final EmployeeBUS employeeBUS;

    private ObservableList<RequestsDTO> requestsList = FXCollections.observableArrayList();
    private Map<Integer, EmployeeDTO> employeeCache = new HashMap<>();
    private Map<Integer, RequestTypeDTO> requestTypeCache = new HashMap<>();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public RequestController(BUSFactory busFactory) {
        this.busFactory = busFactory;
        this.requestBUS = busFactory.getRequestBUS();
        this.requestTypeBUS = busFactory.getRequestTypeBUS();
        this.employeeBUS = busFactory.getEmployeeBUS();
    }

    @FXML
    public void initialize() {
        // Khởi tạo các bộ lọc
        setupFilters();

        // Cấu hình bảng hiển thị
        setupTable();

        // Nạp dữ liệu ban đầu
        loadRequestData();

        // Cập nhật thống kê
        updateStatistics();

        // Sự kiện nút lọc
        btnFilter.setOnAction(event -> filterRequests());

        // Sự kiện nút đặt lại
        btnReset.setOnAction(event -> resetFilters());

        // Sự kiện nút tạo yêu cầu mới (chỉ dành cho admin)
        btnCreateRequest.setVisible(false);
    }

    private void setupFilters() {
        // Cấu hình DatePicker
        dpFromDate.setValue(LocalDate.now().minusMonths(1));
        dpToDate.setValue(LocalDate.now());

        // Cấu hình ComboBox trạng thái
        cbStatus.getItems().addAll("Tất cả", "PENDING", "APPROVED", "REJECTED");
        cbStatus.setValue("Tất cả");

        // Cấu hình ComboBox loại yêu cầu
        List<RequestTypeDTO> requestTypes = requestTypeBUS.findAll();
        requestTypes.forEach(type -> requestTypeCache.put(type.getType_id(), type));

        cbRequestType1.getItems().add(null); // Tùy chọn "Tất cả"
        cbRequestType1.getItems().addAll(requestTypes);

        // Hiển thị tên loại yêu cầu trong ComboBox
        cbRequestType1.setCellFactory(param -> new ListCell<RequestTypeDTO>() {
            @Override
            protected void updateItem(RequestTypeDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Tất cả");
                } else {
                    setText(item.getType_name());
                }
            }
        });

        cbRequestType1.setButtonCell(new ListCell<RequestTypeDTO>() {
            @Override
            protected void updateItem(RequestTypeDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Tất cả");
                } else {
                    setText(item.getType_name());
                }
            }
        });

        cbRequestType1.setValue(null);
    }

    private void setupTable() {
        // Cấu hình các cột trong bảng
        colId.setCellValueFactory(new PropertyValueFactory<>("request_id"));

        colEmployee.setCellValueFactory(cellData -> {
            Integer employeeId = cellData.getValue().getEmployee_id();
            String employeeName = getEmployeeName(employeeId);
            return new SimpleStringProperty(employeeName);
        });

        colType.setCellValueFactory(cellData -> {
            Integer typeId = cellData.getValue().getType_id();
            String typeName = getRequestTypeName(typeId);
            return new SimpleStringProperty(typeName);
        });

        colStartDate.setCellValueFactory(new PropertyValueFactory<>("start_date"));
        colStartDate.setCellFactory(tc -> new TableCell<RequestsDTO, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });

        colEndDate.setCellValueFactory(new PropertyValueFactory<>("end_date"));
        colEndDate.setCellFactory(tc -> new TableCell<RequestsDTO, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });

        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));

        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<RequestsDTO, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(getStatusText(status));
                    setStyle(getStatusStyle(status));
                }
            }
        });

        colCreatedDate.setCellValueFactory(new PropertyValueFactory<>("created_at"));
        colCreatedDate.setCellFactory(tc -> new TableCell<RequestsDTO, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });

        // Cấu hình cột thao tác
        colActions.setCellFactory(param -> new TableCell<RequestsDTO, Void>() {
        private final Button btnView = new Button();
        private final Button btnApprove = new Button();
        private final Button btnReject = new Button();

        private final HBox pane = new HBox(5);
        {
            // Import FontAwesome icons
            FontAwesomeIconView viewIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE);
            viewIcon.setFill(Color.web("#5050a0"));
            viewIcon.setSize("16");
            btnView.setGraphic(viewIcon);

            FontAwesomeIconView approveIcon = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
            approveIcon.setFill(Color.web("#50a050"));
            approveIcon.setSize("16");
            btnApprove.setGraphic(approveIcon);

            FontAwesomeIconView rejectIcon = new FontAwesomeIconView(FontAwesomeIcon.TIMES_CIRCLE);
            rejectIcon.setFill(Color.web("#a05050"));
            rejectIcon.setSize("16");
            btnReject.setGraphic(rejectIcon);

            // Set common button style class
            btnView.getStyleClass().add("icon-button");
            btnApprove.getStyleClass().add("icon-button");
            btnReject.getStyleClass().add("icon-button");

            // Add tooltips for better usability
            btnView.setTooltip(new Tooltip("Xem chi tiết"));
            btnApprove.setTooltip(new Tooltip("Duyệt yêu cầu"));
            btnReject.setTooltip(new Tooltip("Từ chối yêu cầu"));

            // Set button size
            btnView.setPrefSize(32, 32);
            btnApprove.setPrefSize(32, 32);
            btnReject.setPrefSize(32, 32);

            pane.setAlignment(Pos.CENTER);
            pane.getChildren().addAll(btnView, btnApprove, btnReject);

            // Add event handlers
            btnView.setOnAction(event -> {
                RequestsDTO request = getTableView().getItems().get(getIndex());
                showRequestDetails(request);
            });

            btnApprove.setOnAction(event -> {
                RequestsDTO request = getTableView().getItems().get(getIndex());
                approveRequest(request);
            });

            btnReject.setOnAction(event -> {
                RequestsDTO request = getTableView().getItems().get(getIndex());
                rejectRequest(request);
            });
        }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    RequestsDTO request = getTableView().getItems().get(getIndex());

                    // Chỉ hiển thị nút phù hợp với trạng thái hiện tại
                    pane.getChildren().clear();
                    pane.getChildren().add(btnView);

                    if ("PENDING".equals(request.getStatus())) {
                        pane.getChildren().addAll(btnApprove, btnReject);
                    }

                    setGraphic(pane);
                }
            }
        });

        tblRequests.setItems(requestsList);
    }

    private void loadRequestData() {
        try {
            // Nạp dữ liệu yêu cầu từ cơ sở dữ liệu
            List<RequestsDTO> requests = requestBUS.findAll();

            // Nạp thông tin nhân viên và lưu vào cache
            List<Integer> employeeIds = requests.stream()
                    .map(RequestsDTO::getEmployee_id)
                    .distinct()
                    .collect(Collectors.toList());

            for (Integer employeeId : employeeIds) {
                EmployeeDTO employee = employeeBUS.findById(employeeId);
                if (employee != null) {
                    employeeCache.put(employeeId, employee);
                }
            }

            // Cập nhật danh sách hiển thị
            requestsList.clear();
            requestsList.addAll(requests);

            // Cập nhật thống kê
            updateStatistics();

        } catch (Exception e) {
            log.error("Không thể tải dữ liệu yêu cầu", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải dữ liệu yêu cầu: " + e.getMessage());
        }
    }

    private String getEmployeeName(Integer employeeId) {
        if (employeeId == null) return "N/A";

        // Kiểm tra trong cache
        if (employeeCache.containsKey(employeeId)) {
            EmployeeDTO employee = employeeCache.get(employeeId);
            return employee.getFirstName() + " " + employee.getLastName();
        }

        // Nếu không có trong cache, lấy từ cơ sở dữ liệu
        try {
            EmployeeDTO employee = employeeBUS.findById(employeeId);
            if (employee != null) {
                employeeCache.put(employeeId, employee);
                return employee.getFirstName() + " " + employee.getLastName();
            }
        } catch (Exception e) {
            log.error("Không thể lấy thông tin nhân viên: {}", employeeId, e);
        }

        return "ID: " + employeeId;
    }

    private String getRequestTypeName(Integer typeId) {
        if (typeId == null) return "N/A";

        // Kiểm tra trong cache
        if (requestTypeCache.containsKey(typeId)) {
            return requestTypeCache.get(typeId).getType_name();
        }

        // Nếu không có trong cache, lấy từ cơ sở dữ liệu
        try {
            RequestTypeDTO type = requestTypeBUS.findById(typeId);
            if (type != null) {
                requestTypeCache.put(typeId, type);
                return type.getType_name();
            }
        } catch (Exception e) {
            log.error("Không thể lấy thông tin loại yêu cầu: {}", typeId, e);
        }

        return "ID: " + typeId;
    }

    private String getStatusText(String status) {
        if (status == null) return "";

        switch (status) {
            case "PENDING": return "Chờ duyệt";
            case "APPROVED": return "Đã duyệt";
            case "REJECTED": return "Từ chối";
            default: return status;
        }
    }

    private String getStatusStyle(String status) {
        if (status == null) return "";

        switch (status) {
            case "PENDING": return "-fx-text-fill: #ff8c00; -fx-font-weight: bold;";
            case "APPROVED": return "-fx-text-fill: #008000; -fx-font-weight: bold;";
            case "REJECTED": return "-fx-text-fill: #ff0000; -fx-font-weight: bold;";
            default: return "";
        }
    }

    private void updateStatistics() {
        int totalCount = requestsList.size();
        int pendingCount = (int) requestsList.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        int approvedCount = (int) requestsList.stream().filter(r -> "APPROVED".equals(r.getStatus())).count();
        int rejectedCount = (int) requestsList.stream().filter(r -> "REJECTED".equals(r.getStatus())).count();

        lblTotalRequests.setText(String.valueOf(totalCount));
        lblPendingRequests.setText(String.valueOf(pendingCount));
        lblApprovedRequests.setText(String.valueOf(approvedCount));
        lblRejectedRequests.setText(String.valueOf(rejectedCount));
    }

    private void filterRequests() {
        try {
            // Lấy giá trị từ các bộ lọc
            LocalDate fromDate = dpFromDate.getValue();
            LocalDate toDate = dpToDate.getValue();
            String status = "Tất cả".equals(cbStatus.getValue()) ? null : cbStatus.getValue();
            Integer typeId = cbRequestType1.getValue() != null ? cbRequestType1.getValue().getType_id() : null;

            // Kiểm tra điều kiện lọc hợp lệ
            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                NotificationUtil.showErrorNotification("Lỗi", "Ngày bắt đầu không thể sau ngày kết thúc");
                return;
            }

            // Thực hiện lọc từ cơ sở dữ liệu
            List<RequestsDTO> filteredRequests = requestBUS.findByFilters(fromDate, toDate, status, typeId);

            // Cập nhật danh sách hiển thị
            requestsList.clear();
            requestsList.addAll(filteredRequests);

            // Cập nhật thống kê
            updateStatistics();

        } catch (Exception e) {
            log.error("Lỗi khi lọc yêu cầu", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể lọc yêu cầu: " + e.getMessage());
        }
    }

    private void resetFilters() {
        // Đặt lại giá trị cho các bộ lọc
        dpFromDate.setValue(LocalDate.now().minusMonths(1));
        dpToDate.setValue(LocalDate.now());
        cbStatus.setValue("Tất cả");
        cbRequestType1.setValue(null);

        // Nạp lại dữ liệu
        loadRequestData();
    }

    private void showRequestDetails(RequestsDTO request) {
        try {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Chi tiết yêu cầu");
            dialog.setHeaderText(null);
            Image icon = new Image(getClass().getResource("../../../../img/logo.png").toExternalForm());
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(icon);

            // Tạo nội dung dialog
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStyleClass().add("details-dialog");
            dialogPane.getStylesheets().add(getClass().getResource("../../../../css/main-style.css").toExternalForm());
            dialogPane.setPrefWidth(500);

            // Header của dialog với style gradient
            HBox headerBox = new HBox();
            headerBox.getStyleClass().add("details-header-box");
            Label headerLabel = new Label("Thông tin yêu cầu #" + request.getRequest_id());
            headerLabel.getStyleClass().add("details-header");
            headerBox.getChildren().add(headerLabel);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            // Tạo dữ liệu hiển thị chi tiết
            GridPane grid = new GridPane();
            grid.getStyleClass().add("details-content");
            grid.setHgap(15);
            grid.setVgap(12);
            grid.setPadding(new javafx.geometry.Insets(20, 15, 15, 15));

            // Nhân viên
            int row = 0;
            String employeeName = getEmployeeName(request.getEmployee_id());

            Label employeeKeyLabel = new Label("Nhân viên:");
            employeeKeyLabel.getStyleClass().add("detail-key");
            Label employeeValueLabel = new Label(employeeName);
            employeeValueLabel.getStyleClass().add("detail-value");
            grid.add(employeeKeyLabel, 0, row);
            grid.add(employeeValueLabel, 1, row++);

            // Loại yêu cầu
            Label typeKeyLabel = new Label("Loại yêu cầu:");
            typeKeyLabel.getStyleClass().add("detail-key");
            Label typeValueLabel = new Label(getRequestTypeName(request.getType_id()));
            typeValueLabel.getStyleClass().add("detail-value");
            grid.add(typeKeyLabel, 0, row);
            grid.add(typeValueLabel, 1, row++);

            // Thời gian
            Label startDateKeyLabel = new Label("Ngày bắt đầu:");
            startDateKeyLabel.getStyleClass().add("detail-key");
            Label startDateValueLabel = new Label(request.getStart_date().format(dateFormatter));
            startDateValueLabel.getStyleClass().add("detail-value");
            grid.add(startDateKeyLabel, 0, row);
            grid.add(startDateValueLabel, 1, row++);

            Label endDateKeyLabel = new Label("Ngày kết thúc:");
            endDateKeyLabel.getStyleClass().add("detail-key");
            Label endDateValueLabel = new Label(request.getEnd_date().format(dateFormatter));
            endDateValueLabel.getStyleClass().add("detail-value");
            grid.add(endDateKeyLabel, 0, row);
            grid.add(endDateValueLabel, 1, row++);

            Label durationKeyLabel = new Label("Thời gian:");
            durationKeyLabel.getStyleClass().add("detail-key");
            Label durationValueLabel = new Label(request.getDuration() + " ngày");
            durationValueLabel.getStyleClass().add("detail-value");
            grid.add(durationKeyLabel, 0, row);
            grid.add(durationValueLabel, 1, row++);

            // Trạng thái
            Label statusKeyLabel = new Label("Trạng thái:");
            statusKeyLabel.getStyleClass().add("detail-key");
            Label statusValueLabel = new Label(getStatusText(request.getStatus()));

            // Thêm style cho trạng thái
            String statusStyleClass = switch (request.getStatus()) {
                case "APPROVED" -> "status-active";
                case "REJECTED" -> "status-inactive";
                default -> "status-pending";
            };
            statusValueLabel.getStyleClass().add(statusStyleClass);

            grid.add(statusKeyLabel, 0, row);
            grid.add(statusValueLabel, 1, row++);

            // Thông tin tạo
            Label createdKeyLabel = new Label("Ngày tạo:");
            createdKeyLabel.getStyleClass().add("detail-key");
            Label createdValueLabel = new Label(request.getCreated_at().format(dateFormatter));
            createdValueLabel.getStyleClass().add("detail-value");
            grid.add(createdKeyLabel, 0, row);
            grid.add(createdValueLabel, 1, row++);

            // Thông tin duyệt (nếu có)
            if (request.getApproved_at() != null) {
                Label approvedDateKeyLabel = new Label("Ngày duyệt:");
                approvedDateKeyLabel.getStyleClass().add("detail-key");
                Label approvedDateValueLabel = new Label(request.getApproved_at().format(dateFormatter));
                approvedDateValueLabel.getStyleClass().add("detail-value");
                grid.add(approvedDateKeyLabel, 0, row);
                grid.add(approvedDateValueLabel, 1, row++);

                if (request.getApproved_by() != null) {
                    Label approverKeyLabel = new Label("Người duyệt:");
                    approverKeyLabel.getStyleClass().add("detail-key");
                    Label approverValueLabel = new Label(getEmployeeName(request.getApproved_by()));
                    approverValueLabel.getStyleClass().add("detail-value");
                    grid.add(approverKeyLabel, 0, row);
                    grid.add(approverValueLabel, 1, row++);
                }
            }

            // Thêm separator trước phần lý do
            Separator separator = new Separator();
            separator.getStyleClass().add("divider");
            grid.add(separator, 0, row++, 2, 1);

            // Lý do
            Label reasonKeyLabel = new Label("Lý do:");
            reasonKeyLabel.getStyleClass().add("detail-key");
            grid.add(reasonKeyLabel, 0, row++);

            TextArea reasonArea = new TextArea(request.getReason());
            reasonArea.getStyleClass().add("detail-value");
            reasonArea.setEditable(false);
            reasonArea.setWrapText(true);
            reasonArea.setPrefRowCount(4);
            reasonArea.setPrefWidth(300);
            reasonArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6;");
            grid.add(reasonArea, 0, row++, 2, 1);

            // Tạo scroll pane để chứa grid khi nội dung dài
            ScrollPane scrollPane = new ScrollPane(grid);
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("details-scroll-pane");

            // Tạo VBox để chứa header và nội dung
            VBox contentBox = new VBox();
            contentBox.getChildren().addAll(headerBox, scrollPane);

            dialogPane.setContent(contentBox);

            // Thêm nút đóng với style
            dialogPane.getButtonTypes().clear();
            dialogPane.getButtonTypes().add(ButtonType.CLOSE);

            Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
            closeButton.setText("Đóng");
            closeButton.getStyleClass().add("primary-button");
            closeButton.setPrefWidth(100);

            // Hiển thị dialog
            dialog.showAndWait();

        } catch (Exception e) {
            log.error("Lỗi khi hiển thị chi tiết yêu cầu", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể hiển thị chi tiết yêu cầu: " + e.getMessage());
        }
    }
private void approveRequest(RequestsDTO request) {
    try {
        // Kiểm tra nếu yêu cầu không phải đang ở trạng thái chờ duyệt
        if (!"PENDING".equals(request.getStatus())) {
            NotificationUtil.showErrorNotification("Lỗi", "Chỉ có thể duyệt các yêu cầu đang chờ");
            return;
        }

        // Tạo dialog xác nhận với style
        Dialog<ButtonType> confirmDialog = new Dialog<>();
        confirmDialog.setTitle("Xác nhận duyệt yêu cầu");
        confirmDialog.setHeaderText(null);

        // Set icon
        Image icon = new Image(getClass().getResource("../../../../img/logo.png").toExternalForm());
        Stage stage = (Stage) confirmDialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(icon);

        // Style dialog
        DialogPane dialogPane = confirmDialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("../../../../css/main-style.css").toExternalForm());
        dialogPane.getStyleClass().add("details-dialog");

        // Header với icon
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new javafx.geometry.Insets(10, 10, 15, 10));

        FontAwesomeIconView confirmIcon = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
        confirmIcon.setFill(Color.web("#50a050"));
        confirmIcon.setSize("24");

        Label headerLabel = new Label("Xác nhận duyệt yêu cầu");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        headerBox.getChildren().addAll(confirmIcon, headerLabel);

        // Content
        VBox content = new VBox(15);
        content.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));

        // Thông tin yêu cầu
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));
        grid.getStyleClass().add("details-content");

        // Thông tin nhân viên
        Label employeeLabel = new Label("Nhân viên:");
        employeeLabel.getStyleClass().add("detail-key");
        Label employeeValue = new Label(getEmployeeName(request.getEmployee_id()));
        employeeValue.getStyleClass().add("detail-value");
        grid.add(employeeLabel, 0, 0);
        grid.add(employeeValue, 1, 0);

        // Loại yêu cầu
        Label typeLabel = new Label("Loại yêu cầu:");
        typeLabel.getStyleClass().add("detail-key");
        Label typeValue = new Label(getRequestTypeName(request.getType_id()));
        typeValue.getStyleClass().add("detail-value");
        grid.add(typeLabel, 0, 1);
        grid.add(typeValue, 1, 1);

        // Thời gian
        Label periodLabel = new Label("Thời gian:");
        periodLabel.getStyleClass().add("detail-key");
        Label periodValue = new Label(
                request.getStart_date().format(dateFormatter) +
                " đến " +
                request.getEnd_date().format(dateFormatter) +
                " (" + request.getDuration() + " ngày)");
        periodValue.getStyleClass().add("detail-value");
        grid.add(periodLabel, 0, 2);
        grid.add(periodValue, 1, 2);

        // Thông báo xác nhận
        Label confirmMessage = new Label("Bạn có chắc chắn muốn duyệt yêu cầu này không?");
        confirmMessage.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057; -fx-font-size: 14px;");

        content.getChildren().addAll(grid, new Separator(), confirmMessage);

        // Set content
        VBox mainContent = new VBox(headerBox, content);
        dialogPane.setContent(mainContent);

        // Buttons
        ButtonType buttonApprove = new ButtonType("Duyệt", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getDialogPane().getButtonTypes().setAll(buttonApprove, buttonCancel);

        // Style buttons
        Button approveButton = (Button) dialogPane.lookupButton(buttonApprove);
        approveButton.getStyleClass().add("status-active");
        approveButton.setPrefWidth(100);

        Button cancelButton = (Button) dialogPane.lookupButton(buttonCancel);
        cancelButton.getStyleClass().add("primary-button");
        cancelButton.setPrefWidth(100);

        // Show dialog and handle result
        if (confirmDialog.showAndWait().orElse(buttonCancel) == buttonApprove) {
            // Cập nhật trạng thái yêu cầu
            request.setStatus("APPROVED");
            request.setApproved_at(LocalDate.now());
            request.setApproved_by(busFactory.getUserSession().getEmployee().getEmployeeId());

            // Lưu vào cơ sở dữ liệu
            boolean success = requestBUS.update(request);

            if (success) {
                NotificationUtil.showSuccessNotification("Thành công", "Đã duyệt yêu cầu thành công");

                // Cập nhật hiển thị
                int index = requestsList.indexOf(request);
                requestsList.set(index, request);
                tblRequests.refresh();

                // Cập nhật thống kê
                updateStatistics();
            } else {
                NotificationUtil.showErrorNotification("Lỗi", "Không thể duyệt yêu cầu");
            }
        }
    } catch (Exception e) {
        log.error("Lỗi khi duyệt yêu cầu", e);
        NotificationUtil.showErrorNotification("Lỗi", "Không thể duyệt yêu cầu: " + e.getMessage());
    }
}
  private void rejectRequest(RequestsDTO request) {
        try {
            // Kiểm tra nếu yêu cầu không phải đang ở trạng thái chờ duyệt
            if (!"PENDING".equals(request.getStatus())) {
                NotificationUtil.showErrorNotification("Lỗi", "Chỉ có thể từ chối các yêu cầu đang chờ");
                return;
            }

            // Tạo dialog xác nhận với style
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Từ chối yêu cầu");
            dialog.setHeaderText(null);

            // Set icon
            Image icon = new Image(getClass().getResource("../../../../img/logo.png").toExternalForm());
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(icon);

            // Style dialog
            DialogPane dialogPane = dialog.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("../../../../css/main-style.css").toExternalForm());
            dialogPane.getStyleClass().add("details-dialog");

            // Header với icon
            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setPadding(new javafx.geometry.Insets(10, 10, 15, 10));

            FontAwesomeIconView rejectIcon = new FontAwesomeIconView(FontAwesomeIcon.TIMES_CIRCLE);
            rejectIcon.setFill(Color.web("#a05050"));
            rejectIcon.setSize("24");

            Label headerLabel = new Label("Từ chối yêu cầu");
            headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            headerBox.getChildren().addAll(rejectIcon, headerLabel);

            // Content
            VBox content = new VBox(15);
            content.setPadding(new javafx.geometry.Insets(10, 10, 10, 10));

            // Thông tin yêu cầu
            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(10));
            grid.getStyleClass().add("details-content");

            // Thông tin nhân viên
            int row = 0;
            Label employeeLabel = new Label("Nhân viên:");
            employeeLabel.getStyleClass().add("detail-key");
            Label employeeValue = new Label(getEmployeeName(request.getEmployee_id()));
            employeeValue.getStyleClass().add("detail-value");
            grid.add(employeeLabel, 0, row);
            grid.add(employeeValue, 1, row++);

            // Loại yêu cầu
            Label typeLabel = new Label("Loại yêu cầu:");
            typeLabel.getStyleClass().add("detail-key");
            Label typeValue = new Label(getRequestTypeName(request.getType_id()));
            typeValue.getStyleClass().add("detail-value");
            grid.add(typeLabel, 0, row);
            grid.add(typeValue, 1, row++);

            // Thời gian
            Label periodLabel = new Label("Thời gian:");
            periodLabel.getStyleClass().add("detail-key");
            Label periodValue = new Label(
                    request.getStart_date().format(dateFormatter) +
                    " đến " +
                    request.getEnd_date().format(dateFormatter) +
                    " (" + request.getDuration() + " ngày)");
            periodValue.getStyleClass().add("detail-value");
            grid.add(periodLabel, 0, row);
            grid.add(periodValue, 1, row++);

            // Separator
            Separator separator = new Separator();
            separator.getStyleClass().add("divider");

            // Nhập lý do từ chối
            Label reasonLabel = new Label("Lý do từ chối:");
            reasonLabel.getStyleClass().add("detail-key");
            reasonLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");

            TextArea reasonField = new TextArea();
            reasonField.setWrapText(true);
            reasonField.setPrefRowCount(4);
            reasonField.setPrefWidth(350);
            reasonField.setPromptText("Vui lòng nhập lý do từ chối yêu cầu này...");
            reasonField.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6;");

            // Add components to content
            content.getChildren().addAll(grid, separator, reasonLabel, reasonField);

            // Set content
            VBox mainContent = new VBox(headerBox, content);
            dialogPane.setContent(mainContent);

            // Buttons
            ButtonType buttonReject = new ButtonType("Từ chối", ButtonBar.ButtonData.OK_DONE);
            ButtonType buttonCancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().setAll(buttonReject, buttonCancel);

            // Style buttons
            Button rejectButton = (Button) dialogPane.lookupButton(buttonReject);
            rejectButton.getStyleClass().add("status-inactive");
            rejectButton.setPrefWidth(100);

            Button cancelButton = (Button) dialogPane.lookupButton(buttonCancel);
            cancelButton.getStyleClass().add("primary-button");
            cancelButton.setPrefWidth(100);

            // Set result converter
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == buttonReject) {
                    return reasonField.getText();
                }
                return null;
            });

            // Show dialog and handle result
            dialog.showAndWait().ifPresent(reason -> {
                if (reason == null || reason.trim().isEmpty()) {
                    NotificationUtil.showErrorNotification("Lỗi", "Vui lòng nhập lý do từ chối");
                    return;
                }

                try {
                    // Cập nhật trạng thái yêu cầu
                    request.setStatus("REJECTED");
                    request.setApproved_at(LocalDate.now());
                    request.setApproved_by(busFactory.getUserSession().getEmployee().getEmployeeId());

                    // TODO: Lưu lý do từ chối vào trường ghi chú hoặc lịch sử (nếu có)

                    // Lưu vào cơ sở dữ liệu
                    boolean success = requestBUS.update(request);

                    if (success) {
                        NotificationUtil.showSuccessNotification("Thành công", "Đã từ chối yêu cầu thành công");

                        // Cập nhật hiển thị
                        int index = requestsList.indexOf(request);
                        requestsList.set(index, request);
                        tblRequests.refresh();

                        // Cập nhật thống kê
                        updateStatistics();
                    } else {
                        NotificationUtil.showErrorNotification("Lỗi", "Không thể từ chối yêu cầu");
                    }
                } catch (Exception e) {
                    log.error("Lỗi khi từ chối yêu cầu", e);
                    NotificationUtil.showErrorNotification("Lỗi", "Không thể từ chối yêu cầu: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Lỗi khi từ chối yêu cầu", e);
            NotificationUtil.showErrorNotification("Lỗi", "Không thể từ chối yêu cầu: " + e.getMessage());
        }
    }
}