package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.BUS.Interface.ManufacturerBUS;
import com.example.medicinedistribution.DAO.DBConnection;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DAO.MySQLDAOFactory;
import com.example.medicinedistribution.DTO.ManufacturerDTO;
import com.example.medicinedistribution.DTO.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import javax.sql.DataSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ManufacturerBUSImplTest {
    private static BUSFactory busFactory;
    private static ManufacturerBUS manufacturerBUS;
    private static ManufacturerDTO testManufacturer;
    private static int testManufacturerId;

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

        manufacturerBUS = busFactory.getManufacturerBUS();
        testManufacturer = ManufacturerDTO.builder()
                .manufacturerName("TestManufacturer")
                .build();
    }

    @Test
    @Order(1)
    void insert_validManufacturer_manufacturerIsAdded() {
        boolean result = manufacturerBUS.insert(testManufacturer);
        assertTrue(result, "Manufacturer should be inserted");
        ManufacturerDTO inserted = manufacturerBUS.findById(testManufacturer.getManufacturerId());
        assertNotNull(inserted, "Inserted manufacturer should not be null");
        testManufacturerId = inserted.getManufacturerId();
    }

    @Test
    @Order(2)
    void findById_existingManufacturer_returnsManufacturerDTO() {
        ManufacturerDTO retrieved = manufacturerBUS.findById(testManufacturerId);
        assertNotNull(retrieved, "Manufacturer should exist");
        assertEquals("TestManufacturer", retrieved.getManufacturerName());
    }

    @Test
    @Order(3)
    void update_existingManufacturer_manufacturerIsUpdated() {
        ManufacturerDTO updated = ManufacturerDTO.builder()
                .manufacturerId(testManufacturerId)
                .manufacturerName("UpdatedManufacturer")
                .build();
        System.out.println("Manufacturer updated: " + updated);
        boolean result = manufacturerBUS.update(updated);
        assertTrue(result, "Manufacturer should be updated");

        ManufacturerDTO retrieved = manufacturerBUS.findById(testManufacturerId);
        assertEquals("UpdatedManufacturer", retrieved.getManufacturerName());
    }

    @Test
    @Order(4)
    void findAll_manufacturersExist_returnsNonEmptyList() {
        List<ManufacturerDTO> manufacturers = manufacturerBUS.findAll();
        assertFalse(manufacturers.isEmpty(), "Manufacturers should not be empty");
    }

    @Test
    @Order(5)
    void delete_existingManufacturer_manufacturerIsDeleted() {
        boolean result = manufacturerBUS.delete(testManufacturerId);
        assertTrue(result, "Manufacturer should be deleted");
        ManufacturerDTO deleted = manufacturerBUS.findById(testManufacturerId);
        assertNull(deleted, "Manufacturer should be null after deletion");
    }
}