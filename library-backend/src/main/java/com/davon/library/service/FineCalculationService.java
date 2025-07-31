package com.davon.library.service;

import com.davon.library.model.Fine;
import com.davon.library.model.Loan;
import com.davon.library.model.Member;
import com.davon.library.model.Transaction;
import com.davon.library.repository.FineRepository;
import com.davon.library.repository.LoanRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class FineCalculationService {

    @Inject
    private FineRepository fineRepository;

    @Inject
    private LoanRepository loanRepository;
    
    @Inject
    private LoanService loanService;

    private static final BigDecimal DAILY_FINE_RATE = new BigDecimal("2.00");
    private static final BigDecimal MAX_FINE_AMOUNT = new BigDecimal("50.00");

    @Transactional
    public Fine calculateOverdueFine(Loan loan) {
        if (loan == null || loan.getDueDate() == null) {
            log.error("Loan or its due date cannot be null");
            return null;
        }

        Fine existingFine = fineRepository.findFirstByLoan(loan);
        if (existingFine != null) {
            log.info("Fine already exists for loan: {}", loan.getId());
            return existingFine;
        }

        LocalDate currentDate = LocalDate.now();
        LocalDate dueDate = new java.sql.Date(loan.getDueDate().getTime()).toLocalDate();

        if (!currentDate.isAfter(dueDate)) {
            log.info("Book is not overdue yet");
            return null;
        }

        long overdueDays = loanService.getDaysOverdue(loan.getId());
        if (overdueDays <= 0) {
            return null;
        }

        BigDecimal fineAmount = DAILY_FINE_RATE.multiply(new BigDecimal(overdueDays));
        
        if (fineAmount.compareTo(MAX_FINE_AMOUNT) > 0) {
            fineAmount = MAX_FINE_AMOUNT;
        }

        Fine fine = new Fine();
        fine.setAmount(fineAmount);
        fine.setType("FINE");
        fine.setDescription("Overdue fine for book: " + loan.getBook().getTitle());
        fine.setStatus(Transaction.TransactionStatus.PENDING);
        fine.setUser(loan.getMember());
        fine.setDate(new Date());
        fine.setLoan(loan);
        fine.setIssuedDate(new Date());
        fine.setFineType(Fine.FineType.OVERDUE);
        fine.setReason("Book returned " + overdueDays + " days late");
        fine.setPaid(false);
        
        fineRepository.persist(fine);
        log.info("Fine calculated: {} for {} days overdue", fineAmount, overdueDays);
        return fine;
    }

    public BigDecimal calculateTotalFinesForMember(Member member) {
        List<Fine> fines = fineRepository.findByUser(member);
        return fines.stream()
                .filter(fine -> !fine.isPaid() && fine.getAmount() != null)
                .map(Fine::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<Loan> getOverdueBooksForMember(Member member) {
        LocalDate currentDate = LocalDate.now();
        
        return loanRepository.findByMember(member).stream()
                .filter(loan -> loan.getStatus() == Loan.LoanStatus.ACTIVE)
                .filter(loan -> currentDate.isAfter(new java.sql.Date(loan.getDueDate().getTime()).toLocalDate()))
                .toList();
    }

    public boolean hasOutstandingFines(Member member) {
        BigDecimal totalFines = calculateTotalFinesForMember(member);
        return totalFines.compareTo(new BigDecimal("25.00")) > 0; 
    }
}