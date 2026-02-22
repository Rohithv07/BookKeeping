package com.rohithv07.bookkeeping.service;

import com.rohithv07.bookkeeping.dto.LoanDto;
import com.rohithv07.bookkeeping.exception.ResourceNotFoundException;
import com.rohithv07.bookkeeping.model.Borrower;
import com.rohithv07.bookkeeping.model.Loan;
import com.rohithv07.bookkeeping.model.LoanStatus;
import com.rohithv07.bookkeeping.repository.BorrowerRepository;
import com.rohithv07.bookkeeping.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanServiceImpl implements LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanServiceImpl.class);

    // Explicit constructor injection without Lombok magic
    private final LoanRepository loanRepository;
    private final BorrowerRepository borrowerRepository;

    public LoanServiceImpl(LoanRepository loanRepository, BorrowerRepository borrowerRepository) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
    }

    @Override
    public LoanDto addLoan(LoanDto loanDto) {
        log.info("Adding new loan for borrower ID: {}", loanDto.getBorrowerId());

        Borrower borrower = borrowerRepository.findById(loanDto.getBorrowerId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Borrower not found with ID: " + loanDto.getBorrowerId()));

        Loan loan = Loan.builder()
                .borrower(borrower)
                .amount(loanDto.getAmount())
                .dateLent(loanDto.getDateLent())
                .status(LoanStatus.ACTIVE)
                .build();

        Loan savedLoan = loanRepository.save(loan);
        log.debug("Saved loan with ID: {}", savedLoan.getId());

        return mapToDto(savedLoan);
    }

    @Override
    public List<LoanDto> getAllLoans() {
        log.info("Fetching all loans");
        List<Loan> loans = loanRepository.findAll();
        log.debug("Found {} loans in total", loans.size());

        return loans.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanDto> getActiveLoans() {
        log.info("Fetching all active loans");
        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);
        log.debug("Found {} active loans", activeLoans.size());

        return activeLoans.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public LoanDto getLoanById(Long id) {
        log.info("Fetching loan with ID: {}", id);
        Loan loan = getLoanEntityById(id);
        return mapToDto(loan);
    }

    @Override
    public void deleteLoan(Long id) {
        log.info("Deleting loan ID {}", id);
        Loan loan = getLoanEntityById(id);
        loanRepository.delete(loan);
        log.debug("Loan ID {} successfully deleted", id);
    }

    // Internal helper to get entity
    private Loan getLoanEntityById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Loan not found with ID: {}", id);
                    return new ResourceNotFoundException("Loan not found with ID: " + id);
                });
    }

    // Internal helper to map Entity to DTO
    private LoanDto mapToDto(Loan loan) {
        return LoanDto.builder()
                .id(loan.getId())
                .borrowerId(loan.getBorrower() != null ? loan.getBorrower().getId() : null)
                .borrowerName(loan.getBorrower() != null ? loan.getBorrower().getName() : null)
                .amount(loan.getAmount())
                .dateLent(loan.getDateLent())
                .dueDate(loan.getDueDate())
                .status(loan.getStatus())
                .build();
    }
}
