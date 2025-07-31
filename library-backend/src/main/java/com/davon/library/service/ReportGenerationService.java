package com.davon.library.service;

import com.davon.library.model.*;
import com.davon.library.repository.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class ReportGenerationService {

    @Inject
    private BookRepository bookRepository;

    @Inject
    private MemberRepository memberRepository;

    @Inject
    private TransactionRepository transactionRepository;

    @Inject
    private ReservationRepository reservationRepository;
    
    @Inject
    private FineCalculationService fineCalculationService;
    
    @Inject
    private LoanRepository loanRepository;

    public Map<String, Object> generateOverdueBooksReport() {
        Date reportDate = new Date();

        List<Book> allBooks = bookRepository.findAll().list();
        List<Member> allMembers = memberRepository.findAll().list();

        Map<String, Object> report = new HashMap<>();
        report.put("reportDate", reportDate);
        report.put("totalBooks", allBooks.size());
        report.put("totalMembers", allMembers.size());

        List<Book> overdueBooks = allBooks.stream()
                .filter(book -> book.getStatus() == Book.BookStatus.BORROWED)
                .toList();
        report.put("overdueBooks", overdueBooks.size());

        BigDecimal totalExistingFines = allMembers.stream()
                .map(fineCalculationService::calculateTotalFinesForMember)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        report.put("totalFines", totalExistingFines);

        return report;
    }

    public Map<String, Object> generatePopularBooksReport() {
        List<Book> allBooks = bookRepository.findAll().list();
        
        List<Map<String, Object>> popularBooks = allBooks.stream()
                .map(book -> {
                    Map<String, Object> bookData = new HashMap<>();
                    bookData.put("book", book);
                    long popularity = loanRepository.findByBook(book).size();
                    bookData.put("popularity", popularity);
                    return bookData;
                })
                .sorted(Comparator.<Map<String, Object>>comparingLong(m -> (Long) m.get("popularity")).reversed())
                .limit(10)
                .toList();

        Map<String, Object> report = new HashMap<>();
        report.put("popularBooks", popularBooks);
        report.put("totalBooks", allBooks.size());

        log.info("Popular books report generated: {} books analyzed", allBooks.size());
        return report;
    }

    public Map<String, Object> generateMemberActivityReport() {
        List<Member> allMembers = memberRepository.findAll().list();
        
        List<Map<String, Object>> memberActivity = allMembers.stream()
                .map(member -> {
                    Map<String, Object> memberData = new HashMap<>();
                    memberData.put("member", member);
                    
                    List<Loan> activeLoans = loanRepository.findByMember(member).stream()
                            .filter(loan -> loan.getStatus() == Loan.LoanStatus.ACTIVE)
                            .toList();
                    memberData.put("totalLoans", activeLoans.size());
                    
                    List<Loan> overdueLoans = fineCalculationService.getOverdueBooksForMember(member);
                    BigDecimal existingFines = fineCalculationService.calculateTotalFinesForMember(member);
                    memberData.put("totalFines", existingFines);
                    memberData.put("overdueBooks", overdueLoans.size());
                    
                    return memberData;
                })
                .sorted(Comparator.<Map<String, Object>>comparingInt(m -> (Integer) m.get("totalLoans")).reversed())
                .toList();

        Map<String, Object> report = new HashMap<>();
        report.put("memberActivity", memberActivity);
        report.put("totalMembers", allMembers.size());

        log.info("Member activity report generated: {} members analyzed", allMembers.size());
        return report;
    }

    public Map<String, Object> generateFinancialReport() {
        List<Transaction> allTransactions = transactionRepository.findAll().list();
        
        BigDecimal totalRevenue = allTransactions.stream()
                .filter(t -> "MEMBERSHIP_FEE".equals(t.getType()) && t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalFines = allTransactions.stream()
                .filter(t -> "FINE".equals(t.getType()) && t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpenses = allTransactions.stream()
                .filter(t -> "EXPENSE".equals(t.getType()) && t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        Map<String, Object> report = new HashMap<>();
        report.put("totalRevenue", totalRevenue);
        report.put("totalFines", totalFines);
        report.put("totalExpenses", totalExpenses);
        report.put("netProfit", netProfit);
        report.put("transactionCount", allTransactions.size());

        log.info("Financial report generated: Revenue: {}, Profit: {}", totalRevenue, netProfit);
        return report;
    }

    public Map<String, Object> generateInventoryReport() {
        List<Book> allBooks = bookRepository.findAll().list();
        
        List<Loan> activeLoans = loanRepository.findByStatus(Loan.LoanStatus.ACTIVE);
        long borrowedBooks = activeLoans.size();
        long availableBooks = allBooks.size() - borrowedBooks;
        
        Map<String, Long> booksByCategory = allBooks.stream()
                .collect(Collectors.groupingBy(
                        Book::getGenre,
                        Collectors.counting()
                ));

        Map<String, Object> report = new HashMap<>();
        report.put("totalBooks", allBooks.size());
        report.put("availableBooks", availableBooks);
        report.put("borrowedBooks", borrowedBooks);
        report.put("booksByCategory", booksByCategory);

        log.info("Inventory report generated: {} total books, {} available", allBooks.size(), availableBooks);
        return report;
    }

    public Map<String, Object> generateReservationReport() {
        List<Reservation> allReservations = reservationRepository.findAll().list();
        
        long pendingReservations = allReservations.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.PENDING)
                .count();
        long activeReservations = allReservations.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.ACTIVE)
                .count();
        long fulfilledReservations = allReservations.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.FULFILLED)
                .count();

        Map<String, Object> report = new HashMap<>();
        report.put("totalReservations", allReservations.size());
        report.put("pendingReservations", pendingReservations);
        report.put("activeReservations", activeReservations);
        report.put("fulfilledReservations", fulfilledReservations);

        log.info("Reservation report generated: {} total reservations", allReservations.size());
        return report;
    }

    public Map<String, Object> generateComprehensiveReport() {
        Map<String, Object> report = new HashMap<>();
        
        List<Book> allBooks = bookRepository.findAll().list();
        List<Member> allMembers = memberRepository.findAll().list();
        List<Loan> activeLoans = loanRepository.findByStatus(Loan.LoanStatus.ACTIVE);
        List<Transaction> allTransactions = transactionRepository.findAll().list();
        
        report.put("totalBooks", allBooks.size());
        report.put("totalMembers", allMembers.size());
        report.put("activeLoans", activeLoans.size());
        
        Date currentDate = new Date();
        long overdueBooks = activeLoans.stream()
                .filter(loan -> loan.getDueDate().before(currentDate))
                .count();
        report.put("overdueBooks", overdueBooks);
        
        BigDecimal totalFines = allTransactions.stream()
                .filter(t -> "FINE".equals(t.getType()) && t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalFines", totalFines);
        
        BigDecimal monthlyRevenue = allTransactions.stream()
                .filter(t -> "MEMBERSHIP_FEE".equals(t.getType()) && t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("monthlyRevenue", monthlyRevenue);
        
        report.put("booksAddedThisMonth", 0);
        report.put("newMembersThisMonth", 0);
        report.put("loansThisMonth", activeLoans.size());

        log.info("Comprehensive library report generated");
        return report;
    }

    public String exportReport(Map<String, Object> report, String format) {
        if ("json".equalsIgnoreCase(format)) {
            return "{\"report\": " + report.toString() + "}";
        } else if ("csv".equalsIgnoreCase(format)) {
            StringBuilder csv = new StringBuilder();
            csv.append("Key,Value\n");
            report.forEach((key, value) -> csv.append(key).append(",").append(value).append("\n"));
            return csv.toString();
        } else if ("pdf".equalsIgnoreCase(format)) {
            return "PDF content for: " + report.toString();
        } else {
            return "Unsupported format: " + format;
        }
    }
}