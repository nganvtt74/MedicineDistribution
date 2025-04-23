package com.example.medicinedistribution.DAO.Interface;

import com.example.medicinedistribution.DTO.AttendanceDTO;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceDAO {
    boolean insertAttendance(AttendanceDTO attendance, Connection conn);
    boolean updateAttendance(AttendanceDTO attendance, Connection conn);
    boolean deleteAttendance(int employeeId, String date, Connection conn);
    AttendanceDTO getAttendance(int employeeId, String date, Connection conn);
    List<AttendanceDTO> getAttendanceByEmployeeId(int employeeId, Connection conn);
    List<AttendanceDTO> getAttendanceByDate(String date, Connection conn);
    boolean updateListAttendance(List<AttendanceDTO> attendanceList, int status, Connection conn);
    boolean updateAttendanceStatus(int employeeId, String date, int status, Connection conn);
    boolean updateAllAttendanceStatusInDate(String date, int status, Connection conn);
    boolean checkInAttendance(int employeeId, String date, Connection conn);
    boolean checkOutAttendance(int employeeId, String date, Connection conn);
    AttendanceDTO getAttendanceByEmployeeIdAndDate(int employeeId, String date, Connection conn);
    List<AttendanceDTO> getAllAttendanceInMonth(int month, int year, Connection conn);

    List<AttendanceDTO> getAllAttendanceBetweenDates(LocalDate globalStartDate, LocalDate globalEndDate, Connection conn);
}
