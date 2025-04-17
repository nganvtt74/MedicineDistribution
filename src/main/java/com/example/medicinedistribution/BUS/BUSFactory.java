package com.example.medicinedistribution.BUS;
import com.example.medicinedistribution.BUS.Interface.AccountBUS;
import com.example.medicinedistribution.BUS.Interface.AuthBUS;
import com.example.medicinedistribution.BUS.Interface.RoleBUS;

public abstract class BUSFactory {
    public abstract AccountBUS getAccountBUS();
    public abstract RoleBUS getRoleBUS();
    public abstract AuthBUS getAuthBUS();

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
//    public abstract CategoryBUS getCategoryBUS();
}
