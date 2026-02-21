package com.rohithv07.bookkeeping.controller;

import com.rohithv07.bookkeeping.dto.BorrowerDto;
import com.rohithv07.bookkeeping.service.BorrowerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/borrowers")
@Slf4j
public class BorrowerController {

    // Explicit constructor injection without Lombok magic
    private final BorrowerService borrowerService;

    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    @PostMapping
    public ResponseEntity<BorrowerDto> addBorrower(@Valid @RequestBody BorrowerDto borrowerDto) {
        log.info("REST request to add a new borrower");
        return ResponseEntity.ok(borrowerService.addBorrower(borrowerDto));
    }

    @GetMapping
    public ResponseEntity<List<BorrowerDto>> getAllBorrowers() {
        log.info("REST request to get all borrowers");
        return ResponseEntity.ok(borrowerService.getAllBorrowers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowerDto> getBorrowerById(@PathVariable Long id) {
        log.info("REST request to get borrower by ID: {}", id);
        return ResponseEntity.ok(borrowerService.getBorrowerById(id));
    }
}
