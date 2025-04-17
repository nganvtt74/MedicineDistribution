package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.*;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.*;
import lombok.extern.slf4j.Slf4j;
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
        busFactory = new BUSFactoryImpl(dataSource, daoFactory, transactionManager, userSession);
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
                .phone("123-456-7890")
                .email("test.employee@example.com")
                .hireDate(LocalDate.of(2022, 1, 1))  // Giả sử ngày vào làm là 01/01/2022
                .address("123 Test Street, Test City")
                .basicSalary(new BigDecimal("3500.00"))  // Tiền lương cơ bản
                .status("Active")
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
                .phone("987-654-3210")  // Số điện thoại mới
                .email("updated.employee@example.com")  // Email mới
                .hireDate(LocalDate.of(2022, 1, 1))  // Giữ nguyên ngày vào làm
                .address("456 Updated Street, Updated City")  // Địa chỉ mới
                .basicSalary(new BigDecimal("4000.00"))  // Tiền lương cập nhật
                .status("Inactive")  // Trạng thái mới
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



    @AfterAll
    static void tearDownAll() throws SQLException {
        // Dọn dẹp database test nếu cần (ví dụ: xóa tất cả dữ liệu sau khi test)
        // Lưu ý: Cần cẩn thận khi thực hiện việc này
        positionBUS.delete(testPosition.getPositionId());
        departmentBUS.delete(testDepartment.getDepartmentId());
        accountBUS.delete(testAccount.getAccountId());

    }
}