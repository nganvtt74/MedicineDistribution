package com.example.medicinedistribution.Util;

        import javafx.scene.control.Alert;
        import javafx.scene.control.ButtonType;
        import javafx.scene.control.DialogPane;
        import javafx.scene.image.Image;
        import javafx.scene.image.ImageView;
        import javafx.stage.Stage;

        public class NotificationUtil {
            private static void applyCustomStyles(Alert alert, String iconPath) {
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(
                    NotificationUtil.class.getResource("/css/style.css").toExternalForm());
                dialogPane.getStyleClass().add("custom-alert");

                // Try to add icon if provided
                if (iconPath != null) {
                    try {
                        ImageView icon = new ImageView(new Image(
                            NotificationUtil.class.getResourceAsStream(iconPath)));
                        icon.setFitHeight(48);
                        icon.setFitWidth(48);
                        dialogPane.setGraphic(icon);
                    } catch (Exception e) {
                        // Fallback to default icon if image loading fails
                    }
                }

                // Add application icon to the dialog window
                Stage stage = (Stage) dialogPane.getScene().getWindow();

                stage.getIcons().add(new Image(
                        NotificationUtil.class.getResource("../../../../img/logo.png").toExternalForm()));
            }

            public static void showNotification(String title, String message) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);

                applyCustomStyles(alert, "/images/info_icon.png");
                alert.getDialogPane().getStyleClass().add("info-alert");

                alert.showAndWait();
            }

            public static void showWarningNotification(String title, String message) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);

                applyCustomStyles(alert, "/images/warning_icon.png");
                alert.getDialogPane().getStyleClass().add("warning-alert");

                alert.showAndWait();
            }

            public static void showWarningNotification(String title, String header, String message) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(message);

                applyCustomStyles(alert, "/images/warning_icon.png");
                alert.getDialogPane().getStyleClass().add("warning-alert-with-header");

                alert.showAndWait();
            }

            public static void showErrorNotification(String title, String header, String message) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(message);
                applyCustomStyles(alert, "/images/error_icon.png");
                alert.getDialogPane().getStyleClass().add("error-alert-with-header");

                alert.showAndWait();
            }



            public static void showErrorNotification(String title, String message) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);

                applyCustomStyles(alert, "/images/error_icon.png");
                alert.getDialogPane().getStyleClass().add("error-alert");

                alert.showAndWait();
            }

            public static void showSuccessNotification(String title, String message) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);

                applyCustomStyles(alert, "/images/" +
                        ".png");
                alert.getDialogPane().getStyleClass().add("success-alert");

                alert.showAndWait();
            }

            public static void showSuccessNotification(String title, String header, String message) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(message);

                applyCustomStyles(alert, "/images/success_icon.png");
                alert.getDialogPane().getStyleClass().add("success-alert-with-header");

                alert.showAndWait();
            }

            public static boolean showConfirmation(String title, String message) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
                alert.setTitle(title);
                alert.setHeaderText(null);

                applyCustomStyles(alert, "/images/question_icon.png");
                alert.getDialogPane().getStyleClass().add("confirm-alert");

                return alert.showAndWait().filter(t -> t == ButtonType.YES).isPresent();
            }

            public static boolean showConfirmation(String title, String header, String message) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
                alert.setTitle(title);
                alert.setHeaderText(header);

                applyCustomStyles(alert, "/images/question_icon.png");
                alert.getDialogPane().getStyleClass().add("confirm-alert-with-header");

                return alert.showAndWait().filter(t -> t == ButtonType.YES).isPresent();
            }
        }