package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.BUS.Interface.CustomerBUS;
import com.example.medicinedistribution.BUS.Interface.ProductBUS;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.CustomerDTO;
import com.example.medicinedistribution.DTO.ProductDTO;
import com.example.medicinedistribution.DTO.UserSession;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductBUSImplTest {
    private static BUSFactory busFactory;
    private static ProductBUS productBUS;
    private static ProductDTO testProduct;
    private static int testProductId;

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

        productBUS = busFactory.getProductBUS();
        testProduct = ProductDTO.builder()
                .productName("TestProduct")
                .price(BigDecimal.valueOf(1000))
                .unit("Box")
                .categoryId(1) // Assuming category ID 1 exists
                .build();
    }

    @Test
    @Order(1)
    void insert_validProduct_productIsAdded() {
        boolean result = productBUS.insert(testProduct);
        assertTrue(result);
        ProductDTO inserted = productBUS.findById(testProduct.getProductId());
        assertNotNull(inserted);
        testProductId = inserted.getProductId();
    }

    @Test
    @Order(2)
    void findById_existingProduct_returnsProductDTO() {
        ProductDTO retrieved = productBUS.findById(testProductId);
        assertNotNull(retrieved);
        assertEquals("TestProduct", retrieved.getProductName());
    }

    @Test
    @Order(3)
    void update_existingProduct_productIsUpdated() {
        ProductDTO updated = ProductDTO.builder()
                .productId(testProductId)
                .productName("UpdatedProduct")
                .price(BigDecimal.valueOf(2000))
                .unit("Bottle")
                .categoryId(1) // Assuming category ID 1 exists
                .build();
        boolean result = productBUS.update(updated);
        assertTrue(result);

        ProductDTO retrieved = productBUS.findById(testProductId);
        assertEquals("UpdatedProduct", retrieved.getProductName());
    }

    @Test
    @Order(4)
    void findAll_productsExist_returnsNonEmptyList() {
        List<ProductDTO> products = productBUS.findAll();
        assertFalse(products.isEmpty());
    }

    @Test
    @Order(5)
    void delete_existingProduct_productIsDeleted() {
        boolean result = productBUS.delete(testProductId);
        assertTrue(result);
        ProductDTO deleted = productBUS.findById(testProductId);
        assertNull(deleted);
    }
    @Test
    @Order(6)
    void insert_invalidProductName_throwsException() {
        ProductDTO invalidProduct = ProductDTO.builder()
                .productName("") // Invalid: Blank
                .price(BigDecimal.valueOf(1000))
                .unit("Box")
                .categoryId(1)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productBUS.insert(invalidProduct);
        });
        assertEquals("Tên sản phẩm không được để trống", exception.getMessage());
    }

    @Test
    @Order(7)
    void insert_invalidPrice_throwsException() {
        ProductDTO invalidProduct = ProductDTO.builder()
                .productName("InvalidProduct")
                .price(BigDecimal.valueOf(-1000)) // Invalid: Negative price
                .unit("Box")
                .categoryId(1)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productBUS.insert(invalidProduct);
        });
        assertEquals("Giá không được âm", exception.getMessage());
    }
}