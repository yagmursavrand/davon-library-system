'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import loanService, { Loan } from '../services/loanService';
import styles from './LoanManagement.module.css';

const LoanManagement: React.FC = () => {
    const { user } = useAuth();
  const [loans, setLoans] = useState<Loan[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchLoans = async () => {
      if (user) {
        try {
          setLoading(true);
          // TODO: This assumes user.id is the memberId. This might need adjustment
          // if there's a separate member profile.
          const memberId = user.id;
          const userLoans = await loanService.getLoansByMember(memberId);
          setLoans(userLoans);
          setError(null);
        } catch (err) {
          setError('Failed to load loans. Please try again later.');
          console.error(err);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchLoans();
  }, [user]);

  const handleReturnBook = async (loanId: number) => {
    try {
        await loanService.returnBook(loanId);
        alert('Book returned successfully!');
        // Refresh the loans list to show the updated status
        if (user) {
            const userLoans = await loanService.getLoansByMember(user.id);
            setLoans(userLoans);
        }
    } catch (err: any) {
        alert(`Failed to return book. Reason: ${err.message}`);
        console.error(err);
    }
  };

  if (loading) {
    return <p className={styles.loading}>Loading your loans...</p>;
  }

  if (error) {
    return <p className={styles.error}>{error}</p>;
  }
  
  const getStatusClass = (status: string) => {
    switch (status) {
      case 'ACTIVE':
      case 'RENEWED':
        return styles.statusActive;
      case 'OVERDUE':
        return styles.statusOverdue;
      case 'RETURNED':
        return styles.statusReturned;
      default:
        return '';
    }
  };

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>My Loans</h2>
      {loans.length === 0 ? (
        <p>You have no active or past loans.</p>
      ) : (
        <div className={styles.loanGrid}>
          {loans.map((loan) => (
            <div key={loan.id} className={styles.loanCard}>
              <h3 className={styles.bookTitle}>{loan.bookTitle}</h3>
              <p className={styles.loanInfo}>
                Loan Date: {new Date(loan.loanDate).toLocaleDateString()}
              </p>
              <p className={styles.loanInfo}>
                Due Date: {new Date(loan.dueDate).toLocaleDateString()}
              </p>
              {loan.returnDate && (
                <p className={styles.loanInfo}>
                  Returned On: {new Date(loan.returnDate).toLocaleDateString()}
                </p>
              )}
              <p className={styles.loanInfo}>
                Fine: ${parseFloat(loan.fineAmount || '0').toFixed(2)}
              </p>
                            <div className={styles.statusContainer}>
                <span className={`${styles.status} ${getStatusClass(loan.status)}`}>
                  {loan.status}
                </span>
                {(loan.status === 'ACTIVE' || loan.status === 'RENEWED' || loan.status === 'OVERDUE') && (
                    <button onClick={() => handleReturnBook(loan.id)} className={styles.returnButton}>
                        Return Book
                    </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default LoanManagement;
