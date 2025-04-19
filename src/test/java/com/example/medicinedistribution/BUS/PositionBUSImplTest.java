package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.BUS.Interface.DepartmentBUS;
import com.example.medicinedistribution.BUS.Interface.PositionBUS;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.DepartmentDTO;
import com.example.medicinedistribution.DTO.PositionDTO;
import com.example.medicinedistribution.DTO.UserSession;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Để chạy test theo thứ tự
class PositionBUSImplTest {
    private static BUSFactory busFactory;
    private static PositionBUS positionBUS;
    private static DepartmentBUS departmentBUS;
    private static PositionDTO testPosition;
    private static DepartmentDTO testDepartment;
    private static int testPositionId;



    @BeforeAll
    static void setUpAll() {
        // Khởi tạo DataSource cho môi trường test (đảm bảo trỏ đến test database)
        DataSource dataSource = DBConnection.getDataSource(); // Giả sử getDataSource() đã được cấu hình cho test
        TransactionManager transactionManager = new TransactionManager(dataSource);
        DAOFactory daoFactory = new MySQLDAOFactory();
        UserSession userSession = new UserSession();
                ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator()) // Sử dụng interpolator không yêu cầu EL
                .buildValidatorFactory();
        Validator validator = factory.getValidator();

        busFactory = new BUSFactoryImpl(dataSource, daoFactory , transactionManager, userSession,validator);
        testDepartment = DepartmentDTO.builder()
                .departmentName("TestDepartment")
                .build();


        testPosition = PositionDTO.builder()
                .positionName("TestPosition")
                .build();
        positionBUS = busFactory.getPositionBUS();
        departmentBUS = busFactory.getDepartmentBUS();
        AuthBUS authBUS = busFactory.getAuthBUS();
        if (authBUS.login("admin", "admin")){
            System.out.println("Login successful");
        } else {
            System.out.println("Login failed");
        }
        // Tạo một phòng ban test để sử dụng trong các test
        boolean result = departmentBUS.insert(testDepartment);
        assertTrue(result, "Department should be inserted");
        testPosition.setDepartmentId(testDepartment.getDepartmentId());
    }

    @BeforeEach
    void setUp() {

    }

    @Test
    @Order(1)
    void insert_validPosition_positionIsAdded() {
        System.out.println("insert_validPosition_positionIsAdded");
        System.out.println(testPosition);
        boolean result = positionBUS.insert(testPosition);
        assertTrue(result, "Position should be inserted");

        PositionDTO inserted = positionBUS.findById(testPosition.getPositionId());
        assertNotNull(inserted, "Inserted position should not be null");
        testPositionId = inserted.getPositionId();
    }

    @Test
    @Order(2)
    void findById_existingPosition_returnsPositionDTO() {
        PositionDTO retrieved = positionBUS.findById(testPositionId);
        assertNotNull(retrieved, "Position should exist");
        assertEquals("TestPosition", retrieved.getPositionName());
    }

    @Test
    @Order(3)
    void update_existingPosition_positionIsUpdated() {
        PositionDTO updatedPos = PositionDTO.builder()
                .positionId(testPositionId)
                .positionName("UpdatedPosition")
                .departmentId(testDepartment.getDepartmentId())
                .build();

        boolean result = positionBUS.update(updatedPos);
        assertTrue(result, "Position should be updated");

        PositionDTO retrieved = positionBUS.findById(testPositionId);
        assertEquals("UpdatedPosition", retrieved.getPositionName());
    }

    @Test
    @Order(4)
    void findAll_positionsExist_returnsNonEmptyList() {
        List<PositionDTO> positions = positionBUS.findAll();
        assertFalse(positions.isEmpty(), "Positions should not be empty");
    }

    @Test
    @Order(5)
    void delete_existingPosition_positionIsDeleted() {
        boolean result = positionBUS.delete(testPositionId);
        assertTrue(result, "Position should be deleted");

        PositionDTO retrieved = positionBUS.findById(testPositionId);
        assertNull(retrieved, "Position should be null after deletion");
    }

    @Test
    @Order(6)
    void insert_invalidPositionName_throwsException() {
        PositionDTO invalidPosition = PositionDTO.builder()
                .positionName("") // Invalid: Blank
                .departmentId(testDepartment.getDepartmentId())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            positionBUS.insert(invalidPosition);
        });
        assertEquals("Tên chức vụ không được để trống", exception.getMessage());
    }

    @Test
    @Order(7)
    void update_invalidPositionName_throwsException() {
        PositionDTO invalidPosition = PositionDTO.builder()
                .positionId(testPositionId)
                .positionName("") // Invalid: Blank
                .departmentId(testDepartment.getDepartmentId())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            positionBUS.update(invalidPosition);
        });
        assertEquals("Tên chức vụ không được để trống", exception.getMessage());
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        // Dọn dẹp database test nếu cần (ví dụ: xóa tất cả dữ liệu sau khi test)
        // Lưu ý: Cần cẩn thận khi thực hiện việc này
        departmentBUS.delete(testDepartment.getDepartmentId());

    }


}