package com.rohithv07.bookkeeping.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "loans" })
    @ManyToOne(optional = false)
    @JoinColumn(name = "borrower_id", nullable = false)
    private Borrower borrower;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Date lent is required")
    private LocalDate dateLent;

    private LocalDate dueDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private LoanStatus status = LoanStatus.ACTIVE;

    @PrePersist
    public void prePersist() {
        if (this.dueDate == null && this.dateLent != null) {
            this.dueDate = this.dateLent.plusMonths(1);
        }
    }
}
