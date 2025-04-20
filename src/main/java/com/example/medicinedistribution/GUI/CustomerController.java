package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.CustomerBUS;
import com.example.medicinedistribution.DTO.CustomerDTO;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.GUI.SubAction.CustomerAction;
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

public class CustomerController {
    @FXML
    private Button btnAdd;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnRefresh;

    @FXML
    private TableColumn<CustomerDTO,String> colAddress;

    @FXML
    private TableColumn<CustomerDTO,Integer> colCustomerId;

    @FXML
    private TableColumn<CustomerDTO,String> colCustomerName;

    @FXML
    private TableColumn<CustomerDTO,String> colEmail;

    @FXML
    private TableColumn<CustomerDTO, String> colPhone;

    @FXML
    private TableView<CustomerDTO> tblCustomers;

    @FXML
    private TextField txtSearch;



    private BUSFactory busFactory;
    private CustomerBUS customerBUS;
    private ArrayList<CustomerDTO> customerList;


    public CustomerController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }

    public void initialize() {
        // Initialize the controller
        customerBUS = busFactory.getCustomerBUS();
        setupUIData();
        setUpUI();
        setUpTable();
        loadData();

    }

    public void setupUIData(){

        if (customerList != null) {
            customerList.clear();
        }
        customerList = new ArrayList<>(customerBUS.findAll());

    }
    public void setUpUI(){
        // Set up the UI components
        btnAdd.setOnAction(event -> {
            CustomerAction.showDialog(busFactory,this, CustomerAction.ActionType.ADD);
        });

        btnEdit.setOnAction(event -> {
            CustomerDTO customerDTO = tblCustomers.getSelectionModel().getSelectedItem();
            if(customerDTO == null){
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một khách hàng để chỉnh sửa.");
                    return;
            }
            CustomerAction.showDialog(busFactory,this, CustomerAction.ActionType.EDIT, customerDTO);

        });

        btnDelete.setOnAction(event -> {
            if (!NotificationUtil.showConfirmation("Xác nhận", "Bạn có chắc chắn muốn xóa khách hàng này?")) {
                return;
            }
            CustomerDTO customerDTO = tblCustomers.getSelectionModel().getSelectedItem();
            if (customerDTO == null) {
                NotificationUtil.showErrorNotification("Lỗi", "Vui lòng chọn một khách hàng để xóa.");
                return;
            }
            try {
                if (customerBUS.delete(customerDTO.getCustomerId())) {
                    NotificationUtil.showSuccessNotification("Thành công", "Xóa khách hàng thành công.");
                    setupUIData();
                    loadData();
                }
            }catch (PermissionDeniedException e){
                NotificationUtil.showErrorNotification("Quyền truy cập", e.getMessage());
            }
            catch (DeleteFailedException e) {
                NotificationUtil.showErrorNotification("Lỗi", e.getMessage());
            }
            catch (Exception e) {
                NotificationUtil.showErrorNotification("Lỗi", "Có lỗi không xác định: " + e.getMessage());
            }
        });

        btnRefresh.setOnAction(event -> {
            setupUIData();
            loadData();
            txtSearch.clear();
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCustomers();
        });

    }

    private void filterCustomers() {
        String searchText = txtSearch.getText().toLowerCase();
        ArrayList<CustomerDTO> filteredList = new ArrayList<>();

        for (CustomerDTO customer : customerList) {
            if (customer.getCustomerName().toLowerCase().contains(searchText) ||
                    customer.getEmail().toLowerCase().contains(searchText) ||
                    customer.getPhone().toLowerCase().contains(searchText) ||
                    customer.getAddress().toLowerCase().contains(searchText)) {
                filteredList.add(customer);
            }
        }
        tblCustomers.setItems(FXCollections.observableArrayList(filteredList));
    }

    public void setUpTable(){
        // Set up the UI data
        colCustomerId.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getCustomerId()));
        colCustomerName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
        colEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        colPhone.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));
        colAddress.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAddress()));

    }

    public void loadData(){
        tblCustomers.getItems().clear();
        tblCustomers.setItems(FXCollections.observableArrayList(customerList));

    }



}
