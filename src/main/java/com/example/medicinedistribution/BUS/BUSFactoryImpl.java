package com.example.medicinedistribution.BUS;
import com.example.medicinedistribution.BUS.Interface.*;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DTO.UserSession;
import jakarta.validation.Validator;
import lombok.Getter;

import javax.sql.DataSource;

public class BUSFactoryImpl extends BUSFactory{
    private final DataSource dataSource;
    private final DAOFactory mySQLDAOFactory;
    private final TransactionManager transactionManager;
    @Getter
    private final UserSession userSession;
    private final Validator validator;

    public BUSFactoryImpl(DataSource dataSource,
                          DAOFactory mySQLDAOFactory,
                          TransactionManager transactionManager,
                          UserSession userSession,
                          Validator validator
                          ) {
        this.dataSource = dataSource;
        this.mySQLDAOFactory = mySQLDAOFactory;
        this.transactionManager = transactionManager;
        this.userSession = userSession;
        this.validator = validator;
    }

    @Override
    public AccountBUS getAccountBUS() {
        return new AccountBUSImpl(dataSource, mySQLDAOFactory.getAccountDAO(),userSession , validator);
    }

    @Override
    public RoleBUS getRoleBUS() {
        return new RoleBUSImpl(dataSource, mySQLDAOFactory.getRoleDAO(), mySQLDAOFactory.getRolePermDAO(),
                transactionManager, userSession, mySQLDAOFactory.getPermissionDAO() , validator);
    }

    @Override
    public AuthBUS getAuthBUS() {
        RoleBUS roleBUS = getRoleBUS();
        return new AuthBUSImpl(dataSource, mySQLDAOFactory.getAccountDAO(), roleBUS,userSession,mySQLDAOFactory.getEmployeeDAO());
    }

    @Override
    public DepartmentBUS getDepartmentBUS() {
        return new DepartmentBUSImpl( mySQLDAOFactory.getDepartmentDAO(), userSession, dataSource , validator);
    }
    @Override
    public PositionBUS getPositionBUS() {
        return new PositionBUSImpl( mySQLDAOFactory.getPositionDAO(),userSession, dataSource , validator);
    }

    @Override
    public EmployeeBUS getEmployeeBUS() {
        return new EmployeeBUSImpl( mySQLDAOFactory.getEmployeeDAO(), userSession, dataSource , validator ,
                mySQLDAOFactory.getPositionHistoryDAO(),transactionManager, mySQLDAOFactory.getDependentsDAO());
    }

    @Override
    public CategoryBUS getCategoryBUS() {
        return new CategoryBUSImpl(dataSource, mySQLDAOFactory.getCategoryDAO(), userSession , validator);
    }

    @Override
    public ProductBUS getProductBUS() {
        return new ProductBUSImpl(mySQLDAOFactory.getProductDAO(), userSession, dataSource , validator);
    }

    @Override
    public CustomerBUS getCustomerBUS() {
        return new CustomerBUSImpl(mySQLDAOFactory.getCustomerDAO(), dataSource, userSession , validator);
    }

    @Override
    public ManufacturerBUS getManufacturerBUS() {
        return new ManufacturerBUSImpl(mySQLDAOFactory.getManufacturerDAO(), dataSource, userSession, validator);
    }

    @Override
    public GoodsReceiptBUS getGoodsReceiptBUS() {
        return new GoodsReceiptBUSImpl(mySQLDAOFactory.getGoodsReceiptDAO(), mySQLDAOFactory.getGoodsReceiptDetailDAO(),
                dataSource, userSession, transactionManager , validator , mySQLDAOFactory.getProductDAO());
    }

    @Override
    public InvoiceBUS getInvoiceBUS() {
        return new InvoiceBUSImpl(mySQLDAOFactory.getInvoiceDAO(), mySQLDAOFactory.getInvoiceDetailDAO(),
                dataSource, userSession, transactionManager , validator , mySQLDAOFactory.getProductDAO());
    }

    @Override
    public StatisticsBUS getStatisticsBUS() {
        return new StatisticsBUSImpl(getInvoiceBUS(), getGoodsReceiptBUS(), userSession);
    }

    @Override
    public DependentsBUS getDependentsBUS() {
        return new DependentsBUSImpl(mySQLDAOFactory.getDependentsDAO(), userSession, dataSource , validator);
    }
    @Override
    public AllowanceBUS getAllowanceBUS() {
        return new AllowanceBUSImpl(mySQLDAOFactory.getAllowanceDAO(), dataSource, userSession , validator);
    }
    @Override
    public BonusBUS getBonusBUS() {
        return new BonusBUSImpl(mySQLDAOFactory.getBonusDAO(), dataSource, userSession , validator);
    }
    @Override
    public BonusTypeBUS getBonusTypeBUS() {
        return new BonusTypeBUSImpl(mySQLDAOFactory.getBonusTypeDAO(), dataSource, userSession , validator);
    }

}
