package com.rohithv07.bookkeeping.controller;

import com.rohithv07.bookkeeping.dto.LoanDto;
import com.rohithv07.bookkeeping.service.LoanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import com.rohithv07.bookkeeping.dto.RepaymentRequest;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@Slf4j
public class LoanController {

    // Explicit constructor injection without Lombok magic
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping
    public ResponseEntity<LoanDto> addLoan(@Valid @RequestBody LoanDto loanDto) {
        log.info("REST request to add a new loan");
        return ResponseEntity.ok(loanService.addLoan(loanDto));
    }

    @GetMapping
    public ResponseEntity<List<LoanDto>> getAllActiveLoans() {
        log.info("REST request to get all active loans");
        return ResponseEntity.ok(loanService.getActiveLoans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanDto> getLoanById(@PathVariable Long id) {
        log.info("REST request to get loan by ID: {}", id);
        return ResponseEntity.ok(loanService.getLoanById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        log.info("REST request to delete loan ID {}", id);
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/repay")
    public ResponseEntity<Void> repayLoan(@PathVariable Long id, @Valid @RequestBody RepaymentRequest request) {
        log.info("REST request to partial/full repay loan ID {}", id);
        loanService.repayLoan(id, request.getAmount());
        return ResponseEntity.noContent().build();
    }
}
