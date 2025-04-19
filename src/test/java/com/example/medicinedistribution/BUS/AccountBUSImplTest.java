package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.BUS.Interface.AccountBUS;
import com.example.medicinedistribution.DAO.Interface.AccountDAO;
import com.example.medicinedistribution.DTO.AccountDTO;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.AlreadyExistsException;
import com.example.medicinedistribution.Util.PasswordUtil;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Để chạy test theo thứ tự
class AccountBUSImplTest {

    private static DataSource dataSource;
    private static DAOFactory daoFactory;
    private static BUSFactory busFactory;
    private AccountBUS accountBUS;
    private static AccountDTO testAccount;
    private static int testAccountId;

    @BeforeAll
    static void setUpAll() {
        // Khởi tạo DataSource cho môi trường test (đảm bảo trỏ đến test database)
        dataSource = DBConnection.getDataSource(); // Giả sử getDataSource() đã được cấu hình cho test
        TransactionManager transactionManager = new TransactionManager(dataSource);
        daoFactory = new MySQLDAOFactory();
        UserSession userSession = new UserSession();
                ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator()) // Sử dụng interpolator không yêu cầu EL
                .buildValidatorFactory();
        Validator validator = factory.getValidator();

        busFactory = new BUSFactoryImpl(dataSource, daoFactory , transactionManager, userSession,validator);
                // Tạo một tài khoản test để sử dụng trong các test
                testAccount = AccountDTO.builder()
                        .username("testuser")
                        .password("testpassword")
                        .roleId(40)
                        .build();
    }

    @BeforeEach
    void setUp() {
        accountBUS = busFactory.getAccountBUS();
        AuthBUS authBUS = busFactory.getAuthBUS();
        if (authBUS.login("admin", "admin")){
            System.out.println("Login successful");
        } else {
            System.out.println("Login failed");
        }

    }
//
//    @Test
//    @Order(0)
//    void createAccount() {
//        // Tạo một tài khoản test
//        AccountDTO registerAccount = AccountDTO.builder()
//                .username("admin")
//                .password("admin")
//                .roleId(1)
//                .build();
//
//        // Kiểm tra xem tài khoản đã được tạo thành công chưa
//        assertNotNull(registerAccount);
//        authBUS.register("admin", "admin", 1);
//    }

    @Test
    @Order(1)
    void insert_validAccount_accountIsAdded() throws SQLException {
        boolean result = accountBUS.insert(testAccount);
        assertTrue(result);

        // Kiểm tra xem account đã được thêm vào database chưa
        AccountDTO retrievedAccount = getAccountFromDatabase(testAccount.getUsername());
        assertNotNull(retrievedAccount);
        assertEquals(testAccount.getUsername(), retrievedAccount.getUsername());
        testAccountId = retrievedAccount.getAccountId();
    }

    @Test
    @Order(2)
    void insert_existingAccount_throwsAlreadyExistsException() {
        AccountDTO existingAccount = AccountDTO.builder()
                .username("testuser")
                .password("testpassword")
                .roleId(40)
                .build();

        try {
            accountBUS.insert(existingAccount);
        } catch (AlreadyExistsException e) {
            // In ra stack trace của ngoại lệ
            log.error(e.getMessage());

            // Kiểm tra rằng ngoại lệ là đúng
            assertEquals("Tài khoản đã tồn tại", e.getMessage());
        }
    }



    @Test
    @Order(3)
    void getAccountByUsername_existingUser_returnsAccountDTO() throws SQLException {
        AccountDTO retrievedAccount = accountBUS.findByUsername("testuser");
        assertNotNull(retrievedAccount);
        assertEquals("testuser", retrievedAccount.getUsername());
    }

    @Test
    @Order(4)
    void update_existingAccount_accountIsUpdated() throws SQLException {
        AccountDTO updatedAccount = AccountDTO.builder()
                .accountId(testAccountId)
                .username("updateduser")
                .password("updatedpassword")
                .roleId(1)
                .build();
        boolean result = accountBUS.update(updatedAccount);
        assertTrue(result);

        // Kiểm tra xem account đã được cập nhật trong database chưa
        AccountDTO retrievedAccount = getAccountFromDatabase("updateduser");
        assertNotNull(retrievedAccount);
        assertEquals("updateduser", retrievedAccount.getUsername());
        assertEquals(1, retrievedAccount.getRoleId());
        testAccount = retrievedAccount; // Cập nhật testAccount cho các test sau
    }

    @Test
    @Order(5)
    void getAccountById_existingUser_returnsAccountDTO() throws SQLException {
        AccountDTO retrievedAccount = accountBUS.findById(testAccount.getAccountId());
        assertNotNull(retrievedAccount);
        assertEquals(testAccount.getUsername(), retrievedAccount.getUsername());
    }

    @Test
    @Order(6)
    void findAll_accountsExist_returnsListOfAccounts() throws SQLException {
        List<AccountDTO> accounts = accountBUS.findAll();
        assertFalse(accounts.isEmpty()); // Ít nhất có account test
    }

    @Test
    @Order(7)
    void delete_existingAccount_accountIsDeleted() throws SQLException {
        boolean result = accountBUS.delete(testAccount.getAccountId());
        assertTrue(result);

        // Kiểm tra xem account đã bị xóa khỏi database chưa
        AccountDTO retrievedAccount = getAccountFromDatabase(testAccount.getUsername());
        assertNull(retrievedAccount);
    }

    // Helper method để lấy account từ database (sử dụng DAO trực tiếp cho mục đích test)
    private AccountDTO getAccountFromDatabase(String username) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            return daoFactory.getAccountDAO().findByUsername(username, conn);
        }
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        // Dọn dẹp database test nếu cần (ví dụ: xóa tất cả dữ liệu sau khi test)
        // Lưu ý: Cần cẩn thận khi thực hiện việc này
    }
}