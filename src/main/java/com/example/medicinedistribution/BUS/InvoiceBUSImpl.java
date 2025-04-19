package com.example.medicinedistribution.BUS;

import com.example.medicinedistribution.BUS.Interface.InvoiceBUS;
import com.example.medicinedistribution.DAO.Interface.InvoiceDAO;
import com.example.medicinedistribution.DAO.Interface.InvoiceDetailDAO;
import com.example.medicinedistribution.DTO.InvoiceDTO;
import com.example.medicinedistribution.DTO.InvoiceDetailDTO;
import com.example.medicinedistribution.DTO.UserSession;
import com.example.medicinedistribution.Exception.DeleteFailedException;
import com.example.medicinedistribution.Exception.InsertFailedException;
import com.example.medicinedistribution.Exception.PermissionDeniedException;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Slf4j
public class InvoiceBUSImpl implements InvoiceBUS {

    private final InvoiceDAO invoiceDAO;
    private final InvoiceDetailDAO invoiceDetailDAO;
    private final DataSource dataSource;
    private final UserSession userSession;
    private final TransactionManager transactionManager;
    private final Validator validator;

    public InvoiceBUSImpl(InvoiceDAO invoiceDAO, InvoiceDetailDAO invoiceDetailDAO,
                          DataSource dataSource, UserSession userSession, TransactionManager transactionManager, Validator validator) {
        this.invoiceDAO = invoiceDAO;
        this.invoiceDetailDAO = invoiceDetailDAO;
        this.dataSource = dataSource;
        this.userSession = userSession;
        this.transactionManager = transactionManager;
        this.validator = validator;
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

    @Override
    public boolean insert(InvoiceDTO invoiceDTO) {
        valid(invoiceDTO);
        if(!userSession.hasPermission("INSERT_INVOICE")){
            log.error("User does not have permission to insert invoice");
            throw new PermissionDeniedException("Bạn không có quyền thêm hóa đơn");
        }

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
        if(!userSession.hasPermission("VIEW_INVOICE")){
            log.error("User does not have permission to view invoice");
            throw new PermissionDeniedException("Bạn không có quyền xem hóa đơn");
        }
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
        if(!userSession.hasPermission("VIEW_INVOICE")){
            log.error("User does not have permission to view invoice");
            throw new PermissionDeniedException("Bạn không có quyền xem hóa đơn");
        }
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
}
