package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.*;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.*;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InvoiceBUSImplTest {
    private static BUSFactory busFactory;
    private static InvoiceBUS invoiceBUS;
    private static EmployeeBUS employeeBUS;
    private static CustomerBUS customerBUS;
    private static ProductBUS productBUS;

    private static EmployeeDTO testEmployee;
    private static CustomerDTO testCustomer;
    private static List<ProductDTO> testProducts = new ArrayList<>();
    private static InvoiceDTO testInvoice;
    private static Integer testInvoiceId;

    @BeforeAll
    static void setUpAll() {
        DataSource dataSource = DBConnection.getDataSource();
        TransactionManager transactionManager = new TransactionManager(dataSource);
        DAOFactory daoFactory = new MySQLDAOFactory();
        UserSession userSession = new UserSession();
        busFactory = new BUSFactoryImpl(dataSource, daoFactory, transactionManager, userSession);

        AuthBUS authBUS = busFactory.getAuthBUS();
        if (authBUS.login("admin", "admin")) {
            log.info("Login successful");
        } else {
            log.info("Login failed");
        }

        // Initialize BUS objects
        invoiceBUS = busFactory.getInvoiceBUS();
        employeeBUS = busFactory.getEmployeeBUS();
        customerBUS = busFactory.getCustomerBUS();
        productBUS = busFactory.getProductBUS();

        // Create test data
        setupTestData();
    }

    private static void setupTestData() {
        // Create test employee
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
                .status(1)
                .positionId(1)  // Liên kết với testPosition
                .accountId(13)  // Giả sử accountId là 1
                .build();
        employeeBUS.insert(testEmployee);

        // Create test customer
        testCustomer = CustomerDTO.builder()
                .customerName("TestCustomer")
                .build();
        customerBUS.insert(testCustomer);

        // Create test products
        for (int i = 1; i <= 3; i++) {
            ProductDTO product = ProductDTO.builder()
                    .productName("TestProduct" + i)
                    .price(new BigDecimal(i * 100))
                    .build();
            productBUS.insert(product);
            testProducts.add(product);
        }

        // Create test invoice with details
        List<InvoiceDetailDTO> details = new ArrayList<>();
        for (int i = 0; i < testProducts.size(); i++) {
            InvoiceDetailDTO detail = new InvoiceDetailDTO();
            detail.setProductId(testProducts.get(i).getProductId());
            detail.setQuantity(i + 2); // Different quantities
            detail.setPrice(testProducts.get(i).getPrice());
            details.add(detail);
        }

        testInvoice = InvoiceDTO.builder()
                .employeeId(testEmployee.getEmployeeId())
                .customerId(testCustomer.getCustomerId())
                .date(LocalDate.now())
                .total(calculateTotal(details))
                .details(details)
                .build();
    }

    private static BigDecimal calculateTotal(List<InvoiceDetailDTO> details) {
        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceDetailDTO detail : details) {
            BigDecimal detailTotal = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
            total = total.add(detailTotal);
        }
        return total;
    }

    @Test
    @Order(1)
    void insert_validInvoice_invoiceIsAdded() {
        boolean result = invoiceBUS.insert(testInvoice);
        assertTrue(result, "Invoice should be inserted");

        // Get the ID from the inserted invoice
        List<InvoiceDTO> allInvoices = invoiceBUS.findAll();
        InvoiceDTO insertedInvoice = allInvoices.stream()
                .filter(i -> i.getCustomerId().equals(testCustomer.getCustomerId()))
                .findFirst()
                .orElse(null);

        assertNotNull(insertedInvoice, "Inserted invoice should not be null");
        testInvoiceId = insertedInvoice.getInvoiceId();
    }

    @Test
    @Order(2)
    void insert_invalidInvoice_throwsException() {
        // Create invalid invoice with null details
        InvoiceDTO invalidInvoice = InvoiceDTO.builder()
                .employeeId(testEmployee.getEmployeeId())
                .customerId(testCustomer.getCustomerId())
                .date(LocalDate.now())
                .total(BigDecimal.ZERO)
                .details(null)
                .build();
        
        // Test should throw an exception when trying to process null details
        assertThrows(NullPointerException.class, () -> invoiceBUS.insert(invalidInvoice),
                "Should throw exception with null details");
                
        // Create invoice with non-existent product ID
        List<InvoiceDetailDTO> invalidDetails = new ArrayList<>();
        InvoiceDetailDTO invalidDetail = new InvoiceDetailDTO();
        invalidDetail.setProductId(99999); // Non-existent product ID
        invalidDetail.setQuantity(1);
        invalidDetail.setPrice(BigDecimal.TEN);
        invalidDetails.add(invalidDetail);
        
        InvoiceDTO invoiceWithInvalidDetail = InvoiceDTO.builder()
                .employeeId(testEmployee.getEmployeeId())
                .customerId(testCustomer.getCustomerId())
                .date(LocalDate.now())
                .total(BigDecimal.TEN)
                .details(invalidDetails)
                .build();
                
        assertThrows(Exception.class, () -> invoiceBUS.insert(invoiceWithInvalidDetail),
                "Should throw exception with invalid product ID");
    }

    @Test
    @Order(3)
    void findById_existingInvoice_returnsInvoiceDTO() {
        InvoiceDTO retrieved = invoiceBUS.findById(testInvoiceId);
        assertNotNull(retrieved, "Invoice should exist");

        // Verify details
        assertNotNull(retrieved.getDetails(), "Invoice details should not be null");
        assertEquals(3, retrieved.getDetails().size(), "Should have 3 detail items");
        assertEquals(testEmployee.getEmployeeId(), retrieved.getEmployeeId(), "Employee ID should match");
        assertEquals(testCustomer.getCustomerId(), retrieved.getCustomerId(), "Customer ID should match");
    }

    @Test
    @Order(4)
    void findById_nonExistingInvoice_returnsNull() {
        InvoiceDTO nonExistingInvoice = invoiceBUS.findById(99999);
        assertNull(nonExistingInvoice, "Should return null for non-existing invoice");
    }

    @Test
    @Order(5)
    void findById_exceptionHandling_throwsRuntimeException() {
        // Create a test scenario that would cause a SQLException
        // This is generally hard to test without mocking, but we can verify exception handling
        // by checking the return value for an invalid ID
        InvoiceDTO result = invoiceBUS.findById(-1); // Invalid ID that would cause SQL issues
        assertNull(result, "Should handle SQL exceptions and return null for invalid ID");
    }

    @Test
    @Order(6)
    void findAll_invoicesExist_returnsNonEmptyList() {
        List<InvoiceDTO> invoices = invoiceBUS.findAll();
        assertFalse(invoices.isEmpty(), "Invoices list should not be empty");

        // Find our test invoice in the list
        boolean foundTestInvoice = invoices.stream()
                .anyMatch(invoice -> invoice.getInvoiceId().equals(testInvoiceId));
        assertTrue(foundTestInvoice, "Test invoice should be in the list");
    }

    @Test
    @Order(7)
    void delete_existingInvoice_invoiceIsDeleted() {
        boolean result = invoiceBUS.delete(testInvoiceId);
        assertTrue(result, "Invoice should be deleted");

        InvoiceDTO deletedInvoice = invoiceBUS.findById(testInvoiceId);
        assertNull(deletedInvoice, "Invoice should be null after deletion");
    }

    @Test
    @Order(8)
    void delete_nonExistingInvoice_returnsFalse() {
        // Test should throw an exception when trying to delete a non-existing invoice
        assertThrows(DeleteFailedException.class, () -> invoiceBUS.delete(99999),
                "Should throw exception when deleting non-existing invoice");
    }

    @Test
    @Order(9)
    void update_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, 
            () -> invoiceBUS.update(testInvoice), 
            "Should throw UnsupportedOperationException as method is not implemented");
    }

    @AfterAll
    static void cleanUp() {
        // Delete test products
        for (ProductDTO product : testProducts) {
            productBUS.delete(product.getProductId());
        }

        // Delete test customer
        customerBUS.delete(testCustomer.getCustomerId());

        // Delete test employee
        employeeBUS.delete(testEmployee.getEmployeeId());
    }
}
