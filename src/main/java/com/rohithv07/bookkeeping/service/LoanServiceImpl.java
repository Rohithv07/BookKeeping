package com.rohithv07.bookkeeping.service;

import com.rohithv07.bookkeeping.dto.LoanDto;
import com.rohithv07.bookkeeping.exception.ResourceNotFoundException;
import com.rohithv07.bookkeeping.model.AppUser;
import com.rohithv07.bookkeeping.model.Borrower;
import com.rohithv07.bookkeeping.model.Loan;
import com.rohithv07.bookkeeping.model.LoanStatus;
import com.rohithv07.bookkeeping.repository.AppUserRepository;
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
    private final AppUserRepository userRepository;

    public LoanServiceImpl(LoanRepository loanRepository, BorrowerRepository borrowerRepository,
            AppUserRepository userRepository) {
        this.loanRepository = loanRepository;
        this.borrowerRepository = borrowerRepository;
        this.userRepository = userRepository;
    }

    private String getCurrentUsername() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
    }

    @Override
    public LoanDto addLoan(LoanDto loanDto) {
        String username = getCurrentUsername();
        log.info("Adding new loan for borrower ID: {} by user: {}", loanDto.getBorrowerId(), username);

        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Borrower borrower = borrowerRepository.findByIdAndUserUsername(loanDto.getBorrowerId(), username)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Borrower not found natively or access denied for ID: " + loanDto.getBorrowerId()));

        String finalCurrency = (loanDto.getCurrency() != null && !loanDto.getCurrency().trim().isEmpty())
                ? loanDto.getCurrency().trim().toUpperCase()
                : "USD";

        Loan loan = Loan.builder()
                .borrower(borrower)
                .amount(loanDto.getAmount())
                .currency(finalCurrency)
                .dateLent(loanDto.getDateLent())
                .status(LoanStatus.ACTIVE)
                .user(user)
                .build();

        Loan savedLoan = loanRepository.save(loan);
        log.debug("Saved loan with ID: {}", savedLoan.getId());

        return mapToDto(savedLoan);
    }

    @Override
    public List<LoanDto> getAllLoans() {
        log.info("Fetching all loans for user: {}", getCurrentUsername());
        List<Loan> loans = loanRepository.findByUserUsername(getCurrentUsername());
        log.debug("Found {} loans in total", loans.size());

        return loans.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanDto> getActiveLoans() {
        log.info("Fetching all active loans for user: {}", getCurrentUsername());
        List<Loan> activeLoans = loanRepository.findByStatusAndUserUsername(LoanStatus.ACTIVE, getCurrentUsername());
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

    @Override
    public void repayLoan(Long id, java.math.BigDecimal amount) {
        log.info("Processing repayment of {} for loan ID {}", amount, id);
        Loan loan = getLoanEntityById(id);
        java.math.BigDecimal newAmount = loan.getAmount().subtract(amount);
        if (newAmount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            log.debug("Loan {} fully repaid. Deleting record.", id);
            loanRepository.delete(loan);
        } else {
            log.debug("Loan {} partially repaid. Remaining balance: {}", id, newAmount);
            loan.setAmount(newAmount);
            loanRepository.save(loan);
        }
    }

    // Internal helper to get entity
    private Loan getLoanEntityById(Long id) {
        String username = getCurrentUsername();
        return loanRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> {
                    log.error("Loan not found with ID {} for user {}", id, username);
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
                .currency(loan.getCurrency())
                .dateLent(loan.getDateLent())
                .dueDate(loan.getDueDate())
                .status(loan.getStatus())
                .build();
    }
}
