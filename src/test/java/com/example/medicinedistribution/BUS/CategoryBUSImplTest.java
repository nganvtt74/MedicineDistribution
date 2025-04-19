package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.BUS.Interface.CategoryBUS;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.DBConnection;

import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.AccountDTO;
import com.example.medicinedistribution.DTO.CategoryDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.UpdateFailedException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.*;



import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryBUSImplTest {
    private static DataSource dataSource;
    private static DAOFactory daoFactory;
    private static BUSFactory busFactory;
    private static CategoryBUS categoryBUS;
    private static CategoryDTO testCategory;
    private static int testCategoryId;



    @BeforeAll
    static void setUpAll() {
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
        login();
        categoryBUS = busFactory.getCategoryBUS();
        testCategory = CategoryDTO.builder()
                .categoryName("testCategory")
                .build();

    }

    @BeforeEach
    void setUp() {
    }

    @Test
    @Order(1)
    void insert_validCategory_categoryIsAdded() {
        boolean result = categoryBUS.insert(testCategory);
        assertTrue(result);
        CategoryDTO inserted = categoryBUS.findById(testCategory.getCategoryId());
        assertNotNull(inserted);
        testCategoryId = inserted.getCategoryId();
    }

    @Test
    @Order(6)
    void insert_nullCategory_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            categoryBUS.insert(null);
        });
    }

    @Test
    @Order(7)
    void insert_emptyName_throwsException() {
        CategoryDTO invalidCategory = CategoryDTO.builder()
                .categoryName("")
                .build();
        assertThrows(IllegalArgumentException.class, () -> {
            categoryBUS.insert(invalidCategory);
        });
    }

    @Test
    @Order(2)
    void findById_existingCategory_returnsCategoryDTO() {
        CategoryDTO retrieved = categoryBUS.findById(testCategoryId);
        assertNotNull(retrieved);
        assertEquals("testCategory", retrieved.getCategoryName());
    }

    @Test
    @Order(8)
    void findById_nonExistentId_returnsNull() {
        CategoryDTO result = categoryBUS.findById(99999);
        assertNull(result);
    }

    @Test
    @Order(3)
    void update_existingCategory_categoryIsUpdated() {
        CategoryDTO updatedCategory = CategoryDTO.builder()
                .categoryId(testCategoryId)
                .categoryName("UpdatedCategory")
                .build();
        boolean result = categoryBUS.update(updatedCategory);
        assertTrue(result);

        CategoryDTO retrieved = categoryBUS.findById(testCategoryId);
        assertEquals("UpdatedCategory", retrieved.getCategoryName());
    }

    @Test
    @Order(9)
    void update_nonExistentCategory_returnsFalse() {
        CategoryDTO nonExistentCategory = CategoryDTO.builder()
                .categoryId(99999)
                .categoryName("NonExistent")
                .build();
        assertThrows(RuntimeException.class, () -> {
            categoryBUS.update(nonExistentCategory);
        });
    }

    @Test
    @Order(10)
    void update_nullCategory_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            categoryBUS.update(null);
        });
    }

    @Test
    @Order(4)
    void findAll_categoriesExist_returnsNonEmptyList() {
        List<CategoryDTO> categories = categoryBUS.findAll();
        assertFalse(categories.isEmpty());
    }

    @Test
    @Order(5)
    void delete_existingCategory_categoryIsDeleted() {
        boolean result = categoryBUS.delete(testCategoryId);
        assertTrue(result);
        CategoryDTO deleted = categoryBUS.findById(testCategoryId);
        assertNull(deleted, "Category should be deleted");
    }

    @Test
    @Order(11)
    void delete_nonExistentId_returnsFalse() {
//        assertFalse(categoryBUS.delete(99999));
        assertThrows(DeleteFailedException.class,
                ()-> categoryBUS.delete(99999));
    }

    @Test
    @Order(12)
    void insert_invalidCategoryName_throwsException() {
        CategoryDTO invalidCategory = CategoryDTO.builder()
                .categoryName("") // Invalid: Blank
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoryBUS.insert(invalidCategory);
        });
        assertEquals("Tên danh mục không được để trống", exception.getMessage());
    }

    @Test
    @Order(13)
    void update_invalidCategoryName_throwsException() {
        CategoryDTO invalidCategory = CategoryDTO.builder()
                .categoryId(testCategoryId)
                .categoryName("") // Invalid: Blank
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoryBUS.update(invalidCategory);
        });
        assertEquals("Tên danh mục không được để trống", exception.getMessage());
    }

    @AfterEach
    void tearDown() {
    }

    static void login(){
        AuthBUS authBUS = busFactory.getAuthBUS();
        if (authBUS.login("admin", "admin")){
            System.out.println("Login successful");
        } else {
            System.out.println("Login failed");
        }
    }
}

