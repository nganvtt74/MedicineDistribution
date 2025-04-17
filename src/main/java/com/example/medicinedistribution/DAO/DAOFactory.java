package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.*;

import java.sql.SQLException;

public abstract class DAOFactory {
    public abstract AccountDAO getAccountDAO();
    public abstract RoleDAO getRoleDAO();
    public abstract RolePermDAO getRolePermDAO();
    public abstract PermissionDAO getPermissionDAO();
    public abstract PositionDAO getPositionDAO();
    public abstract EmployeeDAO getEmployeeDAO();
    public abstract DepartmentDAO getDepartmentDAO();

}
