package com.splitmate.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ExpenseRequest {

    @Data
    public static class Create {
        @NotBlank(message = "Description is required")
        @Size(max = 500)
        private String description;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        @NotNull(message = "Paid by user ID is required")
        private Long paidById;

        private String splitType = "EQUAL";
        private String category = "OTHER";
        private List<Long> participantIds;
        private Map<Long, BigDecimal> exactSplits;
        private Map<Long, BigDecimal> percentageSplits;
    }

    @Data
    public static class Update {
        @Size(max = 500)
        private String description;

        @DecimalMin(value = "0.01")
        private BigDecimal amount;

        private Long paidById;
        private String splitType;
        private String category;
        private List<Long> participantIds;
        private Map<Long, BigDecimal> exactSplits;
        private Map<Long, BigDecimal> percentageSplits;
    }
}