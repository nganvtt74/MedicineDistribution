package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.AccountDTO;
import com.example.medicinedistribution.DTO.RoleDTO;

import java.util.ArrayList;
import java.util.List;

public interface AccountBUS extends BaseBUS<AccountDTO, Integer> {
    AccountDTO findByUsername(String username);

    void resetPassword(AccountDTO updatedAccount);

    ArrayList<AccountDTO> getAccountByRoleId(List<RoleDTO> roleList);

    ArrayList<AccountDTO> getAccountByNullRoleId();
}
