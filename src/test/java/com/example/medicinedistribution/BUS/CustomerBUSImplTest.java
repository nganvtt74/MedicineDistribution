package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.BUS.Interface.CustomerBUS;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.CustomerDTO;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerBUSImplTest {
    private static BUSFactory busFactory;
    private static CustomerBUS customerBUS;
    private static CustomerDTO testCustomer;
    private static int testCustomerId;

    @BeforeAll
    static void setUpAll() {
        DataSource dataSource = DBConnection.getDataSource();
        TransactionManager transactionManager = new TransactionManager(dataSource);
        DAOFactory daoFactory = new MySQLDAOFactory();
        UserSession userSession = new UserSession();
                ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator()) // Sử dụng interpolator không yêu cầu EL
                .buildValidatorFactory();
        Validator validator = factory.getValidator();

        busFactory = new BUSFactoryImpl(dataSource, daoFactory , transactionManager, userSession,validator);
        AuthBUS authBUS = busFactory.getAuthBUS();
        authBUS.login("admin", "admin");

        customerBUS = busFactory.getCustomerBUS();
        testCustomer = CustomerDTO.builder()
                .customerName("TestCustomer")
                .address("TestAddress")
                .phone("0123456789")
                .email("test@test.com")
                .build();
    }

    @Test
    @Order(1)
    void insert_validCustomer_customerIsAdded() {
        boolean result = customerBUS.insert(testCustomer);
        assertTrue(result);
        CustomerDTO inserted = customerBUS.findById(testCustomer.getCustomerId());
        assertNotNull(inserted);
        testCustomerId = inserted.getCustomerId();
    }

    @Test
    @Order(2)
    void findById_existingCustomer_returnsCustomerDTO() {
        CustomerDTO retrieved = customerBUS.findById(testCustomerId);
        assertNotNull(retrieved);
        assertEquals("TestCustomer", retrieved.getCustomerName());
    }

    @Test
    @Order(3)
    void update_existingCustomer_customerIsUpdated() {
        CustomerDTO updated = CustomerDTO.builder()
                .customerId(testCustomerId)
                .customerName("UpdatedCustomer")
                .address("UpdatedAddress")
                .phone("0987654321")
                .email("test@test.com")
                .build();
        boolean result = customerBUS.update(updated);
        assertTrue(result);

        CustomerDTO retrieved = customerBUS.findById(testCustomerId);
        assertEquals("UpdatedCustomer", retrieved.getCustomerName());
    }

    @Test
    @Order(4)
    void findAll_customersExist_returnsNonEmptyList() {
        List<CustomerDTO> customers = customerBUS.findAll();
        assertFalse(customers.isEmpty());
    }

    @Test
    @Order(5)
    void delete_existingCustomer_customerIsDeleted() {
        boolean result = customerBUS.delete(testCustomerId);
        assertTrue(result);
        CustomerDTO deleted = customerBUS.findById(testCustomerId);
        assertNull(deleted);
    }

    @Test
    @Order(6)
    void insert_nullCustomer_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            customerBUS.insert(null);
        });
    }

    @Test
    @Order(7)
    void insert_emptyName_throwsException() {
        CustomerDTO invalidCustomer = CustomerDTO.builder()
                .customerName("")
                .build();
        assertThrows(IllegalArgumentException.class, () -> {
            customerBUS.insert(invalidCustomer);
        });
    }

    @Test
    @Order(8)
    void findById_nonExistentId_returnsNull() {
        CustomerDTO result = customerBUS.findById(99999);
        assertNull(result);
    }

    @Test
    @Order(9)
    void update_nonExistentCustomer_throwsException() {
        CustomerDTO nonExistentCustomer = CustomerDTO.builder()
                .customerId(99999)
                .customerName("NonExistent")
                .build();
        assertThrows(RuntimeException.class, () -> {
            customerBUS.update(nonExistentCustomer);
        });
    }

    @Test
    @Order(10)
    void update_nullCustomer_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            customerBUS.update(null);
        });
    }

    @Test
    @Order(11)
    void delete_nonExistentId_throwsException() {
        assertThrows(DeleteFailedException.class, () ->
            customerBUS.delete(99999)
        );
    }

    @Test
    @Order(12)
    void insert_invalidCustomerName_throwsException() {
        CustomerDTO invalidCustomer = CustomerDTO.builder()
                .customerName("") // Invalid: Blank
                .address("ValidAddress")
                .phone("0123456789")
                .email("valid.email@example.com")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerBUS.insert(invalidCustomer);
        });
        assertEquals("Tên khách hàng không được để trống", exception.getMessage());
    }

    @Test
    @Order(13)
    void insert_customerNameTooLong_throwsException() {
        CustomerDTO invalidCustomer = CustomerDTO.builder()
                .customerName("A".repeat(101)) // Invalid: Exceeds 100 characters
                .address("ValidAddress")
                .phone("0123456789")
                .email("valid.email@example.com")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerBUS.insert(invalidCustomer);
        });
        assertEquals("Tên khách hàng không được vượt quá 100 ký tự", exception.getMessage());
    }

    @Test
    @Order(14)
    void insert_invalidPhone_throwsException() {
        CustomerDTO invalidCustomer = CustomerDTO.builder()
                .customerName("ValidCustomer")
                .address("ValidAddress")
                .phone("12345") // Invalid: Does not match regex
                .email("valid.email@example.com")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerBUS.insert(invalidCustomer);
        });
        assertEquals("Số điện thoại không hợp lệ", exception.getMessage());
    }

    @Test
    @Order(15)
    void insert_invalidEmail_throwsException() {
        CustomerDTO invalidCustomer = CustomerDTO.builder()
                .customerName("ValidCustomer")
                .address("ValidAddress")
                .phone("0123456789")
                .email("invalid-email") // Invalid: Not a valid email format
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerBUS.insert(invalidCustomer);
        });
        assertEquals("Email không hợp lệ", exception.getMessage());
    }

    @Test
    @Order(16)
    void insert_invalidAddress_throwsException() {
        CustomerDTO invalidCustomer = CustomerDTO.builder()
                .customerName("ValidCustomer")
                .address("") // Invalid: Blank
                .phone("0123456789")
                .email("valid.email@example.com")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            customerBUS.insert(invalidCustomer);
        });
        assertEquals("Địa chỉ không được để trống", exception.getMessage());
    }
}
