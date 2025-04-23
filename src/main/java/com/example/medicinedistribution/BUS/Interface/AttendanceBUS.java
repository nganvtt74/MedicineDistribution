package com.example.medicinedistribution.BUS.Interface;

import com.example.medicinedistribution.DTO.AttendanceDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public interface AttendanceBUS {
    boolean insertAttendance(AttendanceDTO attendance);
    boolean updateAttendance(AttendanceDTO attendance);
    boolean deleteAttendance(int employeeId, String date);
    AttendanceDTO getAttendance(int employeeId, String date);
    List<AttendanceDTO> getAttendanceByEmployeeId(int employeeId);
    List<AttendanceDTO> getAttendanceByDate(String date);
    boolean updateListAttendance(List<AttendanceDTO> attendanceList, int status);
    boolean updateAttendanceStatus(int employeeId, String date, int status);
    boolean updateAllAttendanceStatusInDate(String date, int status);
    boolean checkInAttendance(int employeeId, String date);
    boolean checkOutAttendance(int employeeId, String date);

    List<AttendanceDTO> getAllAttendanceInMonth(int i, int i1);

    List<AttendanceDTO> getAllAttendanceBetweenDates(LocalDate globalStartDate, LocalDate globalEndDate);
}
