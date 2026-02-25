package com.retailmanagement.service;

import com.retailmanagement.dto.response.CustomerBirthdayResponse;
import com.retailmanagement.entity.*;
import com.retailmanagement.repository.CustomRes;
import com.retailmanagement.repository.NotificationRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomRes customerRepository;
    private final SpinWheelService spinWheelService;

    // Constructor thủ công — @Lazy trên SpinWheelService để tránh circular dependency
    public NotificationService(
            NotificationRepository notificationRepository,
            CustomRes customerRepository,
            @Lazy SpinWheelService spinWheelService
    ) {
        this.notificationRepository = notificationRepository;
        this.customerRepository     = customerRepository;
        this.spinWheelService       = spinWheelService;
    }

    // ================================================================
    // ✅ HELPER CHUNG — dùng ở mọi nơi trong service
    // ================================================================
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

    // ================================================================
    // BIRTHDAY
    // ================================================================

    @Transactional
    public void createBirthdayNotification(Customer customer) {
        LocalDateTime yearStart = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime yearEnd   = LocalDate.now().withDayOfYear(1).plusYears(1).atStartOfDay();

        boolean exists = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                customer.getId(), NotificationType.BIRTHDAY, yearStart, yearEnd
        );

        if (!exists) {
            Notification notification = Notification.builder()
                    .customer(customer)
                    .type(NotificationType.BIRTHDAY)
                    .title("🎂 Chúc mừng sinh nhật!")
                    .message(String.format(
                            "Chúc mừng sinh nhật %s! 🎉 Chúc bạn một ngày thật vui vẻ và hạnh phúc. " +
                                    "Đừng quên check voucher sinh nhật đặc biệt của bạn nhé!",
                            customer.getName()
                    ))
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void sendBirthdayNotifications() {
        List<Customer> birthdayCustomers = customerRepository.findCustomersWithBirthdayToday();

        for (Customer customer : birthdayCustomers) {
            if (customer.getIsActive()) {
                createBirthdayNotification(customer);
            }
        }

        System.out.println("📨 Đã gửi " + birthdayCustomers.size() + " thông báo sinh nhật");
    }

    @Transactional
    public void createCustomBirthdayNotification(Integer customerId, String customMessage) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        if (!customer.getIsActive()) {
            throw new RuntimeException("Khách hàng không hoạt động");
        }

        Notification notification = Notification.builder()
                .customer(customer)
                .type(NotificationType.BIRTHDAY)
                .title("🎂 Lời chúc sinh nhật từ Admin")
                .message(customMessage)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    public void sendCustomBirthdayGreeting(Integer customerId, String customMessage) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        Notification notification = Notification.builder()
                .customer(customer)
                .type(NotificationType.BIRTHDAY)
                .title("🎉 Lời chúc sinh nhật từ Admin")
                .message(customMessage)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    public List<Notification> getBirthdayNotificationHistory() {
        return notificationRepository.findByTypeOrderByCreatedAtDesc(NotificationType.BIRTHDAY);
    }

    public List<Notification> getBirthdayNotificationHistory(LocalDateTime from, LocalDateTime to) {
        return notificationRepository.findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(
                NotificationType.BIRTHDAY, from, to
        );
    }

    // ================================================================
    // READ / DELETE / COUNT
    // ================================================================

    public List<Notification> getUnreadNotifications(Integer customerId) {
        return notificationRepository.findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId);
    }

    public List<Notification> getAllNotifications(Integer customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public long countUnread(Integer customerId) {
        return notificationRepository.countByCustomerIdAndIsReadFalse(customerId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Integer customerId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (!notification.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền đọc thông báo này");
        }

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(Integer customerId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByCustomerIdAndIsReadFalseOrderByCreatedAtDesc(customerId);

        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        if (!unreadNotifications.isEmpty()) {
            notificationRepository.saveAll(unreadNotifications);
        }
    }

    @Transactional
    public void deleteNotification(Long notificationId, Integer customerId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (!notification.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Bạn không có quyền xóa thông báo này");
        }

        notificationRepository.delete(notification);
    }

    // ================================================================
    // BIRTHDAY CALENDAR
    // ================================================================

    public List<CustomerBirthdayResponse> getBirthdaysByMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        List<Customer> customers = customerRepository.findCustomersWithBirthdayInMonth(month);
        return customers.stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(CustomerBirthdayResponse::getBirthDay))
                .collect(Collectors.toList());
    }

    public List<CustomerBirthdayResponse> getUpcomingBirthdays(int days) {
        LocalDate today   = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        List<Customer> customers = customerRepository.findCustomersWithBirthdayBetween(
                today.getMonthValue(),
                today.getDayOfMonth(),
                endDate.getMonthValue(),
                endDate.getDayOfMonth()
        );

        return customers.stream()
                .map(this::mapToResponse)
                .filter(c -> c.getDaysUntilBirthday() != null && c.getDaysUntilBirthday() <= days)
                .sorted(Comparator.comparing(CustomerBirthdayResponse::getDaysUntilBirthday))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistics() {
        Map<Integer, Long> monthlyCount = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            monthlyCount.put(month, customerRepository.countCustomersWithBirthdayInMonth(month));
        }

        long totalCustomers        = customerRepository.count();
        long customersWithBirthday = monthlyCount.values().stream().mapToLong(Long::longValue).sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("monthlyCount",             monthlyCount);
        stats.put("totalCustomers",           totalCustomers);
        stats.put("customersWithBirthday",    customersWithBirthday);
        stats.put("customersWithoutBirthday", totalCustomers - customersWithBirthday);
        return stats;
    }

    public Map<String, Object> getMonthlyStatistics(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng phải từ 1 đến 12");
        }

        List<CustomerBirthdayResponse> birthdays = getBirthdaysByMonth(month);

        Map<String, Long> byType = birthdays.stream()
                .collect(Collectors.groupingBy(c -> c.getCustomerType().name(), Collectors.counting()));

        Map<String, Long> byTier = birthdays.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getVipTier() != null ? c.getVipTier().name() : "NONE",
                        Collectors.counting()
                ));

        Map<String, Object> stats = new HashMap<>();
        stats.put("month",          month);
        stats.put("totalCount",     birthdays.size());
        stats.put("byCustomerType", byType);
        stats.put("byVipTier",      byTier);
        stats.put("birthdays",      birthdays);
        return stats;
    }

    // ================================================================
    // VIP TIER UPGRADE
    // ================================================================

    @Transactional
    public void createTierUpgradeNotification(Integer customerId, String tierName, int pointsGap) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        boolean exists = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                customer.getId(), NotificationType.VIP_TIER_UPGRADE, weekAgo, LocalDateTime.now()
        );

        if (!exists) {
            String message;
            if (pointsGap <= 500) {
                message = String.format(
                        "🔥 Bạn chỉ còn thiếu %,d điểm nữa là lên hạng %s! " +
                                "Hoàn tất một đơn hàng nhỏ để nhận ưu đãi tốt hơn ngay!",
                        pointsGap, tierName
                );
            } else {
                message = String.format(
                        "⭐ Bạn sắp đạt hạng %s với %,d điểm nữa! " +
                                "Tiếp tục mua sắm để tận hưởng nhiều đặc quyền hơn.",
                        tierName, pointsGap
                );
            }

            createAndSaveNotification(customerId, NotificationType.VIP_TIER_UPGRADE,
                    "🎯 Bạn sắp lên hạng!", message);
        }
    }

    @Transactional
    public void createUpgradeToVipNotification(Integer customerId, int pointsNeeded) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        if (customer.getCustomerType() == CustomerType.REGULAR) {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            boolean exists = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                    customer.getId(), NotificationType.VIP_TIER_UPGRADE, weekAgo, LocalDateTime.now()
            );

            if (!exists) {
                String message = String.format(
                        "👑 Chỉ còn %,d điểm nữa, bạn sẽ trở thành khách hàng VIP! " +
                                "Khách VIP được giảm giá cao hơn, ưu đãi độc quyền và nhiều đặc quyền khác. " +
                                "Mua sắm ngay để nâng cấp!",
                        pointsNeeded
                );
                createAndSaveNotification(customerId, NotificationType.VIP_TIER_UPGRADE,
                        "👑 Sắp trở thành VIP!", message);
            }
        }
    }

    @Transactional
    public void checkAndSendTierUpgradeNotifications() {
        List<Customer> customers = customerRepository.findAll().stream()
                .filter(Customer::getIsActive)
                .toList();

        for (Customer customer : customers) {
            int currentPoints = customer.getLoyaltyPoints() != null ? customer.getLoyaltyPoints() : 0;
            VipTier currentTier = customer.getVipTier();

            VipTier nextTier = getNextTier(currentTier);
            if (nextTier != null) {
                int pointsGap = nextTier.getMinPoints() - currentPoints;
                int tierRange = currentTier != null
                        ? nextTier.getMinPoints() - currentTier.getMinPoints()
                        : nextTier.getMinPoints();

                double progress = tierRange > 0
                        ? (double)(tierRange - pointsGap) / tierRange * 100
                        : 0;

                if (progress >= 80 && pointsGap > 0) {
                    createTierUpgradeNotification(customer.getId(), nextTier.getDisplayName(), pointsGap);
                }
            }

            if (customer.getCustomerType() == CustomerType.REGULAR) {
                int goldMinPoints = VipTier.GOLD.getMinPoints();
                if (currentPoints >= goldMinPoints * 0.7 && currentPoints < goldMinPoints) {
                    createUpgradeToVipNotification(customer.getId(), goldMinPoints - currentPoints);
                }
            }
        }
    }

    private VipTier getNextTier(VipTier currentTier) {
        if (currentTier == null) return VipTier.BRONZE;
        VipTier[] tiers  = VipTier.values();
        int currentIndex = currentTier.ordinal();
        return currentIndex < tiers.length - 1 ? tiers[currentIndex + 1] : null;
    }

    // ================================================================
    // PURCHASE REMINDER
    // ================================================================

    @Transactional
    public void sendPurchaseReminders() {
        LocalDateTime now         = LocalDateTime.now();
        LocalDateTime twoWeeksAgo = now.minusDays(14);

        List<Customer> cold30  = customerRepository.findCustomersForPurchaseReminder(
                now.minusDays(30), now.minusDays(60), twoWeeksAgo);
        List<Customer> cold60  = customerRepository.findCustomersForPurchaseReminder(
                now.minusDays(60), now.minusDays(120), twoWeeksAgo);
        List<Customer> winback = customerRepository.findCustomersForPurchaseReminder(
                now.minusDays(120), now.minusDays(365), twoWeeksAgo);

        System.out.println("🔍 DEBUG now=" + now);
        System.out.println("🔍 COLD_30 range: " + now.minusDays(60) + " → " + now.minusDays(30));
        System.out.println("🔍 cold30 found: "  + cold30.size()  + " customers");
        System.out.println("🔍 cold60 found: "  + cold60.size()  + " customers");
        System.out.println("🔍 winback found: " + winback.size() + " customers");
        cold30.forEach(c  -> System.out.println("   cold30: "  + c.getName() + " lastOrder=" + c.getLastOrderAt()));
        cold60.forEach(c  -> System.out.println("   cold60: "  + c.getName() + " lastOrder=" + c.getLastOrderAt()));
        winback.forEach(c -> System.out.println("   winback: " + c.getName() + " lastOrder=" + c.getLastOrderAt()));

        cold30.forEach(c  -> createPurchaseReminderNotification(c, "COLD_30"));
        cold60.forEach(c  -> createPurchaseReminderNotification(c, "COLD_60"));
        winback.forEach(c -> createPurchaseReminderNotification(c, "WINBACK"));
    }

    @Transactional
    public void createPurchaseReminderNotification(Customer customer, String segment) {
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusDays(14);

        boolean alreadySent = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                customer.getId(), NotificationType.PURCHASE_REMINDER, twoWeeksAgo, LocalDateTime.now());
        boolean alreadySentWinback = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtBetween(
                customer.getId(), NotificationType.WINBACK, twoWeeksAgo, LocalDateTime.now());

        if (alreadySent || alreadySentWinback) return;

        long daysSinceLastOrder = ChronoUnit.DAYS.between(
                customer.getLastOrderAt(), LocalDateTime.now()
        );

        String title;
        String message;
        NotificationType type;

        switch (segment) {
            case "COLD_30" -> {
                type    = NotificationType.PURCHASE_REMINDER;
                title   = "👀 Có laptop mới về rồi bạn ơi!";
                message = String.format(
                        "Chào %s! Đã %d ngày rồi bạn chưa ghé thăm chúng tôi. " +
                                "Nhiều dòng laptop mới vừa về kho, cùng xem nhé!",
                        customer.getName(), daysSinceLastOrder
                );
            }
            case "COLD_60" -> {
                type    = NotificationType.PURCHASE_REMINDER;
                title   = "🎁 Ưu đãi riêng dành cho bạn – chỉ còn 48h!";
                message = String.format(
                        "Chào %s! Chúng tôi nhớ bạn quá. " +
                                "Nhân dịp bạn chưa ghé lâu (%d ngày), " +
                                "shop gửi tặng bạn ưu đãi độc quyền. Xem ngay trước khi hết hạn!",
                        customer.getName(), daysSinceLastOrder
                );
            }
            default -> {
                type    = NotificationType.WINBACK;
                title   = "💝 Chúng tôi nhớ bạn – Quà tặng đặc biệt!";
                message = String.format(
                        "Chào %s! Đã hơn %d ngày rồi bạn chưa ghé shop. " +
                                "Chúng tôi có quà tặng đặc biệt dành riêng cho bạn. " +
                                "Hãy quay lại để nhận ngay nhé! 🎉",
                        customer.getName(), daysSinceLastOrder
                );
            }
        }

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

    public List<Notification> getPurchaseReminderHistory() {
        return notificationRepository.findByTypeOrderByCreatedAtDesc(NotificationType.PURCHASE_REMINDER);
    }

    // ================================================================
    // ✅ SPIN EXPIRY WARNING
    // ================================================================

    @Transactional
    public void sendSpinExpiryWarnings() {
        List<Map<String, Object>> expiringList = spinWheelService.getExpiringBonuses(24);

        int sent = 0;
        for (Map<String, Object> item : expiringList) {
            Integer    customerId = (Integer)    item.get("customerId");
            BigDecimal bonus      = (BigDecimal) item.get("discountBonus");
            long       hoursLeft  = (long)       item.get("hoursLeft");

            // Chỉ gửi 1 lần / 12h
            boolean alreadySent = notificationRepository.existsByCustomerIdAndTypeAndCreatedAtAfter(
                    customerId,
                    NotificationType.SPIN_EXPIRY_WARNING,
                    LocalDateTime.now().minusHours(12)
            );
            if (alreadySent) continue;

            String title   = "🎡 Ưu đãi vòng quay sắp hết hạn!";
            String message = String.format(
                    "Bạn còn ưu đãi giảm %.0f%% từ vòng quay chưa sử dụng. " +
                            "Còn %d giờ nữa là hết hạn — hãy đặt hàng ngay!",
                    bonus, hoursLeft
            );

            createAndSaveNotification(customerId, NotificationType.SPIN_EXPIRY_WARNING, title, message);
            sent++;
        }

        System.out.println("✅ Đã gửi " + sent + " cảnh báo spin sắp hết hạn");
    }

    // ================================================================
    // PRIVATE HELPER
    // ================================================================

    private CustomerBirthdayResponse mapToResponse(Customer customer) {
        LocalDate birthDate = customer.getDateOfBirth();
        if (birthDate == null) return null;

        LocalDate today = LocalDate.now();
        int age = Period.between(birthDate, today).getYears();

        LocalDate nextBirthday = LocalDate.of(today.getYear(), birthDate.getMonth(), birthDate.getDayOfMonth());
        if (nextBirthday.isBefore(today)) {
            nextBirthday = nextBirthday.plusYears(1);
            age++;
        }

        long    daysUntil = ChronoUnit.DAYS.between(today, nextBirthday);
        boolean isToday   = daysUntil == 0;

        return CustomerBirthdayResponse.builder()
                .id(Long.valueOf(customer.getId()))
                .customerId(customer.getId())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .dateOfBirth(birthDate)
                .birthdayDisplay(String.format("%02d/%02d/%d",
                        birthDate.getDayOfMonth(),
                        birthDate.getMonthValue(),
                        birthDate.getYear()))
                .birthMonth(birthDate.getMonthValue())
                .birthDay(birthDate.getDayOfMonth())
                .age(age)
                .daysUntilBirthday(daysUntil)
                .isBirthdayToday(isToday)
                .customerType(customer.getCustomerType())
                .vipTier(customer.getVipTier())
                .loyaltyPoints(customer.getLoyaltyPoints())
                .totalSpent(customer.getTotalSpent())
                .build();
    }
}