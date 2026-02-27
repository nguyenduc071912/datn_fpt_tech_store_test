package com.retailmanagement.audit;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class AuditLogFilterRequest {
    private Long userId;

    private String module;
    private String action;

    private String targetType;
    private Long targetId;

    private String ipAddress;

    private String keyword;

    private LocalDate startDate;
    private LocalDate  endDate;

    private List<String> modules;
    private List<String> actions;
}
