package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.BUS.Interface.CustomerBUS;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.CustomerDTO;
import com.example.medicinedistribution.DTO.UserSession;
import lombok.extern.slf4j.Slf4j;
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
        busFactory = new BUSFactoryImpl(dataSource, daoFactory, transactionManager, userSession);

        AuthBUS authBUS = busFactory.getAuthBUS();
        authBUS.login("admin", "admin");

        customerBUS = busFactory.getCustomerBUS();
        testCustomer = CustomerDTO.builder()
                .customerName("TestCustomer")
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
}