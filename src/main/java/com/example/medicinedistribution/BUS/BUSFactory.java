package com.example.medicinedistribution.BUS;
import com.example.medicinedistribution.BUS.Interface.*;
import com.example.medicinedistribution.DAO.DAOFactory;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.GUI.PayRollController;

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
    public abstract AllowanceBUS getAllowanceBUS();
    public abstract BonusBUS getBonusBUS();
    public abstract BonusTypeBUS getBonusTypeBUS();
    public abstract AttendanceBUS getAttendanceBUS();
    public abstract PayrollBUS getPayrollBUS();
    public abstract RequestBUS getRequestBUS();
    public abstract RequestTypeBUS getRequestTypeBUS();
}

