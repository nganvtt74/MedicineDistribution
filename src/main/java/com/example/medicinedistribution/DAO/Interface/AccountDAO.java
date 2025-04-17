package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.AccountDTO;

import java.sql.Connection;

public interface AccountDAO extends BaseDAO<AccountDTO,Integer>{
    AccountDTO findByUsername(String username, Connection conn);
}
