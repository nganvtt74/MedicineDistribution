package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.ManufacturerBUS;
import com.example.medicinedistribution.DTO.ManufacturerDTO;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.GUI.SubAction.ManufacturerAction;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public class ManufacturerController {
    @FXML
    private Button btnAdd;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnRefresh;

    @FXML
    private TableColumn<ManufacturerDTO, Integer> colManufacturerId;

    @FXML
    private TableColumn<ManufacturerDTO, String> colManufacturerName;

    @FXML
    private TableColumn<ManufacturerDTO, String> colCountry;

    @FXML
    private TableColumn<ManufacturerDTO, String> colEmail;

    @FXML
    private TableColumn<ManufacturerDTO, String> colPhone;

    @FXML
    private TableColumn<ManufacturerDTO, String> colAddress;

    @FXML
    private TableColumn<ManufacturerDTO, String> colDescription;

    @FXML
    private TableView<ManufacturerDTO> tblManufacturers;

    @FXML
    private TextField txtSearch;

    private BUSFactory busFactory;
    private ManufacturerBUS manufacturerBUS;
    private ArrayList<ManufacturerDTO> manufacturerList;


    public ManufacturerController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }

    public void initialize() {
        // Initialize the controller
        manufacturerBUS = busFactory.getManufacturerBUS();
        setupUIData();
        setUpUI();
        setUpTable();
        loadData();
    }

    public void setupUIData(){
        if (manufacturerList != null) {
            manufacturerList.clear();
        }
        manufacturerList = new ArrayList<>(manufacturerBUS.findAll());
    }

    public void setUpUI(){
        // Set up the UI components
        btnAdd.setOnAction(event -> {
            ManufacturerAction.showDialog(busFactory, this, ManufacturerAction.ActionType.ADD);
        });

        btnEdit.setOnAction(event -> {
            ManufacturerDTO manufacturerDTO = tblManufacturers.getSelectionModel().getSelectedItem();
            if(manufacturerDTO == null){
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một nhà sản xuất để chỉnh sửa.");
                return;
            }
            ManufacturerAction.showDialog(busFactory, this, ManufacturerAction.ActionType.EDIT, manufacturerDTO);
        });

        btnDelete.setOnAction(event -> {
            if (!NotificationUtil.showConfirmation("Xác nhận", "Bạn có chắc chắn muốn xóa nhà sản xuất này?")) {
                return;
            }
            ManufacturerDTO manufacturerDTO = tblManufacturers.getSelectionModel().getSelectedItem();
            if (manufacturerDTO == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một nhà sản xuất để xóa.");
                return;
            }
            try {
                if (manufacturerBUS.delete(manufacturerDTO.getManufacturerId())) {
                    NotificationUtil.showSuccessNotification("Thành công", "Xóa nhà sản xuất thành công.");
                    setupUIData();
                    loadData();
                }
            } catch (PermissionDeniedException e){
                NotificationUtil.showErrorNotification("Quyền truy cập", e.getMessage());
            } catch (DeleteFailedException e) {
                NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
            } catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
            }
        });

        btnRefresh.setOnAction(event -> {
            setupUIData();
            loadData();
            txtSearch.clear();
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterManufacturers();
        });
    }

    private void filterManufacturers() {
        String searchText = txtSearch.getText().toLowerCase();
        ArrayList<ManufacturerDTO> filteredList = new ArrayList<>();

        for (ManufacturerDTO manufacturer : manufacturerList) {
            if (manufacturer.getManufacturerName().toLowerCase().contains(searchText) ||
                    manufacturer.getEmail().toLowerCase().contains(searchText) ||
                    manufacturer.getPhone().toLowerCase().contains(searchText) ||
                    manufacturer.getCountry().toLowerCase().contains(searchText) ||
                    manufacturer.getAddress().toLowerCase().contains(searchText)) {
                filteredList.add(manufacturer);
            }
        }
        tblManufacturers.setItems(FXCollections.observableArrayList(filteredList));
    }

    public void setUpTable(){
        colManufacturerId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getManufacturerId()));
        colManufacturerName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getManufacturerName()));
        colCountry.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCountry()));
        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        colPhone.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));
        colAddress.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));
        colDescription.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescription()));
    }

    public void loadData(){
        tblManufacturers.getItems().clear();
        tblManufacturers.setItems(FXCollections.observableArrayList(manufacturerList));
    }
}