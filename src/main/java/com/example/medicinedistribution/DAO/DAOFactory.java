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
    public abstract CategoryDAO getCategoryDAO();
    public abstract ManufacturerDAO getManufacturerDAO();
    public abstract ProductDAO getProductDAO();
    public abstract InvoiceDAO getInvoiceDAO();
    public abstract InvoiceDetailDAO getInvoiceDetailDAO();
    public abstract GoodsReceiptDAO getGoodsReceiptDAO();
    public abstract GoodsReceiptDetailDAO getGoodsReceiptDetailDAO();
    public abstract CustomerDAO getCustomerDAO();
    public abstract PositionHistoryDAO getPositionHistoryDAO();
    public abstract DependentsDAO getDependentsDAO();
}
