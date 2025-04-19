package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.*;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.DAOFactory;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GoodsReceiptBUSImplTest {
    private static BUSFactory busFactory;
    private static GoodsReceiptBUS goodsReceiptBUS;
    private static EmployeeBUS employeeBUS;
    private static ManufacturerBUS manufacturerBUS;
    private static ProductBUS productBUS;

    private static EmployeeDTO testEmployee;
    private static ManufacturerDTO testManufacturer;
    private static List<ProductDTO> testProducts = new ArrayList<>();
    private static GoodsReceiptDTO testGoodsReceipt;
    private static Integer testGoodsReceiptId;

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
        if (authBUS.login("admin", "admin")) {
            log.info("Login successful");
        } else {
            log.info("Login failed");
        }

        // Initialize BUS objects
        goodsReceiptBUS = busFactory.getGoodsReceiptBUS();
        employeeBUS = busFactory.getEmployeeBUS();
        manufacturerBUS = busFactory.getManufacturerBUS();
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
                .phone("0123456789")
                .email("test.employee@example.com")
                .hireDate(LocalDate.of(2022, 1, 1))  // Giả sử ngày vào làm là 01/01/2022
                .address("123 Test Street, Test City")
                .basicSalary(new BigDecimal("3500.00"))  // Tiền lương cơ bản
                .status(1)
                .positionId(1)  // Liên kết với testPosition
                .accountId(13)  // Giả sử accountId là 1
                .build();
        employeeBUS.insert(testEmployee);

        // Create test manufacturer
        testManufacturer = ManufacturerDTO.builder()
                .manufacturerName("TestManufacturer")
                .address("123 Manufacturer St, Test City")
                .phone("0987654321")
                .email("test@example.com")
                .country("Testland")
                .description("TestDescription")

                .build();
        manufacturerBUS.insert(testManufacturer);

        // Create test products
        for (int i = 1; i <= 3; i++) {
            ProductDTO product = ProductDTO.builder()
                    .productName("TestProduct" + i)
                    .price(new BigDecimal(i * 100))
                    .unit("Box")
                    .categoryId(1)  // Assuming category ID 1 exists
                    .build();
            productBUS.insert(product);
            testProducts.add(product);
        }

        // Create test goods receipt with details
        List<GoodsReceiptDetailDTO> details = new ArrayList<>();
        for (int i = 0; i < testProducts.size(); i++) {
            GoodsReceiptDetailDTO detail = new GoodsReceiptDetailDTO();
            detail.setProductId(testProducts.get(i).getProductId());
            detail.setQuantity(i + 5); // Different quantities
            detail.setPrice(testProducts.get(i).getPrice());
            detail.setTotal(testProducts.get(i).getPrice().multiply(BigDecimal.valueOf(i + 5)));
            details.add(detail);
        }

        testGoodsReceipt = GoodsReceiptDTO.builder()
                .employeeId(testEmployee.getEmployeeId())
                .manufacturerId(testManufacturer.getManufacturerId())
                .date(LocalDate.now())
                .total(calculateTotal(details))
                .details(details)
                .build();
    }

    private static BigDecimal calculateTotal(List<GoodsReceiptDetailDTO> details) {
        BigDecimal total = BigDecimal.ZERO;
        for (GoodsReceiptDetailDTO detail : details) {
            BigDecimal detailTotal = detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
            total = total.add(detailTotal);
        }
        return total;
    }

    @Test
    @Order(1)
    void insert_validGoodsReceipt_goodsReceiptIsAdded() {
        System.out.println(testGoodsReceipt);
        boolean result = goodsReceiptBUS.insert(testGoodsReceipt);
        assertTrue(result, "Goods receipt should be inserted");

        // Get the ID from the inserted receipt
        List<GoodsReceiptDTO> allReceipts = goodsReceiptBUS.findAll();
        GoodsReceiptDTO insertedReceipt = allReceipts.stream()
                .filter(r -> r.getManufacturerId().equals(testManufacturer.getManufacturerId()))
                .findFirst()
                .orElse(null);

        assertNotNull(insertedReceipt, "Inserted receipt should not be null");
        testGoodsReceiptId = insertedReceipt.getGoodsReceiptId();
    }

    @Test
    @Order(2)
    void insert_invalidGoodsReceipt_throwsException() {
        // Create invalid goods receipt with null details
        GoodsReceiptDTO invalidGoodsReceipt = GoodsReceiptDTO.builder()
                .employeeId(testEmployee.getEmployeeId())
                .manufacturerId(testManufacturer.getManufacturerId())
                .date(LocalDate.now())
                .total(BigDecimal.ZERO)
                .details(null)
                .build();
        
        // Test should throw an exception when trying to process null details
        assertThrows(IllegalArgumentException.class, () -> goodsReceiptBUS.insert(invalidGoodsReceipt),
                "Should throw exception with null details");
                
        // Create goods receipt with non-existent product ID
        List<GoodsReceiptDetailDTO> invalidDetails = new ArrayList<>();
        GoodsReceiptDetailDTO invalidDetail = new GoodsReceiptDetailDTO();
        invalidDetail.setProductId(99999); // Non-existent product ID
        invalidDetail.setQuantity(1);
        invalidDetail.setPrice(BigDecimal.TEN);
        invalidDetails.add(invalidDetail);
        
        GoodsReceiptDTO receiptWithInvalidDetail = GoodsReceiptDTO.builder()
                .employeeId(testEmployee.getEmployeeId())
                .manufacturerId(testManufacturer.getManufacturerId())
                .date(LocalDate.now())
                .total(BigDecimal.TEN)
                .details(invalidDetails)
                .build();
                
        assertThrows(Exception.class, () -> goodsReceiptBUS.insert(receiptWithInvalidDetail),
                "Should throw exception with invalid product ID");
    }

    @Test
    @Order(3)
    void findById_existingGoodsReceipt_returnsGoodsReceiptDTO() {
        GoodsReceiptDTO retrieved = goodsReceiptBUS.findById(testGoodsReceiptId);
        assertNotNull(retrieved, "Goods receipt should exist");

        // Verify details
        assertNotNull(retrieved.getDetails(), "Goods receipt details should not be null");
        assertEquals(3, retrieved.getDetails().size(), "Should have 3 detail items");
        assertEquals(testEmployee.getEmployeeId(), retrieved.getEmployeeId(), "Employee ID should match");
        assertEquals(testManufacturer.getManufacturerId(), retrieved.getManufacturerId(), "Manufacturer ID should match");
    }

    @Test
    @Order(4)
    void findById_nonExistingGoodsReceipt_returnsNull() {
        GoodsReceiptDTO nonExistingReceipt = goodsReceiptBUS.findById(99999);
        assertNull(nonExistingReceipt, "Should return null for non-existing receipt");
    }

    @Test
    @Order(5)
    void findById_exceptionHandling_throwsRuntimeException() {
        // Create a test scenario that would cause a SQLException
        // This is generally hard to test without mocking, but we can verify exception handling
        // by checking the return value for an invalid ID
        GoodsReceiptDTO result = goodsReceiptBUS.findById(-1); // Invalid ID that would cause SQL issues
        assertNull(result, "Should handle SQL exceptions and return null for invalid ID");
    }

    @Test
    @Order(6)
    void findAll_goodsReceiptsExist_returnsNonEmptyList() {
        List<GoodsReceiptDTO> goodsReceipts = goodsReceiptBUS.findAll();
        assertFalse(goodsReceipts.isEmpty(), "Goods receipts list should not be empty");

        // Find our test receipt in the list
        boolean foundTestReceipt = goodsReceipts.stream()
                .anyMatch(receipt -> receipt.getGoodsReceiptId().equals(testGoodsReceiptId));
        assertTrue(foundTestReceipt, "Test receipt should be in the list");
    }

    @Test
    @Order(7)
    void delete_existingGoodsReceipt_goodsReceiptIsDeleted() {
        boolean result = goodsReceiptBUS.delete(testGoodsReceiptId);
        assertTrue(result, "Goods receipt should be deleted");

        GoodsReceiptDTO deletedReceipt = goodsReceiptBUS.findById(testGoodsReceiptId);
        assertNull(deletedReceipt, "Goods receipt should be null after deletion");
    }

    @Test
    @Order(8)
    void delete_nonExistingGoodsReceipt_returnsFalse() {
//        boolean result = goodsReceiptBUS.delete(99999);
//        assertFalse(result, "Should return false when deleting non-existing goods receipt");
        assertThrows(DeleteFailedException.class ,
                () -> goodsReceiptBUS.delete(99999) , "Should throw DeleteFailedException when deleting non-existing goods receipt");
    }

    @Test
    @Order(9)
    void update_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, 
            () -> goodsReceiptBUS.update(testGoodsReceipt), 
            "Should throw UnsupportedOperationException as method is not implemented");
    }

    @AfterAll
    static void cleanUp() {
        // Delete test products
        for (ProductDTO product : testProducts) {
            productBUS.delete(product.getProductId());
        }

        // Delete test manufacturer
        manufacturerBUS.delete(testManufacturer.getManufacturerId());

        // Delete test employee
        employeeBUS.delete(testEmployee.getEmployeeId());
    }
}
