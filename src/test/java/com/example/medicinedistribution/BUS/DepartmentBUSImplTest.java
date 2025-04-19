package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.BUS.Interface.DepartmentBUS;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.DepartmentDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Để chạy test theo thứ tự
class DepartmentBUSImplTest {
    private static BUSFactory busFactory;
    private DepartmentBUS departmentBUS;
    private static DepartmentDTO testDepartment;
    private static int testDepartmentId;

    @BeforeAll
    static void setUpAll() {
        // Khởi tạo DataSource cho môi trường test (đảm bảo trỏ đến test database)
        DataSource dataSource = DBConnection.getDataSource(); // Giả sử getDataSource() đã được cấu hình cho test
        TransactionManager transactionManager = new TransactionManager(dataSource);
        DAOFactory daoFactory = new MySQLDAOFactory();
        UserSession userSession = new UserSession();
      ValidatorFactory factory =   Validation.byProvider(HibernateValidator.class)
              .configure()
              .messageInterpolator(new ParameterMessageInterpolator()) // Sử dụng interpolator không yêu cầu EL
              .buildValidatorFactory();
        Validator validator = factory.getValidator();

        busFactory = new BUSFactoryImpl(dataSource, daoFactory , transactionManager, userSession,validator);
        testDepartment = DepartmentDTO.builder()
                .departmentName("TestDepartment")
                .build();
    }

    @BeforeEach
    void setUp() {
        departmentBUS = busFactory.getDepartmentBUS();
        AuthBUS authBUS = busFactory.getAuthBUS();
        if (authBUS.login("admin", "admin")){
            System.out.println("Login successful");
        } else {
            System.out.println("Login failed");
        }
    }
    @Test
    @Order(1)
    void insert_validDepartment_departmentIsAdded() {
        boolean result = departmentBUS.insert(testDepartment);
        assertTrue(result, "Department should be inserted");

        // Assume the new ID is generated after insertion
        // Retrieve the inserted department
        DepartmentDTO inserted = departmentBUS.findById(testDepartment.getDepartmentId());
        assertNotNull(inserted, "Inserted department should not be null");
        testDepartmentId = inserted.getDepartmentId();
    }

    @Test
    @Order(2)
    void findById_existingDepartment_returnsDepartmentDTO() {
        DepartmentDTO retrieved = departmentBUS.findById(testDepartmentId);
        assertNotNull(retrieved, "Department should exist");
        assertEquals("TestDepartment", retrieved.getDepartmentName());
    }

    @Test
    @Order(3)
    void update_existingDepartment_departmentIsUpdated() {
        DepartmentDTO updatedDept = DepartmentDTO.builder()
                .departmentId(testDepartmentId)
                .departmentName("UpdatedDepartment")
                .build();

        boolean result = departmentBUS.update(updatedDept);
        assertTrue(result, "Department should be updated");

        DepartmentDTO retrieved = departmentBUS.findById(testDepartmentId);
        assertEquals("UpdatedDepartment", retrieved.getDepartmentName());
    }

    @Test
    @Order(4)
    void findAll_departmentsExist_returnsNonEmptyList() {
        List<DepartmentDTO> departments = departmentBUS.findAll();
        assertFalse(departments.isEmpty(), "Departments should not be empty");
    }

    @Test
    @Order(5)
    void delete_existingDepartment_departmentIsDeleted() {
        boolean result = departmentBUS.delete(testDepartmentId);
        assertTrue(result, "Department should be deleted");

        DepartmentDTO retrieved = departmentBUS.findById(testDepartmentId);
        assertNull(retrieved, "Department should be null after deletion");
    }

    @Test
    @Order(6)
    void insert_nullDepartment_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            departmentBUS.insert(null);
        });
    }

    @Test
    @Order(7)
    void insert_emptyName_throwsException() {
        DepartmentDTO invalidDepartment = DepartmentDTO.builder()
                .departmentName("")
                .build();
        assertThrows(IllegalArgumentException.class, () -> {
            departmentBUS.insert(invalidDepartment);
        });
    }

    @Test
    @Order(8)
    void findById_nonExistentId_returnsNull() {
        DepartmentDTO result = departmentBUS.findById(99999);
        assertNull(result);
    }

    @Test
    @Order(9)
    void update_nonExistentDepartment_throwsException() {
        DepartmentDTO nonExistentDept = DepartmentDTO.builder()
                .departmentId(99999)
                .departmentName("NonExistent")
                .build();
        assertThrows(RuntimeException.class, () -> {
            departmentBUS.update(nonExistentDept);
        });
    }

    @Test
    @Order(10)
    void update_nullDepartment_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            departmentBUS.update(null);
        });
    }

    @Test
    @Order(11)
    void delete_nonExistentId_throwsException() {
        assertThrows(DeleteFailedException.class, () ->
            departmentBUS.delete(99999)
        );
    }

    @Test
    @Order(12)
    void insert_invalidDepartmentName_throwsException() {
        DepartmentDTO invalidDepartment = DepartmentDTO.builder()
                .departmentName("") // Invalid: Blank
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            departmentBUS.insert(invalidDepartment);
        });
        assertEquals("Tên phòng ban không được để trống", exception.getMessage());
    }

    @Test
    @Order(13)
    void update_invalidDepartmentName_throwsException() {
        DepartmentDTO invalidDepartment = DepartmentDTO.builder()
                .departmentId(testDepartmentId)
                .departmentName("") // Invalid: Blank
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            departmentBUS.update(invalidDepartment);
        });
        assertEquals("Tên phòng ban không được để trống", exception.getMessage());
    }
}
