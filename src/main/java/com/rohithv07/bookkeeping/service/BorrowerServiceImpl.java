package com.rohithv07.bookkeeping.service;

import com.rohithv07.bookkeeping.dto.BorrowerDto;
import com.rohithv07.bookkeeping.exception.ResourceNotFoundException;
import com.rohithv07.bookkeeping.model.Borrower;
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

    public BorrowerServiceImpl(BorrowerRepository borrowerRepository) {
        this.borrowerRepository = borrowerRepository;
    }

    @Override
    public BorrowerDto addBorrower(BorrowerDto borrowerDto) {
        log.info("Adding new borrower: {}", borrowerDto.getEmail());

        Borrower borrower = Borrower.builder()
                .name(borrowerDto.getName())
                .email(borrowerDto.getEmail())
                .phone(borrowerDto.getPhone())
                .build();

        Borrower savedBorrower = borrowerRepository.save(borrower);
        log.debug("Saved borrower with ID: {}", savedBorrower.getId());

        return mapToDto(savedBorrower);
    }

    @Override
    public List<BorrowerDto> getAllBorrowers() {
        log.info("Fetching all borrowers");
        List<Borrower> borrowers = borrowerRepository.findAll();
        log.debug("Found {} borrowers", borrowers.size());

        return borrowers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public BorrowerDto getBorrowerById(Long id) {
        log.info("Fetching borrower with ID: {}", id);
        Borrower borrower = borrowerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Borrower not found with ID: {}", id);
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
