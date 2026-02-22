package com.rohithv07.bookkeeping.service;

import com.rohithv07.bookkeeping.dto.LoanDto;
import java.util.List;

public interface LoanService {
    LoanDto addLoan(LoanDto loanDto);

    List<LoanDto> getAllLoans();

    List<LoanDto> getActiveLoans();

    LoanDto getLoanById(Long id);

    void deleteLoan(Long id);

    void repayLoan(Long id, java.math.BigDecimal amount);
}
