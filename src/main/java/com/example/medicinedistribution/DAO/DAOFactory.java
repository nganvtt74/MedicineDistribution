package com.example.medicinedistribution.DAO;

import com.example.medicinedistribution.DAO.Interface.AccountDAO;
import com.example.medicinedistribution.DAO.Interface.PermissionDAO;
import com.example.medicinedistribution.DAO.Interface.RoleDAO;
import com.example.medicinedistribution.DAO.Interface.RolePermDAO;

import java.sql.SQLException;

public abstract class DAOFactory {
    public abstract AccountDAO getAccountDAO();
    public abstract RoleDAO getRoleDAO();
    public abstract RolePermDAO getRolePermDAO();
    public abstract PermissionDAO getPermissionDAO();
}
