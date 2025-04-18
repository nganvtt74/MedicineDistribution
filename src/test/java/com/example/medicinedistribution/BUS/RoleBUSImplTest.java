package com.example.medicinedistribution.BUS;

    import com.example.medicinedistribution.BUS.Interface.AuthBUS;
    import com.example.medicinedistribution.BUS.Interface.RoleBUS;
    import com.example.medicinedistribution.DAO.DAOFactory;
    import com.example.medicinedistribution.DAO.DBConnection;
    import com.example.medicinedistribution.DAO.MySQLDAOFactory;
    import com.example.medicinedistribution.DTO.PermissionDTO;
    import com.example.medicinedistribution.DTO.RoleDTO;
    import com.example.medicinedistribution.DTO.UserSession;
    import com.example.medicinedistribution.Exception.AlreadyExistsException;
    import com.example.medicinedistribution.Exception.NotExistsException;
    import com.example.medicinedistribution.Util.GenericTablePrinter;
    import org.junit.jupiter.api.*;

    import javax.sql.DataSource;
    import java.util.ArrayList;
    import java.util.List;

    import static org.junit.jupiter.api.Assertions.*;

    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RoleBUSImplTest {

        private static DataSource dataSource;
        private static DAOFactory daoFactory;
        private static BUSFactory busFactory;
        private RoleBUS roleBUS;
        private static RoleDTO testRole;
        private static int testRoleId;
        private static PermissionDTO testPermission;
        private static List<PermissionDTO> permissions = new ArrayList<>();


        @BeforeAll
        static void setUpAll() {
            // Initialize the dataSource and required factories.
            dataSource = DBConnection.getDataSource();
            TransactionManager transactionManager = new TransactionManager(dataSource);
            daoFactory = new MySQLDAOFactory();
            UserSession userSession = new UserSession();
            busFactory = new BUSFactoryImpl(dataSource, daoFactory, transactionManager, userSession);
            // Create a unique test role.
            permissions.add(PermissionDTO.builder()
                    .permissionCode("TEST_CODE")
                    .permName("testPermission")
                    .editableByPermissionCode("MANAGE_ROLE")
                    .build());
            testRole = RoleDTO.builder()
                    .roleName("UniqueTestRole" + System.currentTimeMillis()) // ensure a unique name
                    .permissions(permissions)
                    .build();
            testPermission = PermissionDTO.builder()
                    .permissionCode("TEST_CODE_2")
                    .permName("testPermission")
                    .editableByPermissionCode("MANAGE_ROLE")
                    .build();

        }

        @BeforeEach
        void setUp() {
            roleBUS = busFactory.getRoleBUS();
            AuthBUS authBUS = busFactory.getAuthBUS();
            if (authBUS.login("admin", "admin")){
                System.out.println("Login successful");
            } else {
                System.out.println("Login failed");
            }
        }

        @Test
        @Order(1)
        void insertSuccess() {
            // Insert the test role successfully.
            boolean result = roleBUS.insert(testRole);
            assertTrue(result);
            // Retrieve the inserted role to verify.
            RoleDTO retrieved = roleBUS.findById(testRole.getRoleId());
            assertNotNull(retrieved);
            testRole.setRoleId(retrieved.getRoleId());
            System.out.println("Inserted role: " + retrieved);
        }

        @Test
        @Order(2)
        void insertDuplicateRoleThrowsException() {
            // Attempt to insert a role with the same name to trigger AlreadyExistsException.
            // Language: java
            try {
                RoleDTO duplicateRole = RoleDTO.builder()
                        .roleName(testRole.getRoleName())
                        .permissions(testRole.getPermissions())
                        .build();
                roleBUS.insert(duplicateRole);
                fail("Expected AlreadyExistsException but none was thrown.");
            } catch (AlreadyExistsException exception) {
                System.out.println("Duplicate insert exception: " + exception.getMessage());
            }
        }

        @Test
        @Order(3)
        void updateSuccess() {
            // Update the test role with a new role name.
            testRole.setRoleName("UpdatedTestRole" + System.currentTimeMillis());
            ArrayList<PermissionDTO> newPermissions = new ArrayList<>(permissions);
            newPermissions.add(PermissionDTO.builder()
                    .permissionCode("TEST_CODE_2")
                    .permName("testPermission")
                    .build());
            System.out.println("Permissions: " + newPermissions);
            testRole.setPermissions(newPermissions);
            boolean result = roleBUS.update(testRole);
            assertTrue(result);
            RoleDTO updatedRole = roleBUS.findById(testRole.getRoleId());
            assertEquals(testRole.getRoleName(), updatedRole.getRoleName());
            System.out.println("Updated role: " + updatedRole);
        }

        @Test
        @Order(4)
        void updateNonexistentRoleThrowsException() {
            // Update a non-existent role (with an invalid id) to trigger NotExistsException.
            NotExistsException exception = assertThrows(NotExistsException.class, () -> {
                RoleDTO nonExistent = RoleDTO.builder()
                        .roleId(-1)
                        .roleName("NonExistentRole")
                        .permissions(new ArrayList<>())
                        .build();
                roleBUS.update(nonExistent);
            });
            System.out.println("Update non-existent exception: " + exception.getMessage());
        }

        @Test
        @Order(5)
        void findByIdNonexistentThrowsException() {
            // Attempt to find a role with an invalid id.
            NotExistsException exception = assertThrows(NotExistsException.class, () -> {
                roleBUS.findById(-1);
            });
            System.out.println("FindById non-existent exception: " + exception.getMessage());
        }

        @Test
        @Order(6)
        void findAllSuccess() {
            // Retrieve all roles and verify that at least one role is present.
            List<RoleDTO> roles = roleBUS.findAll();
            assertFalse(roles.isEmpty());
            System.out.println("Found roles: " + roles);
        }

        @Test
        @Order(7)
        void getRoleForEdit(){
            // Retrieve the role for editing and verify its properties.
            RoleDTO roleForEdit = roleBUS.getRoleForEdit(testRole.getRoleId());
            assertNotNull(roleForEdit);
            assertEquals(testRole.getRoleName(), roleForEdit.getRoleName());
//            printPermissions(roleForEdit.getPermissions());
            GenericTablePrinter.printTable(roleForEdit.getPermissions());
        }

        @Test
        @Order(8)
        void deleteSuccess() {
            // Delete the test role.
            boolean result = roleBUS.delete(testRole.getRoleId());
            assertTrue(result);
            // Verify deletion by attempting to find the deleted role.
            NotExistsException exception = assertThrows(NotExistsException.class, () -> {
                roleBUS.findById(testRole.getRoleId());
            });
            System.out.println("Delete verification exception: " + exception.getMessage());
        }

        @Test
        @Order(9)
        void deleteNonexistentThrowsException() {
            // Attempt to delete a role with an invalid id.
            NotExistsException exception = assertThrows(NotExistsException.class, () -> {
                roleBUS.delete(-1);
            });
            System.out.println("Delete non-existent exception: " + exception.getMessage());
        }

    }