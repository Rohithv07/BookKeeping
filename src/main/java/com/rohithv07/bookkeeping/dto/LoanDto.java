package com.rohithv07.bookkeeping.dto;

import com.rohithv07.bookkeeping.model.LoanStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDto {
    private Long id;

    @NotNull(message = "Borrower ID is required")
    private Long borrowerId;

    // Read-only field for response convenience
    private String borrowerName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Date lent is required")
    private LocalDate dateLent;

    private LocalDate dueDate;

    private LoanStatus status;
}
