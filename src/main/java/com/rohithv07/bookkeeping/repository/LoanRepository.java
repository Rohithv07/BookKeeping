package com.rohithv07.bookkeeping.repository;

import com.rohithv07.bookkeeping.model.Loan;
import com.rohithv07.bookkeeping.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStatusAndUserUsername(LoanStatus status, String username);

    List<Loan> findByUserUsername(String username);

    Optional<Loan> findByIdAndUserUsername(Long id, String username);
}
