package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.InvoiceBUS;
import com.example.medicinedistribution.DAO.Interface.InvoiceDAO;
import com.example.medicinedistribution.DAO.Interface.InvoiceDetailDAO;
import com.example.medicinedistribution.DAO.Interface.ProductDAO;
import com.example.medicinedistribution.DTO.*;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class InvoiceBUSImpl implements InvoiceBUS {

    private final InvoiceDAO invoiceDAO;
    private final InvoiceDetailDAO invoiceDetailDAO;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final TransactionManager transactionManager;
    private final Validator validator;
    private final ProductDAO productDAO;

    public InvoiceBUSImpl(InvoiceDAO invoiceDAO, InvoiceDetailDAO invoiceDetailDAO,
                          DataSource dataSource, UserSession userSession, TransactionManager transactionManager,
                          Validator validator, ProductDAO productDAO) {
        this.invoiceDAO = invoiceDAO;
        this.invoiceDetailDAO = invoiceDetailDAO;
        this.dataSource = dataSource;
        this.userSession = userSession;
        this.transactionManager = transactionManager;
        this.validator = validator;
        this.productDAO = productDAO;
    }

    private void valid(InvoiceDTO invoiceDTO) {
        Set<ConstraintViolation<InvoiceDTO>> violations = validator.validate(invoiceDTO);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<InvoiceDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }

    private void valid(List<InvoiceDetailDTO> details) {
        for (InvoiceDetailDTO detail : details) {
            Set<ConstraintViolation<InvoiceDetailDTO>> violations = validator.validate(detail);
            if (!violations.isEmpty()) {
                for (ConstraintViolation<InvoiceDetailDTO> violation : violations) {
                    log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                    throw new IllegalArgumentException(violation.getMessage());
                }
            }
        }
    }

    @Override
    public boolean insert(InvoiceDTO invoiceDTO) {
        if(!userSession.hasPermission("INSERT_INVOICE")){
            log.error("User does not have permission to insert invoice");
            throw new PermissionDeniedException("Bạn không có quyền thêm hóa đơn");
        }
        valid(invoiceDTO);
        valid(invoiceDTO.getDetails());

        try(Connection conn = transactionManager.beginTransaction()){
            Integer result = invoiceDAO.insert(invoiceDTO, conn);
            if (result > 0) {
                for (var detail : invoiceDTO.getDetails()) {
                    detail.setInvoiceId(result);
                    if (invoiceDetailDAO.insert(detail, conn) < 0) {
                        transactionManager.rollbackTransaction(conn);
                        log.error("Insert InvoiceDetail failed");
                        throw new InsertFailedException("Thêm chi tiết hóa đơn thất bại");
                    }
                    if (!productDAO.updateQuantity(detail.getProductId(), detail.getQuantity(), conn)) {
                        transactionManager.rollbackTransaction(conn);
                        log.error("Update Product failed");
                        throw new InsertFailedException("Cập nhật sản phẩm thất bại");
                    }
                }

                transactionManager.commitTransaction(conn);
                log.info("Thêm hóa đơn thành công");
                return true;
            } else {
                transactionManager.rollbackTransaction(conn);
                log.error("Insert Invoice failed");
                throw new InsertFailedException("Thêm hóa đơn thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean update(InvoiceDTO invoiceDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean delete(Integer integer) {

        if(!userSession.hasPermission("DELETE_INVOICE")){
            log.error("User does not have permission to delete invoice");
            throw new PermissionDeniedException("Bạn không có quyền xóa hóa đơn");
        }
        try(Connection conn = dataSource.getConnection()){
            if(invoiceDetailDAO.delete(integer, conn)){
                if(!invoiceDAO.delete(integer, conn)){
                    log.error("Delete Invoice failed");
                    throw new DeleteFailedException("Xóa hóa đơn thất bại");
                } else {
                    log.info("Xóa hóa đơn thành công");
                    return true;
                }
            }else {
                log.error("Delete InvoiceDetail failed");
                throw new DeleteFailedException("Xóa chi tiết hóa đơn thất bại");
            }

        }catch (SQLException e){
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public InvoiceDTO findById(Integer integer) {
        try (Connection connection = dataSource.getConnection()) {
            InvoiceDTO invoiceDTO = invoiceDAO.findById(integer, connection);
            if (invoiceDTO != null) {
                List<InvoiceDetailDTO> details = invoiceDetailDAO.findByInvoiceId(integer, connection);
                invoiceDTO.setDetails(details);
            }
            return invoiceDTO;
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public List<InvoiceDTO> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            List<InvoiceDTO> invoiceDTOList = invoiceDAO.findAll(connection);
            for (InvoiceDTO invoiceDTO : invoiceDTOList) {
                List<InvoiceDetailDTO> details = invoiceDetailDAO.findByInvoiceId(invoiceDTO.getInvoiceId(), connection);
                invoiceDTO.setDetails(details);
            }
            return invoiceDTOList;
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }
    @Override
    public List<StatisticDTO> getRevenueSummary(LocalDate fromDate, LocalDate toDate, String viewType) {
        // Group by time period based on viewType (day, week, month, quarter, year)
        String groupBy = switch (viewType) {
            case "Ngày" -> "DATE(date)";
            case "Tuần" -> "CONCAT(YEAR(date), '-W', WEEK(date))"; // Format: "2024-W45"            case "Tháng" -> "DATE_FORMAT(date, '%Y-%m')";
            case "Quý" -> "CONCAT(YEAR(date), '-Q', QUARTER(date))";
            case "Năm" -> "YEAR(date)";
            default -> "DATE_FORMAT(date, '%Y-%m')"; // default to month
        };
        try(Connection connection = dataSource.getConnection()) {
            return invoiceDAO.getRevenueStatistics(fromDate, toDate, groupBy, viewType, connection);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }

    }

    @Override
    public Map<String, BigDecimal> getRevenueByCategorySummary(LocalDate fromDate, LocalDate toDate) {
        try(Connection connection = dataSource.getConnection()) {
            return invoiceDAO.getRevenueByCategoryStatistics(fromDate, toDate, connection);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public BigDecimal getDailySales(LocalDate now) {
        try(Connection connection = dataSource.getConnection()) {
            return invoiceDAO.getDailySales(now, connection);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public List<ProductStatisticDTO> getProductSalesSummary(LocalDate fromDate, LocalDate toDate, String viewType) {
        // Group by time period based on viewType (day, week, month, quarter, year)
        String groupBy = switch (viewType) {
            case "Ngày" -> "DATE(date)";
            case "Tuần" -> "CONCAT(YEAR(date), '-W', WEEK(date))"; // Format: "2024-W45"
            case "Tháng" -> "DATE_FORMAT(date, '%Y-%m')";
            case "Quý" -> "CONCAT(YEAR(date), '-Q', QUARTER(date))";
            case "Năm" -> "YEAR(date)";
            default -> "DATE_FORMAT(date, '%Y-%m')"; // default to month
        };
        try(Connection connection = dataSource.getConnection()) {
            return invoiceDAO.getProductSalesStatistics(fromDate, toDate, groupBy, viewType, connection);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public Map<String, Integer> getProductSalesByCategorySummary(LocalDate fromDate, LocalDate toDate) {
        try(Connection connection = dataSource.getConnection()) {
            return invoiceDAO.getProductSalesByCategoryStatistics(fromDate, toDate, connection);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public Integer getNextInvoiceId() {
        try (Connection connection = dataSource.getConnection()) {
            return invoiceDAO.getNextInvoiceId(connection);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }
}
