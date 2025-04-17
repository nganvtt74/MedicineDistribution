package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.*;

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
    @Override
    public PositionDAO getPositionDAO() {
        return new PositionDAOImpl();
    }
    @Override
    public EmployeeDAO getEmployeeDAO() {
        return new EmployeeDAOImpl();
    }
    @Override
    public DepartmentDAO getDepartmentDAO() {
        return new DepartmentDAOImpl();
    }
}