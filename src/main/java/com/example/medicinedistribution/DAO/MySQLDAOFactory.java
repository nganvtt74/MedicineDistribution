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
    @Override
    public CategoryDAO getCategoryDAO() {
        return new CategoryDAOImpl();
    }
    @Override
    public ManufacturerDAO getManufacturerDAO() {
        return new ManufacturerDAOImpl();
    }
    @Override
    public ProductDAO getProductDAO() {
        return new ProductDAOImpl();
    }
    @Override
    public InvoiceDAO getInvoiceDAO() {
        return new InvoiceDAOImpl();
    }
    @Override
    public InvoiceDetailDAO getInvoiceDetailDAO() {
        return new InvoiceDetailDAOImpl();
    }
    @Override
    public GoodsReceiptDAO getGoodsReceiptDAO() {
        return new GoodsReceiptDAOImpl();
    }
    @Override
    public GoodsReceiptDetailDAO getGoodsReceiptDetailDAO() {
        return new GoodsReceiptDetailDAOImpl();
    }
    @Override
    public CustomerDAO getCustomerDAO() {
        return new CustomerDAOImpl();
    }
    @Override
    public PositionHistoryDAO getPositionHistoryDAO() {
        return new PositionHistoryDAOImpl();
    }

    @Override
    public DependentsDAO getDependentsDAO() {
        return new DependentsDAOImpl();
    }
    @Override
    public AllowanceDAO getAllowanceDAO() {
        return new AllowanceDAOImpl();
    }
    @Override
    public BonusTypeDAO getBonusTypeDAO() {
        return new BonusTypeDAOImpl();
    }
    @Override
    public BonusDAO getBonusDAO() {
        return new BonusDAOImpl();
    }

}