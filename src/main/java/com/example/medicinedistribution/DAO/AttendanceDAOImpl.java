package com.example.medicinedistribution.DAO;

    import com.example.medicinedistribution.DAO.Interface.AttendanceDAO;
    import com.example.medicinedistribution.DTO.AttendanceDTO;
    import com.example.medicinedistribution.Util.JsonUtil;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import lombok.extern.slf4j.Slf4j;

    import java.io.IOException;
    import java.io.InputStream;
    import java.sql.*;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
    import java.time.LocalTime;
    import java.time.format.DateTimeFormatter;
    import java.util.ArrayList;
    import java.util.List;

    @Slf4j
    public class AttendanceDAOImpl implements AttendanceDAO {

        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public boolean insertAttendance(AttendanceDTO attendance, Connection conn) {
            String sql = "INSERT INTO Attendance (employee_id, date, check_in, check_out, status) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, attendance.getEmployee_id());
                stmt.setDate(2, java.sql.Date.valueOf(attendance.getDate()));
                stmt.setTimestamp(3, attendance.getCheck_in() != null ? Timestamp.valueOf(attendance.getCheck_in()) : null);
                stmt.setTimestamp(4, attendance.getCheck_out() != null ? Timestamp.valueOf(attendance.getCheck_out()) : null);
                stmt.setInt(5, attendance.getStatus());

                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                log.error("Error inserting attendance: ", e);
                return false;
            }
        }

        @Override
        public boolean updateAttendance(AttendanceDTO attendance, Connection conn) {
            String sql = "UPDATE Attendance SET check_in = ?, check_out = ?, status = ? WHERE employee_id = ? AND date = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setTimestamp(1, attendance.getCheck_in() != null ? Timestamp.valueOf(attendance.getCheck_in()) : null);
                stmt.setTimestamp(2, attendance.getCheck_out() != null ? Timestamp.valueOf(attendance.getCheck_out()) : null);
                stmt.setInt(3, attendance.getStatus());
                stmt.setInt(4, attendance.getEmployee_id());
                stmt.setDate(5, java.sql.Date.valueOf(attendance.getDate()));

                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                log.error("Error updating attendance: ", e);
                return false;
            }
        }

        @Override
        public boolean deleteAttendance(int employeeId, String date, Connection conn) {
            String sql = "DELETE FROM Attendance WHERE employee_id = ? AND date = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, employeeId);
                stmt.setDate(2, java.sql.Date.valueOf(LocalDate.parse(date, DATE_FORMATTER)));

                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                log.error("Error deleting attendance: ", e);
                return false;
            }
        }

        @Override
        public AttendanceDTO getAttendance(int employeeId, String date, Connection conn) {
            String sql = "SELECT * FROM Attendance WHERE employee_id = ? AND date = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, employeeId);
                stmt.setDate(2, java.sql.Date.valueOf(LocalDate.parse(date, DATE_FORMATTER)));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAttendanceDTO(rs);
                    }
                }
            } catch (SQLException e) {
                log.error("Error fetching attendance: ", e);
            }

            return null;
        }

        @Override
        public List<AttendanceDTO> getAttendanceByEmployeeId(int employeeId, Connection conn) {
            List<AttendanceDTO> attendanceList = new ArrayList<>();
            String sql = "SELECT * FROM Attendance WHERE employee_id = ? ORDER BY date DESC";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, employeeId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        attendanceList.add(mapResultSetToAttendanceDTO(rs));
                    }
                }
            } catch (SQLException e) {
                log.error("Error fetching attendance by employee ID: ", e);
            }

            return attendanceList;
        }




        @Override
        public List<AttendanceDTO> getAttendanceByDate(String date, Connection conn) {
            List<AttendanceDTO> attendanceList = new ArrayList<>();
            String sql = "SELECT * FROM Attendance WHERE date = ? ORDER BY employee_id";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, java.sql.Date.valueOf(LocalDate.parse(date, DATE_FORMATTER)));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        attendanceList.add(mapResultSetToAttendanceDTO(rs));
                    }
                }
            } catch (SQLException e) {
                log.error("Error fetching attendance by date: ", e);
            }

            return attendanceList;
        }

@Override
public boolean updateListAttendance(List<AttendanceDTO> attendanceList, int status, Connection conn) {
    String sql = "UPDATE Attendance SET status = ? WHERE employee_id = ? AND date = ?";

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        for (AttendanceDTO attendance : attendanceList) {
            stmt.setInt(1, status);
            stmt.setInt(2, attendance.getEmployee_id());
            stmt.setDate(3, java.sql.Date.valueOf(attendance.getDate()));
            stmt.addBatch();
        }

        int[] results = stmt.executeBatch();

        // Check if all updates were successful
        for (int result : results) {
            if (result <= 0) {
                return false;
            }
        }

        return true;
    } catch (SQLException e) {
        log.error("Error updating list of attendance: ", e);
        return false;
    }
}

        @Override
        public boolean updateAttendanceStatus(int employeeId, String date, int status, Connection conn) {
            String sql = "UPDATE Attendance SET status = ? WHERE employee_id = ? AND date = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, status);
                stmt.setInt(2, employeeId);
                stmt.setDate(3, java.sql.Date.valueOf(LocalDate.parse(date, DATE_FORMATTER)));

                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                log.error("Error updating attendance status: ", e);
                return false;
            }
        }

        @Override
        public boolean updateAllAttendanceStatusInDate(String date, int status, Connection conn) {
            String sql = "UPDATE Attendance SET status = ? WHERE date = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, status);
                stmt.setDate(2, java.sql.Date.valueOf(LocalDate.parse(date, DATE_FORMATTER)));

                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                log.error("Error updating all attendance status in date: ", e);
                return false;
            }
        }

@Override
public boolean checkInAttendance(int employeeId, String date, Connection conn) {
    try {
        log.info("Checking in attendance for employee ID: {} on date: {}", employeeId, date);
        // Load system configuration
        JsonNode config = JsonUtil.readJsonFromResource("/config/system_config.json");

        // Parse start time from config
        String startTimeStr = config.get("startTime").asText();
        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime currentTime = LocalTime.now();

        // Check if the attendance record already exists
        AttendanceDTO attendance = getAttendance(employeeId, date, conn);

        // Determine status based on check-in time
        int status;
        if (currentTime.isAfter(startTime)) {
            status = 2; // Late (2)
        } else {
            status = 1; // On time (1)
        }

        if (attendance == null) {
            // Create new attendance record with check-in time
            attendance = AttendanceDTO.builder()
                    .employee_id(employeeId)
                    .date(LocalDate.parse(date, DATE_FORMATTER))
                    .check_in(LocalDateTime.now())
                    .status(status)
                    .build();

            return insertAttendance(attendance, conn);
        } else {
            // Update existing record with check-in time
            attendance.setCheck_in(LocalDateTime.now());
            attendance.setStatus(status);

            return updateAttendance(attendance, conn);
        }
    } catch (IOException e) {
        log.error("Error reading system configuration: ", e);
        return false;
    }
}

@Override
public boolean checkOutAttendance(int employeeId, String date, Connection conn) {
    // Get existing attendance record
    AttendanceDTO attendance = getAttendance(employeeId, date, conn);

    if (attendance == null) {
        log.error("Cannot check out: No check-in record found for employee ID: {} on date: {}", employeeId, date);
        return false;
    }

    // Update with check-out time
    attendance.setCheck_out(LocalDateTime.now());

    return updateAttendance(attendance, conn);
}

        @Override
        public AttendanceDTO getAttendanceByEmployeeIdAndDate(int employeeId, String date, Connection conn) {
            String sql = "SELECT * FROM Attendance WHERE employee_id = ? AND date = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, employeeId);
                stmt.setDate(2, java.sql.Date.valueOf(LocalDate.parse(date, DATE_FORMATTER)));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAttendanceDTO(rs);
                    }
                }
            } catch (SQLException e) {
                log.error("Error fetching attendance by employee ID and date: ", e);
            }
            return null;
        }

        @Override
        public List<AttendanceDTO> getAllAttendanceInMonth(int month, int year, Connection conn) {
            List<AttendanceDTO> attendanceList = new ArrayList<>();
            String sql = "SELECT * FROM Attendance WHERE MONTH(date) = ? AND YEAR(date) = ? ORDER BY employee_id, date";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, month);
                stmt.setInt(2, year);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        attendanceList.add(mapResultSetToAttendanceDTO(rs));
                    }
                }
            } catch (SQLException e) {
                log.error("Error fetching all attendance in month: ", e);
            }

            return attendanceList;
        }

        @Override
        public List<AttendanceDTO> getAllAttendanceBetweenDates(LocalDate globalStartDate, LocalDate globalEndDate, Connection conn) {
            List<AttendanceDTO> attendanceList = new ArrayList<>();
            String sql = "SELECT * FROM Attendance WHERE date BETWEEN ? AND ? ORDER BY employee_id, date";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, java.sql.Date.valueOf(globalStartDate));
                stmt.setDate(2, java.sql.Date.valueOf(globalEndDate));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        attendanceList.add(mapResultSetToAttendanceDTO(rs));
                    }
                }
            } catch (SQLException e) {
                log.error("Error fetching all attendance between dates: ", e);
            }

            return attendanceList;
        }


        private AttendanceDTO mapResultSetToAttendanceDTO(ResultSet rs) throws SQLException {
            return AttendanceDTO.builder()
                    .employee_id(rs.getInt("employee_id"))
                    .date(rs.getDate("date").toLocalDate())
                    .check_in(rs.getTimestamp("check_in") != null ? rs.getTimestamp("check_in").toLocalDateTime() : null)
                    .check_out(rs.getTimestamp("check_out") != null ? rs.getTimestamp("check_out").toLocalDateTime() : null)
                    .status(rs.getInt("status"))
                    .build();
        }

    }