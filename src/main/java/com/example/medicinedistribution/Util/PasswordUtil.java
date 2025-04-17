package com.example.medicinedistribution.Util;
import at.favre.lib.crypto.bcrypt.BCrypt;


public class PasswordUtil {

    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12,password.toCharArray());
    }

    // Kiểm tra mật khẩu nhập vào có khớp với mật khẩu đã lưu không
    public static boolean checkPassword(String inputPassword, String storedHash) {
        return BCrypt.verifyer().verify(inputPassword.toCharArray(), storedHash).verified;
    }
}
