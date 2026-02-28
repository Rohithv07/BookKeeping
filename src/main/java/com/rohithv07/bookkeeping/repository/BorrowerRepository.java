package com.rohithv07.bookkeeping.repository;

import com.rohithv07.bookkeeping.model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    Optional<Borrower> findByEmailAndUserUsername(String email, String username);

    List<Borrower> findByUserUsername(String username);

    Optional<Borrower> findByIdAndUserUsername(Long id, String username);

    List<Borrower> findByUserIsNull();
}
