package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.InvoiceDetailDTO;

import java.sql.Connection;
import java.util.List;

public interface InvoiceDetailDAO extends BaseDAO<InvoiceDetailDTO, Integer> {
    public List<InvoiceDetailDTO> findByInvoiceId(Integer invoiceId, Connection conn);
}
