package com.example.medicinedistribution.GUI;
import com.example.medicinedistribution.DTO.PermissionDTO;
import com.example.medicinedistribution.Util.GenericTablePrinter;
import com.example.medicinedistribution.Util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SubScene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static javafx.scene.control.PopupControl.USE_COMPUTED_SIZE;

@Slf4j
public class PermissionViewController {

    @FXML
    private VBox permissionsContainer;

    @FXML
    private VBox exampleContent;

    private Map<String, CheckBox> checkboxMap = new HashMap<>();
    private List<PermissionDTO> allPermissions;

    public PermissionViewController(List<PermissionDTO> permissions) {
        this.allPermissions = permissions;
        GenericTablePrinter.printTable(permissions);
        // Default constructor
    }

    @FXML
    public void initialize() {
        // Delete example content
        if (exampleContent != null) {
            permissionsContainer.getChildren().remove(exampleContent);
        }

        // Group permissions by parent
        Map<String, PermissionDTO> permissionMap = new HashMap<>();
        Map<String, List<PermissionDTO>> permissionsByParent = new HashMap<>();

        for (PermissionDTO permission : allPermissions) {
            permissionMap.put(permission.getPermissionCode(), permission);

            String parentCode = permission.getParentPermissionCode();
            if (parentCode == null || parentCode.isEmpty()) {
                // Top level permission
                if (!permissionsByParent.containsKey(permission.getPermissionCode())) {
                    permissionsByParent.put(permission.getPermissionCode(), new ArrayList<>());
                }
            } else {
                // Child permission
                if (!permissionsByParent.containsKey(parentCode)) {
                    permissionsByParent.put(parentCode, new ArrayList<>());
                }
                permissionsByParent.get(parentCode).add(permission);
            }
        }

        // Create modules
        String[] mainModules = {"SALES_MANAGEMENT", "SYSTEM_MANAGEMENT", "HUMAN_RESOURCES", "VIEW"};
        for (String moduleCode : mainModules) {
            if (permissionMap.containsKey(moduleCode)) {
                VBox moduleSection = createModuleSection(
                    permissionMap.get(moduleCode),
                    permissionsByParent, moduleCode);
                moduleSection.maxWidthProperty().bind(permissionsContainer.widthProperty().subtract(30)); // 30 for padding
                moduleSection.setMinWidth(100); // Set minimum width
                moduleSection.setPrefWidth(USE_COMPUTED_SIZE);
                permissionsContainer.getChildren().add(moduleSection);
            }
        }

        // Apply initial disabled state to child checkboxes based on parent checked state
        // This needs to be done after all checkboxes are created
        for (String moduleCode : mainModules) {
            if (!moduleCode.equals("VIEW") && permissionMap.containsKey(moduleCode)) {
                PermissionDTO modulePermission = permissionMap.get(moduleCode);
                // If the parent module is not checked, disable all its children
                if (!modulePermission.isChecked()) {
                    updateChildrenDisabledState(modulePermission.getPermissionCode(), false, permissionsByParent);
                }
            }
        }
    }

    private VBox createModuleSection(PermissionDTO modulePermission, Map<String, List<PermissionDTO>> permissionsByParent, String moduleCode) {
        VBox moduleSection = new VBox(10);

        moduleSection.getStyleClass().addAll("permission-section", "card");
        moduleSection.setPadding(new Insets(15));

        // Module header
        HBox moduleHeader = new HBox(10);
        moduleHeader.setAlignment(Pos.CENTER_LEFT);

        Label moduleLabel = new Label(modulePermission.getPermName());
        moduleLabel.getStyleClass().add("module-title");

        CheckBox moduleCheck = new CheckBox();
        moduleCheck.setSelected(modulePermission.isChecked());
        moduleCheck.setDisable(!modulePermission.isEditable());
        moduleCheck.getStyleClass().add("module-checkbox");
        moduleCheck.setUserData(modulePermission);

        // Store in map for future reference
        checkboxMap.put(modulePermission.getPermissionCode(), moduleCheck);

        // Set listener
        moduleCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            modulePermission.setChecked(newVal);
//            updateChildrenCheckboxes(modulePermission.getPermissionCode(), newVal, permissionsByParent);
            // Additionally update the disabled state
            updateChildrenDisabledState(modulePermission.getPermissionCode(), newVal, permissionsByParent);
        });

        moduleHeader.getChildren().add(moduleCheck);
        moduleHeader.getChildren().add(moduleLabel);
        moduleSection.getChildren().add(moduleHeader);

        // Add separator
        Separator separator = new Separator();
        separator.setPrefWidth(Double.MAX_VALUE);
        VBox.setMargin(separator, new Insets(5, 0, 10, 0));
        moduleSection.getChildren().add(separator);

        // Add submodules if any
        if (permissionsByParent.containsKey(modulePermission.getPermissionCode())) {
            GridPane subModulesGrid = new GridPane();
            subModulesGrid.setHgap(20);
            subModulesGrid.setVgap(15);

            List<PermissionDTO> submodules = permissionsByParent.get(modulePermission.getPermissionCode());
            int row = 0;
            int col = 0;
            int maxColumns = 1;
            if (Objects.equals(moduleCode, "VIEW")) {
                maxColumns = 2; // Adjust based on available width
            }

            for (PermissionDTO submodule : submodules) {
                VBox submoduleBox = createSubmoduleBox(submodule, permissionsByParent);
                subModulesGrid.add(submoduleBox, col, row);

                // Update grid position
                col++;
                if (col >= maxColumns) {
                    col = 0;
                    row++;
                }
            }

            moduleSection.getChildren().add(subModulesGrid);
        }

        return moduleSection;
    }

    private VBox createSubmoduleBox(PermissionDTO submodule, Map<String, List<PermissionDTO>> permissionsByParent) {
        VBox submoduleBox = new VBox(5);
        submoduleBox.getStyleClass().add("submodule-container");

        // Submodule header with checkbox
        HBox submoduleHeader = new HBox(10);
        submoduleHeader.setAlignment(Pos.CENTER_LEFT);

        CheckBox submoduleCheck = new CheckBox(submodule.getPermName());
        submoduleCheck.setSelected(submodule.isChecked());
        submoduleCheck.setDisable(!submodule.isEditable());
        submoduleCheck.getStyleClass().add("submodule-checkbox");
        submoduleCheck.setUserData(submodule);

        // Store in map
        checkboxMap.put(submodule.getPermissionCode(), submoduleCheck);

        // Set listener
        submoduleCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            submodule.setChecked(newVal);
            updateChildrenCheckboxes(submodule.getPermissionCode(), newVal, permissionsByParent);
            // Additionally update disabled state
            updateChildrenDisabledState(submodule.getPermissionCode(), newVal, permissionsByParent);
        });

        submoduleHeader.getChildren().add(submoduleCheck);
        submoduleBox.getChildren().add(submoduleHeader);

        // Add CRUD permissions if any
        if (permissionsByParent.containsKey(submodule.getPermissionCode())) {
            HBox actionsBox = new HBox(15);
            actionsBox.getStyleClass().add("permission-actions");
            actionsBox.setPadding(new Insets(5, 0, 5, 25));

            for (PermissionDTO action : permissionsByParent.get(submodule.getPermissionCode())) {
                CheckBox actionCheck = new CheckBox(action.getPermName());
                actionCheck.setSelected(action.isChecked());
                actionCheck.setDisable(!action.isEditable() || !submodule.isChecked()); // Initially disable if parent is unchecked
                actionCheck.getStyleClass().add("action-checkbox");
                actionCheck.setUserData(action);

                // Store in map
                checkboxMap.put(action.getPermissionCode(), actionCheck);

                // Set listener
                actionCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    action.setChecked(newVal);
                });

                actionsBox.getChildren().add(actionCheck);
            }

            submoduleBox.getChildren().add(actionsBox);
        }

        return submoduleBox;
    }

    private void updateChildrenCheckboxes(String parentCode, boolean newVal, Map<String, List<PermissionDTO>> permissionsByParent) {
        if (permissionsByParent.containsKey(parentCode)) {
            for (PermissionDTO child : permissionsByParent.get(parentCode)) {
                CheckBox childCheck = checkboxMap.get(child.getPermissionCode());
                if (childCheck != null) {
                    // Update the checked state
                    childCheck.setSelected(newVal);
                    child.setChecked(newVal);

                    // Recursively update children
                    updateChildrenCheckboxes(child.getPermissionCode(), newVal, permissionsByParent);
                }
            }
        }
    }

    // Separate method to handle the disabled state
    private void updateChildrenDisabledState(String parentCode, boolean parentChecked, Map<String, List<PermissionDTO>> permissionsByParent) {
        // Skip for VIEW module
        if ("VIEW".equals(parentCode)) {
            return;
        }

        if (permissionsByParent.containsKey(parentCode)) {
            for (PermissionDTO child : permissionsByParent.get(parentCode)) {
                CheckBox childCheck = checkboxMap.get(child.getPermissionCode());
                if (childCheck != null) {
                    // Disable checkbox if parent is not checked, unless it's already not editable
                    // Only allow editable checkboxes to be enabled when parent is checked
                    childCheck.setDisable(!parentChecked || !child.isEditable());

                    // Recursively update children's disabled state
                    updateChildrenDisabledState(child.getPermissionCode(), parentChecked && child.isChecked(), permissionsByParent);
                }
            }
        }
    }
public List<PermissionDTO> getUpdatedPermissions() {
    List<PermissionDTO> selectedPermissions = new ArrayList<>();
    List<String> parentsWithNoSelectedChildren = new ArrayList<>();

    // Build a map to track parent-child relationships for validation
    Map<String, List<String>> parentToChildrenMap = new HashMap<>();
    Map<String, String> childToParentMap = new HashMap<>();
    Map<String, Boolean> parentHasSelectedChildMap = new HashMap<>();

    // Initialize parent tracking
    for (PermissionDTO permission : allPermissions) {
        String parentCode = permission.getParentPermissionCode();
        if (parentCode != null && !parentCode.isEmpty()) {
            // This is a child permission
            childToParentMap.put(permission.getPermissionCode(), parentCode);

            // Add to the parent's children list
            if (!parentToChildrenMap.containsKey(parentCode)) {
                parentToChildrenMap.put(parentCode, new ArrayList<>());
            }
            parentToChildrenMap.get(parentCode).add(permission.getPermissionCode());
        }

        // Initialize all parents with no selected children
        if (parentToChildrenMap.containsKey(permission.getPermissionCode())) {
            parentHasSelectedChildMap.put(permission.getPermissionCode(), false);
        }
    }

    // First, collect all selected permissions
    List<PermissionDTO> allSelected = new ArrayList<>();
    for (PermissionDTO permission : allPermissions) {
        CheckBox checkBox = checkboxMap.get(permission.getPermissionCode());
        if (checkBox != null && checkBox.isSelected() && !checkBox.isDisabled()) {
            allSelected.add(permission);

            // Mark parent as having a selected child
            String parentCode = childToParentMap.get(permission.getPermissionCode());
            if (parentCode != null) {
                parentHasSelectedChildMap.put(parentCode, true);
            }
        }
    }

    // Check if any parent is selected but has no selected children
    for (PermissionDTO permission : allSelected) {
        String permCode = permission.getPermissionCode();
        // If this is a parent with children but no selected children
        if (parentToChildrenMap.containsKey(permCode) &&
            !parentToChildrenMap.get(permCode).isEmpty() &&
            !parentHasSelectedChildMap.get(permCode) &&
            !permCode.equals("VIEW")) { // Skip validation for VIEW

            parentsWithNoSelectedChildren.add(permission.getPermName());
        } else {
            // This permission is valid to include
            selectedPermissions.add(permission);
        }
    }

    if (selectedPermissions.isEmpty()) {
        NotificationUtil.showErrorNotification("Quyền không hợp lệ", "Vui lòng chọn ít nhất một quyền.");
        return null; // Indicate validation failed
    }
    // Show notification if there are parents with no selected children
    if (!parentsWithNoSelectedChildren.isEmpty()) {
        log.warn("Parent permissions with no selected children: " + String.join(", ", parentsWithNoSelectedChildren));

        // Show an alert dialog
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.WARNING
        );
        NotificationUtil.showErrorNotification("Quyền không hợp lệ", "Có quyền cha không có quyền con nào được chọn: " + String.join(", ", parentsWithNoSelectedChildren));

        return null; // Indicate validation failed
    }

    return selectedPermissions;
}
}