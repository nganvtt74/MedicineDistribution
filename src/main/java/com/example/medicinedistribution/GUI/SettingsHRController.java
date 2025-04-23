package com.example.medicinedistribution.GUI;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.Interface.BonusTypeBUS;
import com.example.medicinedistribution.DTO.BonusTypeDTO;
import com.example.medicinedistribution.GUI.BonusTypeManage;
import com.example.medicinedistribution.Util.JsonUtil;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
public class SettingsHRController {
    // Tax rate table
    @FXML private TableView<TaxRateEntry> tblTaxRate;
    @FXML private TableColumn<TaxRateEntry, String> colTaxMin;
    @FXML private TableColumn<TaxRateEntry, String> colTaxMax;
    @FXML private TableColumn<TaxRateEntry, String> colTaxRate;
    @FXML private TableColumn<TaxRateEntry, String> colFixedDeduction;
    @FXML private Button btnEditTaxRate;

    // Insurance table
    @FXML private TableView<InsuranceEntry> tblInsurance;
    @FXML private TableColumn<InsuranceEntry, String> colInsuranceId;
    @FXML private TableColumn<InsuranceEntry, String> colInsuranceDescription;
    @FXML private TableColumn<InsuranceEntry, String> colInsurancePercentage;
    @FXML private Button btnEditInsurance;

    // Bonus type table
    @FXML private TableView<BonusTypeDTO> tblBonusType;
    @FXML private TableColumn<BonusTypeDTO, Integer> colBonusTypeId;
    @FXML private TableColumn<BonusTypeDTO, String> colBonusTypeName;
    @FXML private TableColumn<BonusTypeDTO, String> colBonusTypeDescription;
    @FXML private Button btnManageBonusTypes;

    private final BUSFactory busFactory;
    private BonusTypeBUS bonusTypeBUS;
    private List<BonusTypeDTO> bonusTypeList;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DecimalFormat percentFormat = new DecimalFormat("#0.0'%'");

    // Model classes for table data
    public static class TaxRateEntry {
        private final BigDecimal min;
        private final BigDecimal max;
        private final double taxRate;
        private final BigDecimal fixedDeduction;

        public TaxRateEntry(BigDecimal min, BigDecimal max, double taxRate, BigDecimal fixedDeduction) {
            this.min = min;
            this.max = max;
            this.taxRate = taxRate;
            this.fixedDeduction = fixedDeduction;
        }

        public BigDecimal getMin() {
            return min;
        }

        public BigDecimal getMax() {
            return max;
        }

        public double getTaxRate() {
            return taxRate;
        }

        public BigDecimal getFixedDeduction() {
            return fixedDeduction;
        }
    }

    public static class InsuranceEntry {
        private final String insuranceId;
        private final double percentage;
        private final String description;

        public InsuranceEntry(String insuranceId, double percentage) {
            this.insuranceId = insuranceId;
            this.percentage = percentage;

            // Set description based on insurance ID
            switch (insuranceId) {
                case "BHXH":
                    this.description = "Bảo hiểm xã hội";
                    break;
                case "BHYT":
                    this.description = "Bảo hiểm y tế";
                    break;
                case "BHTN":
                    this.description = "Bảo hiểm thất nghiệp";
                    break;
                default:
                    this.description = "";
            }
        }

        public String getInsuranceId() {
            return insuranceId;
        }

        public double getPercentage() {
            return percentage;
        }

        public String getDescription() {
            return description;
        }
    }

    public SettingsHRController(BUSFactory busFactory) {
        this.busFactory = busFactory;
    }

    @FXML
    public void initialize() {
        bonusTypeBUS = busFactory.getBonusTypeBUS();

        setupTaxRateTable();
        setupInsuranceTable();
        setupBonusTypeTable();

        loadTaxRates();
        loadInsuranceRates();
        loadBonusTypes();

        setupButtonHandlers();
    }

    private void setupTaxRateTable() {
        colTaxMin.setCellValueFactory(cellData -> {
            BigDecimal min = cellData.getValue().getMin();
            return new SimpleStringProperty(formatCurrency(min));
        });

        colTaxMax.setCellValueFactory(cellData -> {
            BigDecimal max = cellData.getValue().getMax();
            return max == null ?
                new SimpleStringProperty("Không giới hạn") :
                new SimpleStringProperty(formatCurrency(max));
        });

        colTaxRate.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getTaxRate() + "%"));

        colFixedDeduction.setCellValueFactory(cellData ->
            new SimpleStringProperty(formatCurrency(cellData.getValue().getFixedDeduction())));
    }

    private void setupInsuranceTable() {
        colInsuranceId.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getInsuranceId()));

        colInsuranceDescription.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDescription()));

        colInsurancePercentage.setCellValueFactory(cellData ->
            new SimpleStringProperty(percentFormat.format(cellData.getValue().getPercentage())));
    }

    private void setupBonusTypeTable() {
        colBonusTypeId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBonusTypeName.setCellValueFactory(new PropertyValueFactory<>("name"));
    }


    private void loadTaxRates() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode taxRatesNode = JsonUtil.readJsonFromResource("../../../../config/tax_rate.json");

            List<TaxRateEntry> taxRates = new ArrayList<>();

            for (JsonNode node : taxRatesNode) {
                BigDecimal min = new BigDecimal(node.get("min").asText());
                JsonNode maxNode = node.get("max");
                BigDecimal max = maxNode.isNull() ? null : new BigDecimal(maxNode.asText());
                double taxRate = node.get("tax_rate").asDouble();
                BigDecimal fixedDeduction = new BigDecimal(node.get("fixed_deduction").asText());

                taxRates.add(new TaxRateEntry(min, max, taxRate, fixedDeduction));
            }

            tblTaxRate.setItems(FXCollections.observableArrayList(taxRates));

        } catch (IOException e) {
            log.error("Error loading tax rates: {}", e.getMessage());
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải dữ liệu thuế");
        }
    }

    private void loadInsuranceRates() {
        try {
            JsonNode insuranceNode = JsonUtil.readJsonFromResource("../../../../config/insurance.json");

            List<InsuranceEntry> insuranceEntries = new ArrayList<>();

            for (JsonNode node : insuranceNode) {
                String id = node.get("insurance_id").asText();
                double percentage = node.get("percentage").asDouble();

                insuranceEntries.add(new InsuranceEntry(id, percentage));
            }

            tblInsurance.setItems(FXCollections.observableArrayList(insuranceEntries));

        } catch (IOException e) {
            log.error("Error loading insurance rates: {}", e.getMessage());
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải dữ liệu bảo hiểm");
        }
    }

    private void loadBonusTypes() {
        try {
            bonusTypeList = bonusTypeBUS.findAll();
            tblBonusType.setItems(FXCollections.observableArrayList(bonusTypeList));
        } catch (Exception e) {
            log.error("Error loading bonus types: {}", e.getMessage());
            NotificationUtil.showErrorNotification("Lỗi", "Không thể tải dữ liệu loại thưởng");
        }
    }


    private void setupButtonHandlers() {
        btnManageBonusTypes.setOnAction(event -> {
            BonusTypeManage.showDialog(busFactory, this);
        });
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "";
        return currencyFormat.format(value);
    }

    // Method to be called from BonusTypeAction to refresh data
    public void refreshData() {
        loadBonusTypes();
    }
}