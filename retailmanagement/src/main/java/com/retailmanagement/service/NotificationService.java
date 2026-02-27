package com.retailmanagement.service;

import com.retailmanagement.dto.request.SendNotificationRequest;
import com.retailmanagement.dto.response.CustomerBirthdayResponse;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomRes customerRepository;
    private final SpinWheelService spinWheelService;
    private final PromotionRepository promotionRepository;
    private final PromotionRedemptionRepository promotionRedemptionRepository;

    private static final String BIRTHDAY_VOUCHER_CODE = "BIRTHDAY250K";
    private static final BigDecimal BIRTHDAY_DISCOUNT_AMOUNT = new BigDecimal("250000");
    private static final BigDecimal BIRTHDAY_MIN_ORDER = new BigDecimal("1000000");
    private static final int BIRTHDAY_VALID_DAYS = 7;

    public NotificationService(
            NotificationRepository notificationRepository,
            CustomRes customerRepository,
            @Lazy SpinWheelService spinWheelService,
            PromotionRepository promotionRepository,
            PromotionRedemptionRepository promotionRedemptionRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.customerRepository = customerRepository;
        this.spinWheelService = spinWheelService;
        this.promotionRepository = promotionRepository;
        this.promotionRedemptionRepository = promotionRedemptionRepository;
    }

    // =========================================================
    // GENERIC CREATE NOTIFICATION
    // =========================================================

    @Transactional
    public void createAndSaveNotification(
            Integer customerId,
            NotificationType type,
            String title,
            String message
    ) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng: " + customerId));

        Notification notification = Notification.builder()
                .customer(customer)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    // =========================================================
    // BIRTHDAY VOUCHER CORE
    // =========================================================

    private Promotion ensureBirthdayVoucherExists() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(BIRTHDAY_VALID_DAYS);

        Optional<Promotion> existing = promotionRepository.findByCode(BIRTHDAY_VOUCHER_CODE);

        if (existing.isPresent()) {
            Promotion p = existing.get();
            if (p.getIsActive() && p.getEndDate().isAfter(now)) {
                return p;
            }
            p.setStartDate(now);
            p.setEndDate(endDate);
            p.setIsActive(true);
            return promotionRepository.save(p);
        }

        Promotion voucher = Promotion.builder()
                .code(BIRTHDAY_VOUCHER_CODE)
                .name("🎂 Voucher sinh nhật — Giảm 250.000đ")
                .description("Giảm 250.000đ cho đơn từ 1.000.000đ. Hiệu lực 7 ngày.")
                .discountType("AMOUNT")
                .discountValue(BIRTHDAY_DISCOUNT_AMOUNT)
                .minOrderAmount(BIRTHDAY_MIN_ORDER)
                .priority(10)
                .stackable(false)
                .startDate(now)
                .endDate(endDate)
                .isActive(true)
                .createdAt(now)
                .build();

        Promotion saved = promotionRepository.save(voucher);

        PromotionRedemption redemption = new PromotionRedemption();
        redemption.setPromotionId(saved.getId());
        redemption.setUsedCount(0L);
        redemption.setUpdatedAt(now);
        promotionRedemptionRepository.save(redemption);

        return saved;
    }

    @Transactional
    public void createBirthdayNotification(Customer customer) {
        LocalDateTime yearStart = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime yearEnd = yearStart.plusYears(1);

        boolean exists = notificationRepository
                .existsByCustomerIdAndTypeAndCreatedAtBetween(
                        customer.getId(),
                        NotificationType.BIRTHDAY,
                        yearStart,
                        yearEnd
                );

        if (exists) return;

        Promotion voucher = ensureBirthdayVoucherExists();

        String expiryDisplay = voucher.getEndDate()
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String message = String.format(
                "🎉 Sinh nhật vui vẻ %s!\n\n" +
                        "🎁 Mã: %s\n" +
                        "💰 Giảm: 250.000đ\n" +
                        "🛒 Đơn tối thiểu: 1.000.000đ\n" +
                        "⏳ Hết hạn: %s",
                customer.getName(),
                BIRTHDAY_VOUCHER_CODE,
                expiryDisplay
        );

        createAndSaveNotification(
                customer.getId(),
                NotificationType.BIRTHDAY,
                "🎂 Chúc mừng sinh nhật!",
                message
        );
    }

    @Transactional
    public void sendBirthdayNotifications() {
        List<Customer> customers = customerRepository.findCustomersWithBirthdayToday();

        for (Customer customer : customers) {
            if (customer.getIsActive()) {
                createBirthdayNotification(customer);
            }
        }
    }

    // =========================================================
    // CUSTOM BIRTHDAY (GỘP)
    // =========================================================

    @Transactional
    public void sendCustomBirthdayGreeting(Integer customerId, String customMessage) {
        createAndSaveNotification(
                customerId,
                NotificationType.BIRTHDAY,
                "🎉 Lời chúc sinh nhật từ Admin",
                customMessage
        );
    }

    // =========================================================
    // VIP TIER LOGIC (80% RULE)
    // =========================================================

    @Transactional
    public void checkAndSendTierUpgradeNotifications() {
        List<Customer> customers = customerRepository.findAll()
                .stream()
                .filter(Customer::getIsActive)
                .toList();

        for (Customer customer : customers) {

            int currentPoints = Optional.ofNullable(customer.getLoyaltyPoints()).orElse(0);
            VipTier nextTier = getNextTier(customer.getVipTier());

            if (nextTier != null) {
                int pointsGap = nextTier.getMinPoints() - currentPoints;

                int tierRange = customer.getVipTier() != null
                        ? nextTier.getMinPoints() - customer.getVipTier().getMinPoints()
                        : nextTier.getMinPoints();

                double progress = tierRange > 0
                        ? (double) (tierRange - pointsGap) / tierRange * 100
                        : 0;

                if (progress >= 80 && pointsGap > 0) {
                    createAndSaveNotification(
                            customer.getId(),
                            NotificationType.VIP_TIER_UPGRADE,
                            "🎯 Bạn sắp lên hạng!",
                            String.format(
                                    "🔥 Bạn chỉ còn thiếu %,d điểm để đạt hạng %s!",
                                    pointsGap,
                                    nextTier.getDisplayName()
                            )
                    );
                }
            }
        }
    }

    private VipTier getNextTier(VipTier currentTier) {
        if (currentTier == null) return VipTier.BRONZE;
        VipTier[] tiers = VipTier.values();
        int idx = currentTier.ordinal();
        return idx < tiers.length - 1 ? tiers[idx + 1] : null;
    }

    // =========================================================
    // PROMOTION EXPIRY WARNING
    // =========================================================

    @Transactional
    public void sendPromotionExpiryWarnings(List<Promotion> expiringPromos) {

        List<Customer> customers = customerRepository.findAll()
                .stream()
                .filter(Customer::getIsActive)
                .toList();

        for (Promotion promo : expiringPromos) {

            String message = String.format(
                    "🔔 Khuyến mãi \"%s\" (mã: %s) sắp hết hạn vào %s!",
                    promo.getName(),
                    promo.getCode(),
                    promo.getEndDate().toLocalDate()
            );

            for (Customer customer : customers) {

                LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

                boolean exists = notificationRepository
                        .existsByCustomerIdAndTypeAndCreatedAtBetween(
                                customer.getId(),
                                NotificationType.PROMOTION,
                                oneDayAgo,
                                LocalDateTime.now()
                        );

                if (!exists) {
                    createAndSaveNotification(
                            customer.getId(),
                            NotificationType.PROMOTION,
                            "⏰ Khuyến mãi sắp hết hạn!",
                            message
                    );
                }
            }
        }
    }
}