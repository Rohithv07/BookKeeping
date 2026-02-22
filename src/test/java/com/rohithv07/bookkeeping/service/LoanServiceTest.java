package com.rohithv07.bookkeeping.service;

import com.rohithv07.bookkeeping.dto.LoanDto;
import com.rohithv07.bookkeeping.exception.ResourceNotFoundException;
import com.rohithv07.bookkeeping.model.Borrower;
import com.rohithv07.bookkeeping.model.Loan;
import com.rohithv07.bookkeeping.model.LoanStatus;
import com.rohithv07.bookkeeping.repository.BorrowerRepository;
import com.rohithv07.bookkeeping.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Loan sampleLoan;
    private Borrower sampleBorrower;
    private LoanDto sampleLoanDto;

    @BeforeEach
    void setUp() {
        sampleBorrower = Borrower.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        sampleLoan = Loan.builder()
                .id(100L)
                .borrower(sampleBorrower)
                .amount(new BigDecimal("500.00"))
                .dateLent(LocalDate.now().minusDays(15))
                .dueDate(LocalDate.now().plusDays(15))
                .status(LoanStatus.ACTIVE)
                .build();

        sampleLoanDto = LoanDto.builder()
                .id(100L)
                .borrowerId(1L)
                .amount(new BigDecimal("500.00"))
                .dateLent(LocalDate.now().minusDays(15))
                .build();
    }

    @Test
    void addLoan_ShouldReturnSavedLoan() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(sampleBorrower));
        when(loanRepository.save(any(Loan.class))).thenReturn(sampleLoan);

        LoanDto savedLoan = loanService.addLoan(sampleLoanDto);

        assertNotNull(savedLoan);
        assertEquals(100L, savedLoan.getId());
        assertEquals(new BigDecimal("500.00"), savedLoan.getAmount());
        assertEquals(1L, savedLoan.getBorrowerId());
        verify(borrowerRepository, times(1)).findById(1L);
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void getAllLoans_ShouldReturnListOfLoans() {
        when(loanRepository.findAll()).thenReturn(List.of(sampleLoan));

        List<LoanDto> loans = loanService.getAllLoans();

        assertFalse(loans.isEmpty());
        assertEquals(1, loans.size());
        verify(loanRepository, times(1)).findAll();
    }

    @Test
    void getActiveLoans_ShouldReturnOnlyActiveLoans() {
        when(loanRepository.findByStatus(LoanStatus.ACTIVE)).thenReturn(List.of(sampleLoan));

        List<LoanDto> activeLoans = loanService.getActiveLoans();

        assertFalse(activeLoans.isEmpty());
        assertEquals(LoanStatus.ACTIVE, activeLoans.get(0).getStatus());
        verify(loanRepository, times(1)).findByStatus(LoanStatus.ACTIVE);
    }

    @Test
    void getLoanById_ExistingId_ShouldReturnLoan() {
        when(loanRepository.findById(100L)).thenReturn(Optional.of(sampleLoan));

        LoanDto foundLoan = loanService.getLoanById(100L);

        assertNotNull(foundLoan);
        assertEquals(100L, foundLoan.getId());
        verify(loanRepository, times(1)).findById(100L);
    }

    @Test
    void getLoanById_NonExistingId_ShouldThrowResourceNotFoundException() {
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loanService.getLoanById(999L));
        verify(loanRepository, times(1)).findById(999L);
    }

    @Test
    void deleteLoan_ExistingActiveLoan_ShouldDelete() {
        when(loanRepository.findById(100L)).thenReturn(Optional.of(sampleLoan));

        loanService.deleteLoan(100L);

        verify(loanRepository, times(1)).findById(100L);
        verify(loanRepository, times(1)).delete(sampleLoan);
    }

    @Test
    void repayLoan_PartialAmount_ShouldUpdateLoan() {
        when(loanRepository.findById(100L)).thenReturn(Optional.of(sampleLoan));
        // Current amount is 500. Repaying 200 should leave 300.

        loanService.repayLoan(100L, new BigDecimal("200.00"));

        verify(loanRepository, times(1)).findById(100L);
        verify(loanRepository, times(1)).save(sampleLoan);
        assertEquals(new BigDecimal("300.00"), sampleLoan.getAmount());
    }

    @Test
    void repayLoan_EqualOrGreaterAmount_ShouldDeleteLoan() {
        when(loanRepository.findById(100L)).thenReturn(Optional.of(sampleLoan));
        // Current amount is 500. Repaying 600 should delete it.

        loanService.repayLoan(100L, new BigDecimal("600.00"));

        verify(loanRepository, times(1)).findById(100L);
        verify(loanRepository, times(1)).delete(sampleLoan);
        verify(loanRepository, never()).save(any(Loan.class));
    }
}
