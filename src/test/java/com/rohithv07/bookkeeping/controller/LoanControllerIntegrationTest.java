package com.rohithv07.bookkeeping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohithv07.bookkeeping.dto.LoanDto;
import com.rohithv07.bookkeeping.model.AppUser;
import com.rohithv07.bookkeeping.model.Borrower;
import com.rohithv07.bookkeeping.model.Loan;
import com.rohithv07.bookkeeping.model.LoanStatus;
import com.rohithv07.bookkeeping.repository.AppUserRepository;
import com.rohithv07.bookkeeping.repository.BorrowerRepository;
import com.rohithv07.bookkeeping.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin")
class LoanControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private LoanRepository loanRepository;

        @Autowired
        private BorrowerRepository borrowerRepository;

        @Autowired
        private AppUserRepository userRepository;

        @Autowired
        private ObjectMapper objectMapper;

        private Borrower savedBorrower;
        private AppUser adminUser;

        @BeforeEach
        void setUp() {
                loanRepository.deleteAll();
                borrowerRepository.deleteAll();
                userRepository.deleteAll();

                adminUser = AppUser.builder().username("admin").password("pass").build();
                adminUser = userRepository.save(adminUser);

                Borrower borrower = Borrower.builder()
                                .name("Integration Loan User")
                                .email("loanuser@example.com")
                                .user(adminUser)
                                .build();
                savedBorrower = borrowerRepository.save(borrower);
        }

        @Test
        void addLoan_ShouldReturnCreatedLoan() throws Exception {
                LoanDto loanDto = LoanDto.builder()
                                .borrowerId(savedBorrower.getId())
                                .amount(new BigDecimal("1500.50"))
                                .dateLent(LocalDate.now())
                                .build();

                mockMvc.perform(post("/api/loans").with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loanDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.amount").value(1500.5));
        }

        @Test
        void getAllActiveLoans_ShouldReturnList() throws Exception {
                Loan loan = Loan.builder()
                                .borrower(savedBorrower)
                                .amount(new BigDecimal("100.00"))
                                .dateLent(LocalDate.now())
                                .dueDate(LocalDate.now().plusMonths(1))
                                .status(LoanStatus.ACTIVE)
                                .user(adminUser)
                                .build();
                loanRepository.save(loan);

                mockMvc.perform(get("/api/loans"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
        }

        @Test
        void deleteLoan_ShouldReturnNoContent() throws Exception {
                Loan loan = Loan.builder()
                                .borrower(savedBorrower)
                                .amount(new BigDecimal("50.00"))
                                .dateLent(LocalDate.now())
                                .dueDate(LocalDate.now().plusMonths(1))
                                .status(LoanStatus.ACTIVE)
                                .user(adminUser)
                                .build();
                Loan savedLoan = loanRepository.save(loan);

                mockMvc.perform(delete("/api/loans/" + savedLoan.getId()).with(csrf()))
                                .andExpect(status().isNoContent());
        }

        @Test
        void repayLoan_PartialAmount_ShouldReturnNoContent() throws Exception {
                Loan loan = Loan.builder()
                                .borrower(savedBorrower)
                                .amount(new BigDecimal("500.00"))
                                .dateLent(LocalDate.now())
                                .dueDate(LocalDate.now().plusMonths(1))
                                .status(LoanStatus.ACTIVE)
                                .user(adminUser)
                                .build();
                Loan savedLoan = loanRepository.save(loan);

                com.rohithv07.bookkeeping.dto.RepaymentRequest request = com.rohithv07.bookkeeping.dto.RepaymentRequest
                                .builder()
                                .amount(new BigDecimal("200.00"))
                                .build();

                mockMvc.perform(put("/api/loans/" + savedLoan.getId() + "/repay").with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNoContent());
        }
}
