package com.example.medicinedistribution.BUS.Interface;

public interface AuthBUS {
    boolean login(String username, String password);

    boolean logout();

    boolean register(String username, String password, int role);

    boolean changePassword(String username, String oldPassword, String newPassword);

    boolean resetPassword(String username, String newPassword);

    boolean isAuthenticated();

}
