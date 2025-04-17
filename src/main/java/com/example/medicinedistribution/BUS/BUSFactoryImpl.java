package com.example.medicinedistribution.BUS;
import com.example.medicinedistribution.BUS.Interface.AccountBUS;
import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.BUS.Interface.RoleBUS;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DTO.UserSession;

import javax.sql.DataSource;

public class BUSFactoryImpl extends BUSFactory{
    private final DataSource dataSource;
    private final DAOFactory mySQLDAOFactory;
    private final TransactionManager transactionManager;
    private final UserSession userSession;

    public BUSFactoryImpl(DataSource dataSource,
                          DAOFactory mySQLDAOFactory,
                          TransactionManager transactionManager,
                          UserSession userSession) {
        this.dataSource = dataSource;
        this.mySQLDAOFactory = mySQLDAOFactory;
        this.transactionManager = transactionManager;
        this.userSession = userSession;
    }

    @Override
    public AccountBUS getAccountBUS() {
        return new AccountBUSImpl(dataSource, mySQLDAOFactory.getAccountDAO(),userSession);
    }

    @Override
    public RoleBUS getRoleBUS() {
        return new RoleBUSImpl(dataSource, mySQLDAOFactory.getRoleDAO(), mySQLDAOFactory.getRolePermDAO(), transactionManager, userSession, mySQLDAOFactory.getPermissionDAO());
    }

    @Override
    public AuthBUS getAuthBUS() {
        RoleBUS roleBUS = getRoleBUS();
        return new AuthBUSImpl(dataSource, mySQLDAOFactory.getAccountDAO(), roleBUS,userSession);
    }

}
