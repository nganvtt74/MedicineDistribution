package com.example.medicinedistribution.DTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {
    private AccountDTO account;
    private RoleDTO role;
    private HashMap<String ,PermissionDTO> permissions;

    public boolean hasPermission(String permissionCode) {
        return permissions.containsKey(permissionCode);
    }
}

