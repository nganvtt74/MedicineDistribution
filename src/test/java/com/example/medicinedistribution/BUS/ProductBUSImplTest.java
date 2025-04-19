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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.sql.DataSource;
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
        busFactory = new BUSFactoryImpl(dataSource, daoFactory, transactionManager, userSession);

        AuthBUS authBUS = busFactory.getAuthBUS();
        authBUS.login("admin", "admin");

        productBUS = busFactory.getProductBUS();
        testProduct = ProductDTO.builder()
                .productName("TestProduct")
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
}