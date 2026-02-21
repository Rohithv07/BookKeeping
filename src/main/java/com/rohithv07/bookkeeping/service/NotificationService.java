package com.rohithv07.bookkeeping.service;

import com.rohithv07.bookkeeping.model.Loan;
import com.rohithv07.bookkeeping.model.LoanStatus;
import com.rohithv07.bookkeeping.repository.LoanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class NotificationService {

    private final LoanRepository loanRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String adminEmail;

    // Explicit constructor injection without Lombok magic
    public NotificationService(LoanRepository loanRepository, JavaMailSender mailSender) {
        this.loanRepository = loanRepository;
        this.mailSender = mailSender;
    }

    // Run every day at 8 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendReminders() {
        log.info("Checking for due loans to send reminders...");
        LocalDate today = LocalDate.now();
        List<Loan> dueLoans = loanRepository.findByDueDateLessThanEqualAndStatusAndReminderSentFalse(today,
                LoanStatus.ACTIVE);

        for (Loan loan : dueLoans) {
            try {
                sendEmail(loan);
                loan.setReminderSent(true);
                loanRepository.save(loan);
                log.info("Reminder sent for loan ID {}", loan.getId());
            } catch (Exception e) {
                log.error("Failed to send reminder for loan ID {}", loan.getId(), e);
            }
        }
    }

    private void sendEmail(Loan loan) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(adminEmail);
        message.setSubject("Bookkeeping Reminder: Money Due!");
        message.setText("Reminder: The loan of $" + loan.getAmount() + " provided to "
                + loan.getBorrower().getName() + " on " + loan.getDateLent()
                + " has reached the 1-month mark (Due date: " + loan.getDueDate()
                + ").\n\nIt's time to ask them for the money back!");

        mailSender.send(message);
    }

    // For testing purposes, allows us to trigger the task immediately
    public void triggerManualReminder() {
        sendReminders();
    }
}
