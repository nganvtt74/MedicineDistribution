package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.AccountDTO;

public interface AccountBUS extends BaseBUS<AccountDTO, Integer> {
    AccountDTO findByUsername(String username);
}
