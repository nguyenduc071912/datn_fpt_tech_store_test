package com.retailmanagement.repository;

import com.retailmanagement.entity.Notification;
import com.retailmanagement.entity.NotificationType;
import com.retailmanagement.entity.SpinWheelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Tìm tất cả thông báo của khách hàng theo thứ tự mới nhất
     */
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    /**
     * Tìm thông báo chưa đọc của khách hàng
     */
    List<Notification> findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(Integer customerId);

    /**
     * Đếm số thông báo chưa đọc
     */
    long countByCustomerIdAndIsReadFalse(Integer customerId);

    /**
     * Kiểm tra xem đã có thông báo loại này trong khoảng thời gian chưa
     */
    boolean existsByCustomerIdAndTypeAndCreatedAtBetween(
            Integer customerId,
            NotificationType type,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Tìm thông báo theo loại
     */
    List<Notification> findByCustomerIdAndTypeOrderByCreatedAtDesc(Integer customerId, NotificationType type);




    // ✅ THÊM: Lấy thông báo theo loại
    List<Notification> findByTypeOrderByCreatedAtDesc(NotificationType type);

    // ✅ THÊM: Lấy thông báo theo loại và khoảng thời gian
    List<Notification> findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
            NotificationType type, LocalDateTime from, LocalDateTime to);
    boolean existsByCustomerIdAndTypeAndCreatedAtAfter(
            Integer customerId,
            NotificationType type,
            LocalDateTime after
    );

    @Query("SELECT s FROM SpinWheelHistory s " +
            "WHERE s.isUsed = false " +
            "AND s.expiresAt > :now " +
            "AND s.expiresAt <= :threshold " +
            "ORDER BY s.expiresAt ASC")
    List<SpinWheelHistory> findExpiringUnusedBonuses(
            @Param("now")       LocalDateTime now,
            @Param("threshold") LocalDateTime threshold
    );

    /**
     * Tìm spin history theo orderId đã dùng — dùng cho restoreBonus()
     */

}