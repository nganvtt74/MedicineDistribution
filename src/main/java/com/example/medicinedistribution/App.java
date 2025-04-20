package com.example.medicinedistribution;

import com.example.medicinedistribution.BUS.BUSFactory;
import com.example.medicinedistribution.BUS.BUSFactoryImpl;
import com.example.medicinedistribution.BUS.TransactionManager;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.GUI.LoginController;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import javax.sql.DataSource;
import java.util.Objects;


public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        DAOFactory daoFactory = new MySQLDAOFactory();
        DataSource dataSource = DBConnection.getDataSource();
        TransactionManager transactionManager = new TransactionManager(dataSource);
        UserSession userSession = new UserSession();
        // Initialize the validator factory
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator()) // Sử dụng interpolator không yêu cầu EL
                .buildValidatorFactory();
        Validator validator = factory.getValidator();

        BUSFactory busFactory = new BUSFactoryImpl (dataSource, daoFactory, transactionManager, userSession, validator);



        LoginController loginController = new LoginController(busFactory);
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("GUI/login.fxml"));
        fxmlLoader.setController(loginController);
        ImageView icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResource("../../../img/logo.png")).toExternalForm()));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Medicine Distribution Management");
        stage.getIcons().add(icon.getImage());
        stage.setScene(scene);
        stage.show();

    }
}
