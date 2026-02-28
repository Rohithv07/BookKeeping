package com.rohithv07.bookkeeping.config;

import com.rohithv07.bookkeeping.model.AppUser;
import com.rohithv07.bookkeeping.model.Borrower;
import com.rohithv07.bookkeeping.model.Loan;
import com.rohithv07.bookkeeping.repository.AppUserRepository;
import com.rohithv07.bookkeeping.repository.BorrowerRepository;
import com.rohithv07.bookkeeping.repository.LoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class DataMigrationRunner implements CommandLineRunner {

    private final BorrowerRepository borrowerRepository;
    private final LoanRepository loanRepository;
    private final AppUserRepository userRepository;

    public DataMigrationRunner(BorrowerRepository borrowerRepository, LoanRepository loanRepository,
            AppUserRepository userRepository) {
        this.borrowerRepository = borrowerRepository;
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting DataMigrationRunner to check for orphaned records...");

        List<Borrower> orphanedBorrowers = borrowerRepository.findByUserIsNull();
        List<Loan> orphanedLoans = loanRepository.findByUserIsNull();

        if (orphanedBorrowers.isEmpty() && orphanedLoans.isEmpty()) {
            log.info("No orphaned records found. Data migration skipped.");
            return;
        }

        List<AppUser> allUsers = userRepository.findAll();
        if (allUsers.isEmpty()) {
            log.warn(
                    "Found orphaned records, but no AppUser exists in the database to assign them to. Skipping migration.");
            return;
        }

        // Just take the first registered user to assume ownership of all legacy data
        AppUser legacyOwner = allUsers.get(0);
        log.info("Assigning {} orphaned borrowers and {} orphaned loans to default user: {}",
                orphanedBorrowers.size(), orphanedLoans.size(), legacyOwner.getUsername());

        for (Borrower b : orphanedBorrowers) {
            b.setUser(legacyOwner);
            borrowerRepository.save(b);
        }

        for (Loan l : orphanedLoans) {
            l.setUser(legacyOwner);
            loanRepository.save(l);
        }

        log.info("Legacy Data Migration completed successfully.");
    }
}
