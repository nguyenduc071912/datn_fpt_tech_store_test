package com.retailmanagement.security.log;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityLogService {

    private final SecurityLogRepository securityLogRepository;

    public void log(
            String username,
            ActionType actionType,
            String targetEntity,
            String targetId,
            String ip,
            SeverityLevel severity,
            String status,
            String description
    ) {
        try {
            SecurityLog log = SecurityLog.builder()
                    .username(username)
                    .actionType(actionType != null ? actionType.name() : null)
                    .targetEntity(targetEntity)
                    .targetId(targetId)
                    .ipAddress(ip)
                    .severity(severity != null ? severity.name() : null)
                    .status(status)
                    .description(description)
                    .build();

            securityLogRepository.save(log);

        } catch (Exception ex) {
            // Không throw ra ngoài để tránh ảnh hưởng business flow
        }
    }

    public Page<SecurityLog> filterLogs(
            SecurityLogFilterRequest request,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {

        // ===== 1. Giới hạn page size =====
        size = Math.min(size, 100); // max 100 record / page

        // ===== 2. Whitelist field được sort =====
        List<String> allowedSortFields = List.of(
                "createdAt",
                "username",
                "actionType",
                "severity",
                "status",
                "ipAddress"
        );

        if (!allowedSortFields.contains(sortBy)) {
            sortBy = "createdAt";
        }

        // ===== 3. Validate sort direction =====
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDir);
        } catch (Exception e) {
            direction = Sort.Direction.DESC;
        }

        // ===== 4. Build Pageable =====
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                size,
                Sort.by(direction, sortBy)
        );

        // ===== 5. Specification =====
        Specification<SecurityLog> spec =
                Specification.where(SecurityLogSpecification.filter(request));

        return securityLogRepository.findAll(spec, pageable);
    }
}
