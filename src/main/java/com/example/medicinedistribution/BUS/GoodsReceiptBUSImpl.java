package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.GoodsReceiptBUS;
import com.example.medicinedistribution.DAO.Interface.GoodsReceiptDAO;
import com.example.medicinedistribution.DAO.Interface.GoodsReceiptDetailDAO;
import com.example.medicinedistribution.DAO.Interface.ProductDAO;
import com.example.medicinedistribution.DTO.GoodsReceiptDTO;
import com.example.medicinedistribution.DTO.GoodsReceiptDetailDTO;
import com.example.medicinedistribution.DTO.StatisticDTO;
import com.example.medicinedistribution.DTO.UserSession;
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
public class GoodsReceiptBUSImpl implements GoodsReceiptBUS {

    private final GoodsReceiptDAO goodsReceiptDAO;
    private final GoodsReceiptDetailDAO goodsReceiptDetailDAO;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final TransactionManager transactionManager;
    private final Validator validator;
    private final ProductDAO productDAO;

    public GoodsReceiptBUSImpl(GoodsReceiptDAO goodsReceiptDAO, GoodsReceiptDetailDAO goodsReceiptDetailDAO,
                               DataSource dataSource, UserSession userSession, TransactionManager transactionManager,
                               Validator validator , ProductDAO productDAO) {
        this.goodsReceiptDAO = goodsReceiptDAO;
        this.goodsReceiptDetailDAO = goodsReceiptDetailDAO;
        this.dataSource = dataSource;
        this.userSession = userSession;
        this.transactionManager = transactionManager;
        this.validator = validator;
        this.productDAO = productDAO;
    }

    private void valid(GoodsReceiptDTO goodsReceiptDTO) {
        Set<ConstraintViolation<GoodsReceiptDTO>> violations = validator.validate(goodsReceiptDTO);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<GoodsReceiptDTO> violation : violations) {
                log.error("Validation error: {} - {}", violation.getPropertyPath(), violation.getMessage());
                throw new IllegalArgumentException(violation.getMessage());
            }
        }
    }

    @Override
    public boolean insert(GoodsReceiptDTO goodsReceiptDTO) {
        valid(goodsReceiptDTO);
        if (!userSession.hasPermission("INSERT_GOODS_RECEIPT")) {
            log.error("User does not have permission to insert goods receipt");
            throw new PermissionDeniedException("Bạn không có quyền thêm phiếu nhập");
        }
        try (Connection connection = transactionManager.beginTransaction()) {
            Integer result = goodsReceiptDAO.insert(goodsReceiptDTO, connection);
            if (result > 0) {
                for (var detail : goodsReceiptDTO.getDetails()) {
                    detail.setGoodsReceiptId(result);
                    if (goodsReceiptDetailDAO.insert(detail, connection) < 0) {
                        transactionManager.rollbackTransaction(connection);
                        log.error("Insert GoodsReceiptDetail failed");
                        throw new InsertFailedException("Thêm chi tiết phiếu nhập thất bại");
                    }
                    // Update product quantity
                    if (!productDAO.increaseQuantity(detail.getProductId(), detail.getQuantity(), connection)) {
                        transactionManager.rollbackTransaction(connection);
                        log.error("Update product quantity failed");
                        throw new InsertFailedException("Cập nhật số lượng sản phẩm thất bại");
                    }

                }
                transactionManager.commitTransaction(connection);
                log.info("Thêm phiếu nhập thành công");
                return true;
            } else {
                transactionManager.rollbackTransaction(connection);
                log.error("Insert GoodsReceipt failed");
                throw new InsertFailedException("Thêm phiếu nhập thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public boolean update(GoodsReceiptDTO goodsReceiptDTO) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean delete(Integer integer) {
        if (!userSession.hasPermission("DELETE_GOODS_RECEIPT")) {
            log.error("User does not have permission to delete goods receipt");
            throw new PermissionDeniedException("Bạn không có quyền xóa phiếu nhập");
        }
        try (Connection connection = transactionManager.beginTransaction()) {
            if (goodsReceiptDetailDAO.delete(integer, connection)) {
                if (!goodsReceiptDAO.delete(integer, connection)) {
                    transactionManager.rollbackTransaction(connection);
                    log.error("Delete GoodsReceiptDetail failed");
                    throw new DeleteFailedException("Xóa chi tiết phiếu nhập thất bại");
                }
                transactionManager.commitTransaction(connection);
                return true;
            } else {
                transactionManager.rollbackTransaction(connection);
                log.error("Delete GoodsReceipt failed");
                throw new DeleteFailedException("Xóa phiếu nhập thất bại");
            }
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public GoodsReceiptDTO findById(Integer integer) {
        if (!userSession.hasPermission("VIEW_GOODS_RECEIPT")) {
            log.error("User does not have permission to view goods receipt");
            throw new PermissionDeniedException("Bạn không có quyền xem phiếu nhập");
        }
        try (Connection connection = dataSource.getConnection()) {
            GoodsReceiptDTO goodsReceiptDTO = goodsReceiptDAO.findById(integer, connection);
            if (goodsReceiptDTO != null) {
                List<GoodsReceiptDetailDTO> details = goodsReceiptDetailDAO.findByGoodsReceiptId(integer, connection);
                if (details != null) {
                    goodsReceiptDTO.setDetails(details);
                } else {
                    log.error("No details found for GoodsReceipt with ID: {}", integer);
                    throw new RuntimeException("Không tìm thấy chi tiết phiếu nhập");
                }
                return goodsReceiptDTO;
            } else {
                log.error("No GoodsReceipt found with ID: {}", integer);
                return null;
            }

        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public List<GoodsReceiptDTO> findAll() {
        if (!userSession.hasPermission("VIEW_GOODS_RECEIPT")) {
            log.error("User does not have permission to view goods receipt");
            throw new PermissionDeniedException("Bạn không có quyền xem phiếu nhập");
        }
        try (Connection connection = dataSource.getConnection()) {
            List<GoodsReceiptDTO> goodsReceiptDTOs = goodsReceiptDAO.findAll(connection);
            for (GoodsReceiptDTO goodsReceiptDTO : goodsReceiptDTOs) {
                List<GoodsReceiptDetailDTO> details = goodsReceiptDetailDAO.findByGoodsReceiptId(goodsReceiptDTO.getGoodsReceiptId(), connection);
                if (details != null) {
                    goodsReceiptDTO.setDetails(details);
                } else {
                    log.error("No details found for GoodsReceipt with ID: {}", goodsReceiptDTO.getGoodsReceiptId());
                }
            }
            return goodsReceiptDTOs;
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }
    @Override
    public List<StatisticDTO> getExpenseSummary(LocalDate fromDate, LocalDate toDate, String viewType) {
        // Group by time period based on viewType (day, week, month, quarter, year)
        String groupBy = switch (viewType) {
            case "Ngày" -> "DATE(date)";
// Better week grouping options
            case "Tuần" -> "CONCAT(YEAR(date), '-W', WEEK(date))"; // Format: "2024-W45"            case "Tháng" -> "DATE_FORMAT(date, '%Y-%m')";
            case "Quý" -> "CONCAT(YEAR(date), '-Q', QUARTER(date))";
            case "Năm" -> "YEAR(date)";
            default -> "DATE_FORMAT(date, '%Y-%m')"; // default to month
        };

        try (Connection conn = dataSource.getConnection()) {
            return goodsReceiptDAO.getExpenseStatistics(fromDate, toDate, groupBy, viewType, conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }

    @Override
    public Map<String, BigDecimal> getExpenseByManufacturerSummary(LocalDate fromDate, LocalDate toDate) {
        try (Connection conn = dataSource.getConnection()) {
            return goodsReceiptDAO.getExpenseByManufacturerStatistics(fromDate, toDate, conn);
        } catch (SQLException e) {
            log.error("Error while getting connection", e);
            throw new RuntimeException("Lỗi khi lấy kết nối", e);
        }
    }
}
