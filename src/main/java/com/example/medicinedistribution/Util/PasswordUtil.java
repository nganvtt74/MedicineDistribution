package com.example.medicinedistribution.Util;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.example.medicinedistribution.DTO.EmployeeDTO;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class PasswordUtil {

    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12,password.toCharArray());
    }

    // Kiểm tra mật khẩu nhập vào có khớp với mật khẩu đã lưu không
    public static boolean checkPassword(String inputPassword, String storedHash) {
        return BCrypt.verifyer().verify(inputPassword.toCharArray(), storedHash).verified;
    }

    /**
     * Generates a default password combining a custom key with the employee's birth date
     * in ddMMyyyy format.
     *
     * @param key The custom key prefix for the password
     * @param employee The employee whose birth date will be used
     * @return The generated default password or null if employee or birth date is null
     */
    public static String generateDefaultPassword(String key, EmployeeDTO employee) {
        if (employee == null || employee.getBirthday() == null) {
            log.warn("Cannot generate password: employee or birth date is null");
            return null;
        }

        try {
            LocalDate birthDate = employee.getBirthday();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            String dateStr = birthDate.format(formatter);
            return key + dateStr;
        } catch (Exception e) {
            log.error("Error generating default password", e);
            return null;
        }
    }

    /**
     * Generates a default password with a standard key "MedDist"
     *
     * @param employee The employee whose birth date will be used
     * @return The generated default password or null if employee or birth date is null
     */
    public static String generateDefaultPassword(EmployeeDTO employee) {
        return generateDefaultPassword("MedDist@", employee);
    }
}
