package com.example.medicinedistribution.GUI.SubSelect;

/**
 * Abstract class for handling entity selection in selection dialogs
 * @param <T> The type of entity being selected
 */
public abstract class SelectionHandler<T> {

    /**
     * This method is called when an item is selected in a selection dialog
     * @param selectedItem The entity that was selected by the user
     */
    public abstract void onItemSelected(T selectedItem);

    /**
     * Optional method to validate the selected item before closing the dialog
     * @param selectedItem The selected entity to validate
     * @return true if the selection is valid, false otherwise
     */
    public boolean validateSelection(T selectedItem) {
        return selectedItem != null;
    }

    /**
     * Optional method called when the selection dialog is canceled
     * This allows for cleanup or other handling when users dismiss the dialog
     */
    public void onSelectionCancelled() {
        // Default empty implementation
    }
}