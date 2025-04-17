package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.AccountDAO;
import com.example.medicinedistribution.DAO.Interface.PermissionDAO;
import com.example.medicinedistribution.DAO.Interface.RoleDAO;
import com.example.medicinedistribution.DAO.Interface.RolePermDAO;

import java.sql.SQLException;

public class MySQLDAOFactory extends DAOFactory {

    @Override
    public AccountDAO getAccountDAO(){
        return new AccountDAOImpl();
    }
    @Override
    public RoleDAO getRoleDAO(){
        return new RoleDAOImpl();
    }
    @Override
    public RolePermDAO getRolePermDAO(){
        return new RolePermDAOImpl();
    }
    @Override
    public PermissionDAO getPermissionDAO(){
        return new PermissionDAOImpl();
    }
}