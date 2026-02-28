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

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import com.rohithv07.bookkeeping.model.AppUser;
import com.rohithv07.bookkeeping.repository.AppUserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private BorrowerServiceImpl borrowerService;

    private Borrower sampleBorrower;
    private BorrowerDto sampleBorrowerDto;
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
                .phone("1234567890")
                .user(sampleUser)
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
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(borrowerRepository.save(any(Borrower.class))).thenReturn(sampleBorrower);

        BorrowerDto savedBorrower = borrowerService.addBorrower(sampleBorrowerDto);

        assertNotNull(savedBorrower);
        assertEquals(1L, savedBorrower.getId());
        assertEquals("John Doe", savedBorrower.getName());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(borrowerRepository, times(1)).save(any(Borrower.class));
    }

    @Test
    void getAllBorrowers_ShouldReturnListOfBorrowers() {
        when(borrowerRepository.findByUserUsername("testuser")).thenReturn(List.of(sampleBorrower));

        List<BorrowerDto> borrowers = borrowerService.getAllBorrowers();

        assertFalse(borrowers.isEmpty());
        assertEquals(1, borrowers.size());
        verify(borrowerRepository, times(1)).findByUserUsername("testuser");
    }

    @Test
    void getBorrowerById_ExistingId_ShouldReturnBorrower() {
        when(borrowerRepository.findByIdAndUserUsername(1L, "testuser")).thenReturn(Optional.of(sampleBorrower));

        BorrowerDto foundBorrower = borrowerService.getBorrowerById(1L);

        assertNotNull(foundBorrower);
        assertEquals(1L, foundBorrower.getId());
        verify(borrowerRepository, times(1)).findByIdAndUserUsername(1L, "testuser");
    }

    @Test
    void getBorrowerById_NonExistingId_ShouldThrowResourceNotFoundException() {
        when(borrowerRepository.findByIdAndUserUsername(2L, "testuser")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> borrowerService.getBorrowerById(2L));
        verify(borrowerRepository, times(1)).findByIdAndUserUsername(2L, "testuser");
    }
}
