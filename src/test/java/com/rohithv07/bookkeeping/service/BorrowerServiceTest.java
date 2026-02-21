package com.rohithv07.bookkeeping.service;

import com.rohithv07.bookkeeping.dto.BorrowerDto;
import com.rohithv07.bookkeeping.exception.ResourceNotFoundException;
import com.rohithv07.bookkeeping.model.Borrower;
import com.rohithv07.bookkeeping.repository.BorrowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @InjectMocks
    private BorrowerServiceImpl borrowerService;

    private Borrower sampleBorrower;
    private BorrowerDto sampleBorrowerDto;

    @BeforeEach
    void setUp() {
        sampleBorrower = Borrower.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();

        sampleBorrowerDto = BorrowerDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .build();
    }

    @Test
    void addBorrower_ShouldReturnSavedBorrower() {
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(sampleBorrower);

        BorrowerDto savedBorrower = borrowerService.addBorrower(sampleBorrowerDto);

        assertNotNull(savedBorrower);
        assertEquals(1L, savedBorrower.getId());
        assertEquals("John Doe", savedBorrower.getName());
        verify(borrowerRepository, times(1)).save(any(Borrower.class));
    }

    @Test
    void getAllBorrowers_ShouldReturnListOfBorrowers() {
        when(borrowerRepository.findAll()).thenReturn(List.of(sampleBorrower));

        List<BorrowerDto> borrowers = borrowerService.getAllBorrowers();

        assertFalse(borrowers.isEmpty());
        assertEquals(1, borrowers.size());
        verify(borrowerRepository, times(1)).findAll();
    }

    @Test
    void getBorrowerById_ExistingId_ShouldReturnBorrower() {
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(sampleBorrower));

        BorrowerDto foundBorrower = borrowerService.getBorrowerById(1L);

        assertNotNull(foundBorrower);
        assertEquals(1L, foundBorrower.getId());
        verify(borrowerRepository, times(1)).findById(1L);
    }

    @Test
    void getBorrowerById_NonExistingId_ShouldThrowResourceNotFoundException() {
        when(borrowerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> borrowerService.getBorrowerById(2L));
        verify(borrowerRepository, times(1)).findById(2L);
    }
}
