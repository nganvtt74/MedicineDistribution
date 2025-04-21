package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.AccountDTO;
import com.example.medicinedistribution.DTO.RoleDTO;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public interface AccountDAO extends BaseDAO<AccountDTO,Integer>{
    AccountDTO findByUsername(String username, Connection conn);

    boolean updatePassword(AccountDTO updatedAccount, Connection connection);

    ArrayList<AccountDTO> getAccountByRoleId(List<RoleDTO> roleList, Connection connection);

    ArrayList<AccountDTO> getAccountByNullRoleId(Connection connection);
}
