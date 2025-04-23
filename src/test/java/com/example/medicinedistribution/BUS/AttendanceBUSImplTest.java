package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AttendanceBUS;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.AttendanceDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import com.example.medicinedistribution.Exception.UpdateFailedException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AttendanceBUSImplTest {

    private DataSource dataSource;
    private AttendanceBUS attendanceBUS;
    private final int employeeId = 1;
    private final String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

    @BeforeEach
    void setUp() {
        // Get actual database connection
        dataSource = DBConnection.getDataSource();

        // Create transaction manager
        TransactionManager transactionManager = new TransactionManager(dataSource);

        // Create DAO factory
        DAOFactory daoFactory = new MySQLDAOFactory();

        // Create user session with proper permissions
        UserSession userSession = new UserSession();
        // Create validator
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        Validator validator = factory.getValidator();

        // Create BUS factory and get attendance BUS
        BUSFactory busFactory = new BUSFactoryImpl(dataSource, daoFactory, transactionManager, userSession, validator);
        attendanceBUS = busFactory.getAttendanceBUS();
    }

    @Order(1)
    @Test
    void testCheckIn() {
        // First make sure attendance doesn't exist or delete it if it does
        try {
            attendanceBUS.deleteAttendance(employeeId, date);
        } catch (Exception e) {
            // Ignore if not exists
        }

        // Test check in
        boolean result = attendanceBUS.checkInAttendance(employeeId, date);

        // Verify check in was successful
        assertTrue(result);

        // Verify record was created with correct data
        AttendanceDTO attendance = attendanceBUS.getAttendance(employeeId, date);
        assertNotNull(attendance);
        assertNotNull(attendance.getCheck_in());
        assertEquals(employeeId, attendance.getEmployee_id());
    }

    @Order(2)
    @Test
    void testCheckOut() {
        // Test depends on check-in being completed first

        // Test check out
        boolean result = attendanceBUS.checkOutAttendance(employeeId, date);

        // Verify check out was successful
        assertTrue(result);

        // Verify record was updated with check-out time
        AttendanceDTO attendance = attendanceBUS.getAttendance(employeeId, date);
        assertNotNull(attendance);
        assertNotNull(attendance.getCheck_in());
        assertNotNull(attendance.getCheck_out());
        assertEquals(employeeId, attendance.getEmployee_id());
    }

    @Order(3)
    @Test
    void testCheckInWithoutPermission() {
        // Create user session without proper permissions
        UserSession restrictedUserSession = new UserSession();

        // Create new BUS with restricted permissions
        TransactionManager transactionManager = new TransactionManager(dataSource);
        DAOFactory daoFactory = new MySQLDAOFactory();
        ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        Validator validator = factory.getValidator();

        AttendanceBUS restrictedBUS = new AttendanceBUSImpl(
                daoFactory.getAttendanceDAO(),
                dataSource,
                restrictedUserSession,
                transactionManager,
                validator);

        // Test check in without permission
        assertThrows(PermissionDeniedException.class, () -> {
            restrictedBUS.checkInAttendance(employeeId, date);
        });
    }
//
//    @AfterAll
//    static void tearDown() throws SQLException {
//        // Clean up test data if needed
//        // You might want to delete the test attendance record
//        // or reset its state to what it was before testing
//        DataSource dataSource = DBConnection.getDataSource();
//        try (Connection conn = dataSource.getConnection()) {
//            conn.prepareStatement("DELETE FROM attendance WHERE employee_id = 1 AND date = CURRENT_DATE()").executeUpdate();
//        }
//    }
}