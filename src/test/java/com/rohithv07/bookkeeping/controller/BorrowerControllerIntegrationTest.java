package com.rohithv07.bookkeeping.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rohithv07.bookkeeping.dto.BorrowerDto;
import com.rohithv07.bookkeeping.model.Borrower;
import com.rohithv07.bookkeeping.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BorrowerControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private BorrowerRepository borrowerRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                borrowerRepository.deleteAll();
        }

        @Test
        void addBorrower_ShouldReturnCreatedBorrower() throws Exception {
                BorrowerDto borrowerDto = BorrowerDto.builder()
                                .name("Integration Test User")
                                .email("integratetest@example.com")
                                .phone("0987654321")
                                .build();

                mockMvc.perform(post("/api/borrowers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(borrowerDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.name").value("Integration Test User"))
                                .andExpect(jsonPath("$.email").value("integratetest@example.com"));
        }

        @Test
        void getAllBorrowers_ShouldReturnList() throws Exception {
                Borrower borrower = Borrower.builder()
                                .name("List User")
                                .email("listuser@example.com")
                                .build();
                borrowerRepository.save(borrower);

                mockMvc.perform(get("/api/borrowers"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].name").value("List User"));
        }

        @Test
        void getBorrowerById_ExistingId_ShouldReturnBorrower() throws Exception {
                Borrower borrower = Borrower.builder()
                                .name("Get By Id User")
                                .email("getiduser@example.com")
                                .build();
                Borrower saved = borrowerRepository.save(borrower);

                mockMvc.perform(get("/api/borrowers/" + saved.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(saved.getId()))
                                .andExpect(jsonPath("$.name").value("Get By Id User"));
        }

        @Test
        void getBorrowerById_NonExistingId_ShouldReturnNotFound() throws Exception {
                mockMvc.perform(get("/api/borrowers/99999"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("Borrower not found with ID: 99999"));
        }
}
