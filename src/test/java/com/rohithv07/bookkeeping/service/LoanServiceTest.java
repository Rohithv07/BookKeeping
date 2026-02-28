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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import com.rohithv07.bookkeeping.model.AppUser;
import com.rohithv07.bookkeeping.repository.AppUserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Loan sampleLoan;
    private Borrower sampleBorrower;
    private LoanDto sampleLoanDto;
    private AppUser sampleUser;

    @BeforeEach
    void setUp() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(authentication.getName()).thenReturn("testuser");
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        sampleUser = new AppUser(1L, "testuser", "encodedPass");
        sampleBorrower = Borrower.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .user(sampleUser)
                .build();

        sampleLoan = Loan.builder()
                .id(100L)
                .borrower(sampleBorrower)
                .amount(new BigDecimal("500.00"))
                .dateLent(LocalDate.now().minusDays(15))
                .dueDate(LocalDate.now().plusDays(15))
                .status(LoanStatus.ACTIVE)
                .user(sampleUser)
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
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(borrowerRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(sampleBorrower));
        when(loanRepository.save(any(Loan.class))).thenReturn(sampleLoan);

        LoanDto savedLoan = loanService.addLoan(sampleLoanDto);

        assertNotNull(savedLoan);
        assertEquals(100L, savedLoan.getId());
        assertEquals(new BigDecimal("500.00"), savedLoan.getAmount());
        assertEquals(1L, savedLoan.getBorrowerId());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(borrowerRepository, times(1)).findByIdAndUserUsername(1L, "testuser");
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void getAllLoans_ShouldReturnListOfLoans() {
        when(loanRepository.findByUserUsername("testuser")).thenReturn(List.of(sampleLoan));

        List<LoanDto> loans = loanService.getAllLoans();

        assertFalse(loans.isEmpty());
        assertEquals(1, loans.size());
        verify(loanRepository, times(1)).findByUserUsername("testuser");
    }

    @Test
    void getActiveLoans_ShouldReturnOnlyActiveLoans() {
        when(loanRepository.findByStatusAndUserUsername(LoanStatus.ACTIVE, "testuser")).thenReturn(List.of(sampleLoan));

        List<LoanDto> activeLoans = loanService.getActiveLoans();

        assertFalse(activeLoans.isEmpty());
        assertEquals(LoanStatus.ACTIVE, activeLoans.get(0).getStatus());
        verify(loanRepository, times(1)).findByStatusAndUserUsername(LoanStatus.ACTIVE, "testuser");
    }

    @Test
    void getLoanById_ExistingId_ShouldReturnLoan() {
        when(loanRepository.findByIdAndUserUsername(100L, "testuser")).thenReturn(Optional.of(sampleLoan));

        LoanDto foundLoan = loanService.getLoanById(100L);

        assertNotNull(foundLoan);
        assertEquals(100L, foundLoan.getId());
        verify(loanRepository, times(1)).findByIdAndUserUsername(100L, "testuser");
    }

    @Test
    void getLoanById_NonExistingId_ShouldThrowResourceNotFoundException() {
        when(loanRepository.findByIdAndUserUsername(999L, "testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> loanService.getLoanById(999L));
        verify(loanRepository, times(1)).findByIdAndUserUsername(999L, "testuser");
    }

    @Test
    void deleteLoan_ExistingActiveLoan_ShouldDelete() {
        when(loanRepository.findByIdAndUserUsername(100L, "testuser")).thenReturn(Optional.of(sampleLoan));

        loanService.deleteLoan(100L);

        verify(loanRepository, times(1)).findByIdAndUserUsername(100L, "testuser");
        verify(loanRepository, times(1)).delete(sampleLoan);
    }

    @Test
    void repayLoan_PartialAmount_ShouldUpdateLoan() {
        when(loanRepository.findByIdAndUserUsername(100L, "testuser")).thenReturn(Optional.of(sampleLoan));
        // Current amount is 500. Repaying 200 should leave 300.

        loanService.repayLoan(100L, new BigDecimal("200.00"));

        verify(loanRepository, times(1)).findByIdAndUserUsername(100L, "testuser");
        verify(loanRepository, times(1)).save(sampleLoan);
        assertEquals(new BigDecimal("300.00"), sampleLoan.getAmount());
    }

    @Test
    void repayLoan_EqualOrGreaterAmount_ShouldDeleteLoan() {
        when(loanRepository.findByIdAndUserUsername(100L, "testuser")).thenReturn(Optional.of(sampleLoan));
        // Current amount is 500. Repaying 600 should delete it.

        loanService.repayLoan(100L, new BigDecimal("600.00"));

        verify(loanRepository, times(1)).findByIdAndUserUsername(100L, "testuser");
        verify(loanRepository, times(1)).delete(sampleLoan);
        verify(loanRepository, never()).save(any(Loan.class));
    }
}
