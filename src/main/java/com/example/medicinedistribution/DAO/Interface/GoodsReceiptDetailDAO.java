package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.GoodsReceiptDetailDTO;

import java.sql.Connection;
import java.util.List;

public interface GoodsReceiptDetailDAO extends BaseDAO<GoodsReceiptDetailDTO, Integer> {
    public List<GoodsReceiptDetailDTO> findByGoodsReceiptId(Integer goodsReceiptId, Connection conn);

}
