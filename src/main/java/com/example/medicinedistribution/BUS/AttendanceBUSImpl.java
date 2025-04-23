package com.example.medicinedistribution.BUS;

    import com.example.medicinedistribution.BUS.Interface.AttendanceBUS;
    import com.example.medicinedistribution.DAO.Interface.AttendanceDAO;
    import com.example.medicinedistribution.DAO.Interface.LeaveYearsDAO;
    import com.example.medicinedistribution.DTO.AttendanceDTO;
    import com.example.medicinedistribution.DTO.LeaveYears;
    import com.example.medicinedistribution.DTO.UserSession;
    import com.example.medicinedistribution.Exception.DeleteFailedException;
    import com.example.medicinedistribution.Exception.InsertFailedException;
    import com.example.medicinedistribution.Exception.PermissionDeniedException;
    import com.example.medicinedistribution.Exception.UpdateFailedException;
    import com.example.medicinedistribution.Util.JsonUtil;
    import com.fasterxml.jackson.databind.JsonNode;
    import jakarta.validation.ConstraintViolation;
    import jakarta.validation.Validator;
    import lombok.extern.slf4j.Slf4j;
    import org.checkerframework.common.reflection.qual.GetClass;

    import javax.sql.DataSource;
    import java.sql.Connection;
    import java.sql.SQLException;
    import java.time.LocalDate;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Objects;
    import java.util.Set;

    @Slf4j
    public class AttendanceBUSImpl implements AttendanceBUS {
        private final AttendanceDAO attendanceDAO;
        private final DataSource dataSource;
        private final TransactionManager transactionManager;
        private final UserSession userSession;
        private final Validator validator;
        private final LeaveYearsDAO leaveYearsDAO;

        public AttendanceBUSImpl(AttendanceDAO attendanceDAO, DataSource dataSource, UserSession userSession,
                                 TransactionManager transactionManager, Validator validator, LeaveYearsDAO leaveYearsDAO) {
            this.attendanceDAO = attendanceDAO;
            this.dataSource = dataSource;
            this.userSession = userSession;
            this.transactionManager = transactionManager;
            this.validator = validator;
            this.leaveYearsDAO = leaveYearsDAO;
        }

        @Override
        public boolean insertAttendance(AttendanceDTO attendance) {
            if (!userSession.hasPermission("INSERT_ATTENDANCE")) {
                log.error("User does not have permission to insert attendance");
                throw new PermissionDeniedException("Bạn không có quyền thêm dữ liệu chấm công");
            }

            validateAttendance(attendance);

            try(Connection conn = dataSource.getConnection()) {
                boolean result = attendanceDAO.insertAttendance(attendance, conn);
                if (!result) {
                    log.error("Failed to insert attendance: {}", attendance);
                    throw new InsertFailedException("Thêm dữ liệu chấm công thất bại");
                }
                log.info("Inserted attendance: {}", attendance);
                return true;
            }catch (SQLException e) {
                log.error(e.getMessage());
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }

        }

        @Override
        public boolean updateAttendance(AttendanceDTO attendance) {
            if (!userSession.hasPermission("UPDATE_ATTENDANCE")) {
                log.error("User does not have permission to update attendance");
                throw new PermissionDeniedException("Bạn không có quyền cập nhật dữ liệu chấm công");
            }

            validateAttendance(attendance);

            try(Connection conn = dataSource.getConnection()) {
                boolean result = attendanceDAO.updateAttendance(attendance, conn);
                if (!result) {
                    log.error("Failed to update attendance: {}", attendance);
                    throw new UpdateFailedException("Cập nhật dữ liệu chấm công thất bại");
                }
                log.info("Updated attendance: {}", attendance);
                return true;
            }catch (SQLException e) {
                log.error(e.getMessage());
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }

        @Override
        public boolean deleteAttendance(int employeeId, String date) {
            if (!userSession.hasPermission("DELETE_ATTENDANCE")) {
                log.error("User does not have permission to delete attendance");
                throw new PermissionDeniedException("Bạn không có quyền xóa dữ liệu chấm công");
            }


            try(Connection conn = dataSource.getConnection()) {

                boolean result = attendanceDAO.deleteAttendance(employeeId, date, conn);
                if (!result) {
                    log.error("Failed to delete attendance for employee ID: {} on date: {}", employeeId, date);
                    throw new DeleteFailedException("Xóa dữ liệu chấm công thất bại");
                }
                log.info("Deleted attendance for employee ID: {} on date: {}", employeeId, date);
                return true;
            }catch (SQLException e) {
                log.error(e.getMessage());
                throw new RuntimeException("Lỗi khi lấy kết nối", e);
            }
        }

        @Override
        public AttendanceDTO getAttendance(int employeeId, String date) {
            if (!userSession.hasPermission("VIEW_ATTENDANCE")) {
                log.error("User does not have permission to view attendance");
                throw new PermissionDeniedException("Bạn không có quyền xem dữ liệu chấm công");
            }

            try (Connection conn = dataSource.getConnection()) {
                AttendanceDTO attendance = attendanceDAO.getAttendance(employeeId, date, conn);
                if (attendance == null) {
                    log.info("No attendance found for employee ID: {} on date: {}", employeeId, date);
                }
                return attendance;
            } catch (SQLException e) {
                log.error("Error while getting attendance: ", e);
                throw new RuntimeException("Lỗi khi lấy dữ liệu chấm công", e);
            }
        }

        @Override
        public List<AttendanceDTO> getAttendanceByEmployeeId(int employeeId) {
            if (!userSession.hasPermission("VIEW_ATTENDANCE")) {
                log.error("User does not have permission to view attendance");
                throw new PermissionDeniedException("Bạn không có quyền xem dữ liệu chấm công");
            }

            try (Connection conn = dataSource.getConnection()) {
                List<AttendanceDTO> attendances = attendanceDAO.getAttendanceByEmployeeId(employeeId, conn);
                log.info("Found {} attendance records for employee ID: {}", attendances.size(), employeeId);
                return attendances;
            } catch (SQLException e) {
                log.error("Error while getting attendance by employee ID: ", e);
                throw new RuntimeException("Lỗi khi lấy dữ liệu chấm công theo nhân viên", e);
            }
        }

        @Override
        public List<AttendanceDTO> getAttendanceByDate(String date) {
            if (!userSession.hasPermission("VIEW_ATTENDANCE")) {
                log.error("User does not have permission to view attendance");
                throw new PermissionDeniedException("Bạn không có quyền xem dữ liệu chấm công");
            }

            try (Connection conn = dataSource.getConnection()) {
                List<AttendanceDTO> attendances = attendanceDAO.getAttendanceByDate(date, conn);
                log.info("Found {} attendance records for date: {}", attendances.size(), date);
                return attendances;
            } catch (SQLException e) {
                log.error("Error while getting attendance by date: ", e);
                throw new RuntimeException("Lỗi khi lấy dữ liệu chấm công theo ngày", e);
            }
        }

        @Override
        public boolean updateListAttendance(List<AttendanceDTO> attendanceList, int status) {
            if (!userSession.hasPermission("UPDATE_ATTENDANCE")) {
                log.error("User does not have permission to update attendance");
                throw new PermissionDeniedException("Bạn không có quyền cập nhật dữ liệu chấm công");
            }

            for (AttendanceDTO attendance : attendanceList) {
                validateAttendance(attendance);
            }

            try(Connection conn = transactionManager.beginTransaction()) {
                boolean result = attendanceDAO.updateListAttendance(attendanceList, status, conn);
                if (!result) {
                    log.error("Failed to update list of attendances with status: {}", status);
                    transactionManager.rollbackTransaction(conn);
                    throw new UpdateFailedException("Cập nhật danh sách chấm công thất bại");
                }
                log.info("Updated {} attendance records with status: {}", attendanceList.size(), status);
                transactionManager.commitTransaction(conn);
                return true;
            }catch (SQLException e) {
                log.error("Error while updating list of attendances: ", e);
                throw new RuntimeException("Lỗi khi cập nhật danh sách chấm công", e);
            }
        }

        @Override
        public boolean updateAttendanceStatus(int employeeId, String date, int status) {
            if (!userSession.hasPermission("UPDATE_ATTENDANCE")) {
                log.error("User does not have permission to update attendance status");
                throw new PermissionDeniedException("Bạn không có quyền cập nhật trạng thái chấm công");
            }

            try(Connection conn = transactionManager.beginTransaction()) {
                // If status is 4 (leave/day off), check and update leave days
                if (status == 4) {

                    AttendanceDTO attendanceDTO = attendanceDAO.getAttendanceByEmployeeIdAndDate(employeeId, date, conn);
                    // Check if attendance record exists
                    if (attendanceDTO == null) {
                        log.error("Attendance record not found for employee ID: {} on date: {}", employeeId, date);
                        transactionManager.rollbackTransaction(conn);
                        throw new UpdateFailedException("Không tìm thấy bản ghi chấm công");
                    }
                    // Check if attendance status is already 4
                    if (attendanceDTO.getStatus() == 4) {
                        log.error("Attendance status is already 4 for employee ID: {} on date: {}", employeeId, date);
                        transactionManager.rollbackTransaction(conn);
                        throw new UpdateFailedException("Trạng thái chấm công đã là nghỉ phép");
                    }
                    // Parse date string to LocalDate
                    LocalDate attendanceDate = LocalDate.parse(date);
                    LocalDate leaveYearDate = LocalDate.of(attendanceDate.getYear(), 1, 1);

                    // Try to find existing leave year record
                    LeaveYears leaveYear = leaveYearsDAO.findByEmployeeIdAndYear(employeeId, leaveYearDate, conn);
                    log.info("Leave year record for employee ID: {} for year: {}: {}", employeeId, leaveYearDate.getYear(), leaveYear);
                    if (leaveYear == null) {
                        // Create new leave year record with default value from system config
                        leaveYear = new LeaveYears();
                        leaveYear.setEmployeeId(employeeId);
                        leaveYear.setLeaveYear(leaveYearDate);

                        // Get default leave days from system config
                        int defaultLeaveDays = getDefaultLeaveDays();

                        // Set and subtract one day
                        int remainingLeaveDays = defaultLeaveDays - 1;
                        if (remainingLeaveDays < 0) {
                            log.error("Employee ID: {} has no remaining leave days for year: {}", employeeId, leaveYearDate.getYear());
                            transactionManager.rollbackTransaction(conn);
                            throw new UpdateFailedException("Nhân viên không còn ngày nghỉ phép cho năm " + leaveYearDate.getYear());
                        }

                        leaveYear.setValidLeaveDays(remainingLeaveDays);
                        Integer insertResult = leaveYearsDAO.insert(leaveYear, conn);

                        if (insertResult == null) {
                            log.error("Failed to create leave year record for employee ID: {} for year: {}",
                                    employeeId, leaveYearDate.getYear());
                            transactionManager.rollbackTransaction(conn);
                            throw new UpdateFailedException("Không thể tạo bản ghi nghỉ phép cho năm " + leaveYearDate.getYear());
                        }
                    } else {
                        // Update existing leave year record
                        int remainingLeaveDays = leaveYear.getValidLeaveDays() - 1;

                        if (remainingLeaveDays < 0) {
                            log.error("Employee ID: {} has no remaining leave days for year: {}",
                                    employeeId, leaveYearDate.getYear());
                            transactionManager.rollbackTransaction(conn);
                            throw new UpdateFailedException("Nhân viên không còn ngày nghỉ phép cho năm " + leaveYearDate.getYear());
                        }

                        boolean updateResult = leaveYearsDAO.updateValidLeaveDays(
                                leaveYear.getLeaveYearsId(), remainingLeaveDays, conn);

                        if (!updateResult) {
                            log.error("Failed to update leave days for employee ID: {} for year: {}",
                                    employeeId, leaveYearDate.getYear());
                            transactionManager.rollbackTransaction(conn);
                            throw new UpdateFailedException("Không thể cập nhật số ngày nghỉ phép");
                        }
                    }
                }
                // Check if attendance record exists
                AttendanceDTO attendanceDTO = attendanceDAO.getAttendanceByEmployeeIdAndDate(employeeId, date, conn);
                if (attendanceDTO == null) {
                    log.error("Attendance record not found for employee ID: {} on date: {}", employeeId, date);
                    transactionManager.rollbackTransaction(conn);
                    throw new UpdateFailedException("Không tìm thấy bản ghi chấm công");
                }
                // Check if attendance status is already the same
                if (!(attendanceDTO.getStatus() == status)) {
                    if (attendanceDTO.getStatus() == 4) {
                        // Previous status was leave/day off, need to add back a leave day
                        try {
                            // Get the leave year record for the current year
                            LocalDate attendanceDate = LocalDate.parse(date);
                            LocalDate leaveYearDate = LocalDate.of(attendanceDate.getYear(), 1, 1);

                            LeaveYears leaveYear = leaveYearsDAO.findByEmployeeIdAndYear(employeeId, leaveYearDate, conn);

                            if (leaveYear != null) {
                                // Get the maximum allowed days from config
                                int maxLeaveDays = getDefaultLeaveDays();

                                // Only add a day back if the current count is less than the maximum
                                if (leaveYear.getValidLeaveDays() < maxLeaveDays) {
                                    int updatedLeaveDays = leaveYear.getValidLeaveDays() + 1;

                                    boolean updateResult = leaveYearsDAO.updateValidLeaveDays(
                                            leaveYear.getLeaveYearsId(), updatedLeaveDays, conn);

                                    if (!updateResult) {
                                        log.error("Failed to update leave days for employee ID: {} for year: {}",
                                                employeeId, leaveYearDate.getYear());
                                        transactionManager.rollbackTransaction(conn);
                                        throw new UpdateFailedException("Không thể cập nhật số ngày nghỉ phép");
                                    }

                                    log.info("Added back one leave day for employee ID: {} for year: {}. New balance: {}",
                                            employeeId, leaveYearDate.getYear(), updatedLeaveDays);
                                } else {
                                    log.info("Leave days already at maximum ({}) for employee ID: {} for year: {}",
                                            maxLeaveDays, employeeId, leaveYearDate.getYear());
                                }
                            } else {
                                log.warn("No leave year record found for employee ID: {} for year: {} when trying to add back leave day",
                                        employeeId, leaveYearDate.getYear());
                            }
                        } catch (Exception e) {
                            log.error("Error while adding back leave day: ", e);
                            transactionManager.rollbackTransaction(conn);
                            throw new UpdateFailedException("Lỗi khi cập nhật ngày nghỉ phép: " + e.getMessage());
                        }
                    }

                    // Update attendance status
                    boolean result = attendanceDAO.updateAttendanceStatus(employeeId, date, status, conn);
                    if (!result) {
                        log.error("Failed to update attendance status for employee ID: {} on date: {} with status: {}",
                                employeeId, date, status);
                        transactionManager.rollbackTransaction(conn);
                        throw new UpdateFailedException("Cập nhật trạng thái chấm công thất bại");
                    }

                    log.info("Updated attendance status for employee ID: {} on date: {} with status: {}",
                            employeeId, date, status);
                }





                transactionManager.commitTransaction(conn);
                return true;

            } catch (SQLException e) {
                log.error("Error while updating attendance status: ", e);
                throw new RuntimeException("Lỗi khi cập nhật trạng thái chấm công", e);
            }
        }

        @Override
        public boolean updateAllAttendanceStatusInDate(String date, int status) {
            if (!userSession.hasPermission("UPDATE_ATTENDANCE")) {
                log.error("User does not have permission to update all attendance status in date");
                throw new PermissionDeniedException("Bạn không có quyền cập nhật trạng thái chấm công");
            }

            try(Connection conn = transactionManager.beginTransaction()) {
                boolean result = attendanceDAO.updateAllAttendanceStatusInDate(date, status, conn);
                if (!result) {
                    log.error("Failed to update all attendance status for date: {} with status: {}", date, status);
                    transactionManager.rollbackTransaction(conn);
                    throw new UpdateFailedException("Cập nhật trạng thái chấm công thất bại");
                }
                log.info("Updated all attendance status for date: {} with status: {}", date, status);
                transactionManager.commitTransaction(conn);
                return true;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        private void validateAttendance(AttendanceDTO attendance) {
            Set<ConstraintViolation<AttendanceDTO>> violations = validator.validate(attendance);
            if (!violations.isEmpty()) {
                for (ConstraintViolation<AttendanceDTO> violation : violations) {
                    log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                    throw new IllegalArgumentException(violation.getMessage());
                }
            }
        }
        @Override
        public boolean checkInAttendance(int employeeId, String date) {
            try(Connection conn = dataSource.getConnection()) {
                boolean result = attendanceDAO.checkInAttendance(employeeId, date, conn);
                if (!result) {
                    log.error("Failed to check in attendance for employee ID: {} on date: {}", employeeId, date);
                    throw new UpdateFailedException("Điểm danh thất bại");
                }
                return true;
            } catch (SQLException e) {
                log.error("Error while checking in attendance: ", e);
                throw new RuntimeException("Lỗi khi điểm danh", e);
            }
        }
        @Override
        public boolean checkOutAttendance(int employeeId, String date) {
            try(Connection conn = dataSource.getConnection()) {
                boolean result = attendanceDAO.checkOutAttendance(employeeId, date, conn);
                if (!result) {
                    log.error("Failed to check out attendance for employee ID: {} on date: {}", employeeId, date);
                    throw new UpdateFailedException("Điểm danh thất bại");
                }
                log.info("Checked out attendance for employee ID: {} on date: {}", employeeId, date);
                return true;
            } catch (SQLException e) {
                log.error("Error while checking out attendance: ", e);
                throw new RuntimeException("Lỗi khi điểm danh", e);
            }
        }

        @Override
        public List<AttendanceDTO> getAllAttendanceInMonth(int i, int i1) {
            if (!userSession.hasPermission("VIEW_ATTENDANCE")) {
                log.error("User does not have permission to view all attendance in month");
                throw new PermissionDeniedException("Bạn không có quyền xem dữ liệu chấm công");
            }

            try (Connection conn = dataSource.getConnection()) {
                List<AttendanceDTO> attendances = attendanceDAO.getAllAttendanceInMonth(i, i1, conn);
                log.info("Found {} attendance records for month: {} year: {}", attendances.size(), i, i1);
                return attendances;
            } catch (SQLException e) {
                log.error("Error while getting all attendance in month: ", e);
                throw new RuntimeException("Lỗi khi lấy dữ liệu chấm công theo tháng", e);
            }
        }

        @Override
        public List<AttendanceDTO> getAllAttendanceBetweenDates(LocalDate globalStartDate, LocalDate globalEndDate) {
            if (!userSession.hasPermission("VIEW_ATTENDANCE")) {
                log.error("User does not have permission to view all attendance between dates");
                throw new PermissionDeniedException("Bạn không có quyền xem dữ liệu chấm công");
            }

            try (Connection conn = dataSource.getConnection()) {
                List<AttendanceDTO> attendances = attendanceDAO.getAllAttendanceBetweenDates(globalStartDate, globalEndDate, conn);
                log.info("Found {} attendance records between dates: {} and {}", attendances.size(), globalStartDate, globalEndDate);
                return attendances;
            } catch (SQLException e) {
                log.error("Error while getting all attendance between dates: ", e);
                throw new RuntimeException("Lỗi khi lấy dữ liệu chấm công theo khoảng thời gian", e);
            }
        }

        private int getDefaultLeaveDays() {
            try {
                // Read from system_config.json

                JsonNode configNode = JsonUtil.readJsonFromResource("/config/system_config.json");
                return configNode.get("validLeaveDays").asInt();
            } catch (Exception e) {
                log.error("Error reading system config", e);
                // Default to 15 days if config read fails
                return 15;
            }
        }
    }