package com.example.medicinedistribution.BUS;
import com.example.medicinedistribution.BUS.Interface.*;

public abstract class BUSFactory {
    public abstract AccountBUS getAccountBUS();
    public abstract RoleBUS getRoleBUS();
    public abstract AuthBUS getAuthBUS();
    public abstract DepartmentBUS getDepartmentBUS();
    public abstract PositionBUS getPositionBUS();
    public abstract EmployeeBUS getEmployeeBUS();



//    public abstract MedicineBUS getMedicineBUS();
//
//    public abstract SupplierBUS getSupplierBUS();
//
//    public abstract OrderBUS getOrderBUS();
//
//    public abstract OrderDetailBUS getOrderDetailBUS();
//
//    public abstract UserBUS getUserBUS();
//
//    public abstract RoleBUS getRoleBUS();
//
    public abstract CategoryBUS getCategoryBUS();
    public abstract ProductBUS getProductBUS();
    public abstract CustomerBUS getCustomerBUS();
    public abstract ManufacturerBUS getManufacturerBUS();
}
