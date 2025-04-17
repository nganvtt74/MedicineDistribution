package com.example.medicinedistribution.DTO;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Integer accountId;
    private String username;
    private String password;
    private Integer roleId;
}