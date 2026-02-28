package com.rohithv07.bookkeeping.service;

import com.rohithv07.bookkeeping.dto.BorrowerDto;
import com.rohithv07.bookkeeping.exception.ResourceNotFoundException;
import com.rohithv07.bookkeeping.model.AppUser;
import com.rohithv07.bookkeeping.model.Borrower;
import com.rohithv07.bookkeeping.repository.AppUserRepository;
import com.rohithv07.bookkeeping.repository.BorrowerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BorrowerServiceImpl implements BorrowerService {

    // Explicit constructor injection without Lombok magic
    private final BorrowerRepository borrowerRepository;
    private final AppUserRepository userRepository;

    public BorrowerServiceImpl(BorrowerRepository borrowerRepository, AppUserRepository userRepository) {
        this.borrowerRepository = borrowerRepository;
        this.userRepository = userRepository;
    }

    private String getCurrentUsername() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getName();
    }

    @Override
    public BorrowerDto addBorrower(BorrowerDto borrowerDto) {
        log.info("Adding new borrower: {}", borrowerDto.getEmail());

        String username = getCurrentUsername();
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Borrower borrower = Borrower.builder()
                .name(borrowerDto.getName())
                .email(borrowerDto.getEmail())
                .phone(borrowerDto.getPhone())
                .user(user)
                .build();

        Borrower savedBorrower = borrowerRepository.save(borrower);
        log.debug("Saved borrower with ID: {}", savedBorrower.getId());

        return mapToDto(savedBorrower);
    }

    @Override
    public List<BorrowerDto> getAllBorrowers() {
        log.info("Fetching all borrowers for user: {}", getCurrentUsername());
        List<Borrower> borrowers = borrowerRepository.findByUserUsername(getCurrentUsername());
        log.debug("Found {} borrowers", borrowers.size());

        return borrowers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public BorrowerDto getBorrowerById(Long id) {
        String username = getCurrentUsername();
        log.info("Fetching borrower with ID: {} for user: {}", id, username);
        Borrower borrower = borrowerRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> {
                    log.error("Borrower not found with ID {} for user {}", id, username);
                    return new ResourceNotFoundException("Borrower not found with ID: " + id);
                });

        return mapToDto(borrower);
    }

    // Helper method to map Entity to DTO
    private BorrowerDto mapToDto(Borrower borrower) {
        return BorrowerDto.builder()
                .id(borrower.getId())
                .name(borrower.getName())
                .email(borrower.getEmail())
                .phone(borrower.getPhone())
                .build();
    }
}
