package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.PayrollBUS;
import com.example.medicinedistribution.DAO.Interface.PayrollDAO;
import com.example.medicinedistribution.DAO.Interface.Payroll_AllowanceDAO;
import com.example.medicinedistribution.DTO.PayrollDTO;
import com.example.medicinedistribution.DTO.Payroll_AllowanceDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

@Slf4j
public class PayrollBUSImpl implements PayrollBUS {

    private final PayrollDAO payrollDAO;
    private final UserSession userSession;
    private final DataSource dataSource;
    private final Payroll_AllowanceDAO payroll_AllowanceDAO;

    public PayrollBUSImpl(PayrollDAO payrollDAO, UserSession userSession, DataSource dataSource
    , Payroll_AllowanceDAO payroll_AllowanceDAO) {
        this.payrollDAO = payrollDAO;
        this.userSession = userSession;
        this.dataSource = dataSource;
        this.payroll_AllowanceDAO = payroll_AllowanceDAO;
    }

    @Override
    public boolean insertPayroll(List<PayrollDTO> payrollDTOList) {
        if(userSession.hasPermission("MANAGE_PAYROLL")){
            try(Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);
                payrollDAO.insertPayroll(payrollDTOList,connection);
                for (PayrollDTO payrollDTO : payrollDTOList) {
                    if (!payroll_AllowanceDAO.insertPayroll_Allowance(payrollDTO.getPayrollId(),
                            payrollDTO.getMeal_allowance(), payrollDTO.getGas_allowance(),
                            payrollDTO.getPhone_allowance(),payrollDTO.getResponsibility_allowance(), connection)){
                        log.error("Error inserting payroll allowance for payroll ID: {}", payrollDTO.getPayrollId());
                        connection.rollback();
                        throw new InsertFailedException("Failed to insert payroll allowance");
                    }
                }
                connection.commit();
                return true;
            } catch (Exception e) {
                log.error("Error inserting payroll: {}", e.getMessage());
                return false;
            }
        }else {
            log.error("User does not have permission to manage payroll");
            throw new PermissionDeniedException("User does not have permission to manage payroll");
        }
    }

    @Override
    public PayrollDTO getPayrollByEmployeeId(int employeeId) {
            try(Connection connection = dataSource.getConnection()) {
                return payrollDAO.getPayrollByEmployeeId(employeeId, connection);
            } catch (Exception e) {
                log.error("Error fetching payroll by employee ID: {}", e.getMessage());
                return null;
            }
        }

    @Override
    public boolean getPayrollByMonthAndYear(int month, int year) {
        try(Connection connection = dataSource.getConnection()) {
            return payrollDAO.getPayrollByMonthAndYear(month, year, connection);
        } catch (Exception e) {
            log.error("Error fetching payroll by month and year: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public BigDecimal getAvgNetIncome6Months(int employeeId, int month, int year) {
        try(Connection connection = dataSource.getConnection()) {
            return payrollDAO.getAvgNetIncome6Months(employeeId, month, year, connection);
        } catch (Exception e) {
            log.error("Error fetching average net income for last 6 months: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public PayrollDTO getPayrollByEmployeeId(int employeeId, int month, int year) {
        try(Connection connection = dataSource.getConnection()) {
            PayrollDTO payrollDTO = payrollDAO.getPayrollByEmployeeId(employeeId, month, year, connection);
            if (payrollDTO != null) {
                List<Payroll_AllowanceDTO> payrollAllowanceDTOList = payroll_AllowanceDAO.getByPayrollId(payrollDTO.getPayrollId(), connection);
                if (payrollAllowanceDTOList != null && !payrollAllowanceDTOList.isEmpty()) {
                    payrollDTO.setMeal_allowance(payrollAllowanceDTOList.stream()
                            .filter(payrollAllowanceDTO -> payrollAllowanceDTO.getAllowance_id() == 1)
                            .findFirst()
                            .map(Payroll_AllowanceDTO::getAmount)
                            .orElse(BigDecimal.ZERO));
                    payrollDTO.setGas_allowance(payrollAllowanceDTOList.stream()
                            .filter(payrollAllowanceDTO -> payrollAllowanceDTO.getAllowance_id() == 2)
                            .findFirst()
                            .map(Payroll_AllowanceDTO::getAmount)
                            .orElse(BigDecimal.ZERO));
                    payrollDTO.setPhone_allowance(payrollAllowanceDTOList.stream()
                            .filter(payrollAllowanceDTO -> payrollAllowanceDTO.getAllowance_id() == 3)
                            .findFirst()
                            .map(Payroll_AllowanceDTO::getAmount)
                            .orElse(BigDecimal.ZERO));
                    payrollDTO.setResponsibility_allowance(payrollAllowanceDTOList.stream()
                            .filter(payrollAllowanceDTO -> payrollAllowanceDTO.getAllowance_id() == 4)
                            .findFirst()
                            .map(Payroll_AllowanceDTO::getAmount)
                            .orElse(BigDecimal.ZERO));
                    return payrollDTO;
                }
            }
        } catch (Exception e) {
            log.error("Error fetching payroll by employee ID: {}", e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public List<PayrollDTO> findByPeriod(Integer selectedMonth, Integer selectedYear) {
        try (Connection connection = dataSource.getConnection()) {
            return payrollDAO.findByPeriod(selectedMonth, selectedYear, connection);
        } catch (Exception e) {
            log.error("Error fetching payroll by period: {}", e.getMessage());
            return null;
        }
    }

}
