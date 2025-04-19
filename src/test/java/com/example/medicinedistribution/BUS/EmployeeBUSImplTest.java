package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.*;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.*;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Để chạy test theo thứ tự
class EmployeeBUSImplTest {
    private static BUSFactory busFactory;
    private static EmployeeBUS employeeBUS;
    private static PositionBUS positionBUS;
    private static DepartmentBUS departmentBUS;
    private static EmployeeDTO testEmployee;
    private static int testEmployeeId;
    private static PositionDTO testPosition;
    private static DepartmentDTO testDepartment;
    private static AccountDTO testAccount;
    private static AccountBUS accountBUS;

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
        // Tạo một tài khoản test để sử dụng trong các test

        testDepartment = DepartmentDTO.builder()
                .departmentName("TestDepartment")
                .build();


        testPosition = PositionDTO.builder()
                .positionName("TestPosition")
                .build();
        testAccount = AccountDTO.builder()
                .username("testuser")
                .password("testpassword")
                .roleId(1)  // Giả sử là admin
                .build();

        positionBUS = busFactory.getPositionBUS();
        employeeBUS = busFactory.getEmployeeBUS();
        positionBUS = busFactory.getPositionBUS();
        departmentBUS = busFactory.getDepartmentBUS();
        accountBUS = busFactory.getAccountBUS();
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
        result = positionBUS.insert(testPosition);
        assertTrue(result, "Position should be inserted");
        result = accountBUS.insert(testAccount);
        assertTrue(result, "Account should be inserted");
        testEmployee = EmployeeDTO.builder()
                .firstName("TestEmployee")
                .lastName("Example")
                .birthday(LocalDate.of(1990, 5, 15))  // Giả sử sinh nhật là 15/05/1990
                .gender("Male")
                .phone("0123456789")  // Số điện thoại giả định
                .email("test.employee@example.com")
                .hireDate(LocalDate.of(2022, 1, 1))  // Giả sử ngày vào làm là 01/01/2022
                .address("123 Test Street, Test City")
                .basicSalary(new BigDecimal("3500.00"))  // Tiền lương cơ bản
                .status(1)
                .positionId(testPosition.getPositionId())  // Liên kết với testPosition
                .accountId(testAccount.getAccountId())  // Giả sử accountId là 1
                .build();
    }

    @BeforeEach
    void setUp() {

    }

    @Test
    @Order(1)
    void insert_validEmployee_employeeIsAdded() {
        boolean result = employeeBUS.insert(testEmployee);
        assertTrue(result, "Employee should be inserted");

        EmployeeDTO inserted = employeeBUS.findById(testEmployee.getEmployeeId());
        assertNotNull(inserted, "Inserted employee should not be null");
        testEmployeeId = inserted.getEmployeeId();
    }

    @Test
    @Order(2)
    void findById_existingEmployee_returnsEmployeeDTO() {
        EmployeeDTO retrieved = employeeBUS.findById(testEmployeeId);
        assertNotNull(retrieved, "Employee should exist");
        assertEquals("TestEmployee", retrieved.getFirstName());
    }

    @Test
    @Order(3)
    void update_existingEmployee_employeeIsUpdated() {
        EmployeeDTO updateEmp = EmployeeDTO.builder()
                .employeeId(testEmployee.getEmployeeId())  // Giả sử employeeId cần cập nhật là 1
                .firstName("UpdatedEmployee")  // Tên mới
                .lastName("UpdatedLastName")   // Họ mới
                .birthday(LocalDate.of(1991, 7, 20))  // Ngày sinh mới
                .gender("Female")  // Giới tính mới
                .phone("0987654321")  // Số điện thoại mới
                .email("updated.employee@example.com")  // Email mới
                .hireDate(LocalDate.of(2022, 1, 1))  // Giữ nguyên ngày vào làm
                .address("456 Updated Street, Updated City")  // Địa chỉ mới
                .basicSalary(new BigDecimal("4000.00"))  // Tiền lương cập nhật
                .status(0)  // Trạng thái mới
                .positionId(testPosition.getPositionId())  // Cập nhật positionId
                .accountId(testAccount.getAccountId())  // Giữ nguyên accountId
                .build();

        boolean result = employeeBUS.update(updateEmp);
        assertTrue(result, "Employee should be updated");

        EmployeeDTO retrieved = employeeBUS.findById(testEmployeeId);
        assertEquals("UpdatedEmployee", retrieved.getFirstName());
    }

    @Test
    @Order(4)
    void findAll_employeesExist_returnsNonEmptyList() {
        List<EmployeeDTO> employees = employeeBUS.findAll();
        assertFalse(employees.isEmpty(), "Employees should not be empty");
    }

    @Test
    @Order(5)
    void delete_existingEmployee_employeeIsDeleted() {
        boolean result = employeeBUS.delete(testEmployeeId);
        assertTrue(result, "Employee should be deleted");

        EmployeeDTO retrieved = employeeBUS.findById(testEmployeeId);
        assertNull(retrieved, "Employee should be null after deletion");
    }

    @Test
    @Order(6)
    void insert_nullEmployee_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            employeeBUS.insert(null);
        });
    }

    @Test
    @Order(7)
    void insert_emptyName_throwsException() {
        EmployeeDTO invalidEmployee = EmployeeDTO.builder()
                .firstName("")
                .lastName("")
                .build();
        assertThrows(IllegalArgumentException.class, () -> {
            employeeBUS.insert(invalidEmployee);
        });
    }

    @Test
    @Order(8)
    void findById_nonExistentId_returnsNull() {
        EmployeeDTO result = employeeBUS.findById(99999);
        assertNull(result);
    }

    @Test
    @Order(9)
    void update_nonExistentEmployee_throwsException() {
        EmployeeDTO nonExistentEmp = EmployeeDTO.builder()
                .employeeId(99999)
                .firstName("NonExistent")
                .lastName("Employee")
                .positionId(testPosition.getPositionId())
                .accountId(testAccount.getAccountId())
                .build();
        assertThrows(RuntimeException.class, () -> {
            employeeBUS.update(nonExistentEmp);
        });
    }

    @Test
    @Order(10)
    void update_nullEmployee_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            employeeBUS.update(null);
        });
    }

    @Test
    @Order(11)
    void delete_nonExistentId_throwsException() {
        assertThrows(DeleteFailedException.class, () ->
            employeeBUS.delete(99999)
        );
    }
    @Test
    @Order(12)
    void insert_invalidFirstName_throwsException() {
        EmployeeDTO invalidEmployee = EmployeeDTO.builder()
                .firstName("") // Invalid: Blank
                .lastName("ValidLastName")
                .birthday(LocalDate.of(1990, 5, 15))
                .gender("Male")
                .phone("0123456789")
                .email("valid.email@example.com")
                .hireDate(LocalDate.of(2022, 1, 1))
                .address("123 Test Street")
                .basicSalary(new BigDecimal("3500.00"))
                .status(1)
                .positionId(testPosition.getPositionId())
                .accountId(testAccount.getAccountId())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeBUS.insert(invalidEmployee);
        });
        assertEquals("Họ không được để trống", exception.getMessage());
    }

    @Test
    @Order(13)
    void insert_invalidLastName_throwsException() {
        EmployeeDTO invalidEmployee = EmployeeDTO.builder()
                .firstName("ValidFirstName")
                .lastName("") // Invalid: Blank
                .birthday(LocalDate.of(1990, 5, 15))
                .gender("Male")
                .phone("0123456789")
                .email("valid.email@example.com")
                .hireDate(LocalDate.of(2022, 1, 1))
                .address("123 Test Street")
                .basicSalary(new BigDecimal("3500.00"))
                .status(1)
                .positionId(testPosition.getPositionId())
                .accountId(testAccount.getAccountId())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeBUS.insert(invalidEmployee);
        });
        assertEquals("Tên không được để trống", exception.getMessage());
    }

    @Test
    @Order(14)
    void insert_invalidBirthday_throwsException() {
        EmployeeDTO invalidEmployee = EmployeeDTO.builder()
                .firstName("ValidFirstName")
                .lastName("ValidLastName")
                .birthday(LocalDate.now().plusDays(1)) // Invalid: Future date
                .gender("Male")
                .phone("0123456789")
                .email("valid.email@example.com")
                .hireDate(LocalDate.of(2022, 1, 1))
                .address("123 Test Street")
                .basicSalary(new BigDecimal("3500.00"))
                .status(1)
                .positionId(testPosition.getPositionId())
                .accountId(testAccount.getAccountId())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeBUS.insert(invalidEmployee);
        });
        assertEquals("Ngày sinh phải là ngày trong quá khứ", exception.getMessage());
    }

    @Test
    @Order(15)
    void insert_invalidPhone_throwsException() {
        EmployeeDTO invalidEmployee = EmployeeDTO.builder()
                .firstName("ValidFirstName")
                .lastName("ValidLastName")
                .birthday(LocalDate.of(1990, 5, 15))
                .gender("Male")
                .phone("12345") // Invalid: Does not match regex
                .email("valid.email@example.com")
                .hireDate(LocalDate.of(2022, 1, 1))
                .address("123 Test Street")
                .basicSalary(new BigDecimal("3500.00"))
                .status(1)
                .positionId(testPosition.getPositionId())
                .accountId(testAccount.getAccountId())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeBUS.insert(invalidEmployee);
        });
        assertEquals("Số điện thoại không hợp lệ", exception.getMessage());
    }

    @Test
    @Order(16)
    void insert_invalidEmail_throwsException() {
        EmployeeDTO invalidEmployee = EmployeeDTO.builder()
                .firstName("ValidFirstName")
                .lastName("ValidLastName")
                .birthday(LocalDate.of(1990, 5, 15))
                .gender("Male")
                .phone("0123456789")
                .email("invalid-email") // Invalid: Not a valid email format
                .hireDate(LocalDate.of(2022, 1, 1))
                .address("123 Test Street")
                .basicSalary(new BigDecimal("3500.00"))
                .status(1)
                .positionId(testPosition.getPositionId())
                .accountId(testAccount.getAccountId())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeBUS.insert(invalidEmployee);
        });
        assertEquals("Email không hợp lệ", exception.getMessage());
    }

    @Test
    @Order(17)
    void insert_invalidBasicSalary_throwsException() {
        EmployeeDTO invalidEmployee = EmployeeDTO.builder()
                .firstName("ValidFirstName")
                .lastName("ValidLastName")
                .birthday(LocalDate.of(1990, 5, 15))
                .gender("Male")
                .phone("0123456789")
                .email("valid.email@example.com")
                .hireDate(LocalDate.of(2022, 1, 1))
                .address("123 Test Street")
                .basicSalary(new BigDecimal("-1000.00")) // Invalid: Negative salary
                .status(1)
                .positionId(testPosition.getPositionId())
                .accountId(testAccount.getAccountId())
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            employeeBUS.insert(invalidEmployee);
        });
        assertEquals("Lương cơ bản phải lớn hơn 0", exception.getMessage());
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        // Dọn dẹp database test nếu cần (ví dụ: xóa tất cả dữ liệu sau khi test)
        // Lưu ý: Cần cẩn thận khi thực hiện việc này
        positionBUS.delete(testPosition.getPositionId());
        departmentBUS.delete(testDepartment.getDepartmentId());
        accountBUS.delete(testAccount.getAccountId());

    }
}
