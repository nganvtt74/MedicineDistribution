package com.example.medicinedistribution.BUS;
import com.example.medicinedistribution.BUS.Interface.*;
import com.example.medicinedistribution.DTO.UserSession;

public abstract class BUSFactory {
    public abstract AccountBUS getAccountBUS();
    public abstract RoleBUS getRoleBUS();
    public abstract AuthBUS getAuthBUS();
    public abstract DepartmentBUS getDepartmentBUS();
    public abstract PositionBUS getPositionBUS();
    public abstract EmployeeBUS getEmployeeBUS();
    public abstract CategoryBUS getCategoryBUS();
    public abstract ProductBUS getProductBUS();
    public abstract CustomerBUS getCustomerBUS();
    public abstract ManufacturerBUS getManufacturerBUS();
    public abstract GoodsReceiptBUS getGoodsReceiptBUS();
    public abstract InvoiceBUS getInvoiceBUS();
    public abstract UserSession getUserSession();
    public abstract StatisticsBUS getStatisticsBUS();
    public abstract DependentsBUS getDependentsBUS();
}
