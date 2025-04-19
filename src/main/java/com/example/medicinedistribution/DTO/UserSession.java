package com.example.medicinedistribution.DTO;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;

@Slf4j
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

