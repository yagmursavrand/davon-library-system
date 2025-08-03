'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import dashboardService, { DashboardStats, UrgentLoan } from '../services/dashboardService';
import fineService, { Fine } from '../services/fineService'; // Import fine service
import styles from './UserDashboard.module.css';

const UserDashboard: React.FC = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // New state for the fines modal
  const [isFinesModalOpen, setIsFinesModalOpen] = useState<boolean>(false);
  const [fines, setFines] = useState<Fine[]>([]);
  const [loadingFines, setLoadingFines] = useState<boolean>(false);

  useEffect(() => {
    const fetchDashboardData = async () => {
      if (user) {
        try {
          setLoading(true);
          const data = await dashboardService.getDashboardData(user.id);
          setStats(data);
          setError(null);
        } catch (err) {
          setError('Failed to load dashboard data.');
          console.error(err);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchDashboardData();
  }, [user]);

  const handleOpenFinesModal = async () => {
    if (!user) return;
    
    setIsFinesModalOpen(true);
    setLoadingFines(true);
    try {
      const fetchedFines = await fineService.getUnpaidFines(user.id);
      setFines(fetchedFines);
    } catch (err) {
      console.error("Failed to fetch fines:", err);
      // Optionally set an error state for the modal
    } finally {
      setLoadingFines(false);
    }
  };

  const handlePayFines = async () => {
    if (!user) return;

    try {
      await fineService.payAllFines(user.id);
      alert('Fines paid successfully!');
      setIsFinesModalOpen(false);
      // Manually update stats on the frontend for immediate feedback, then refetch
      if (stats) {
        setStats({ ...stats, outstandingFines: '0.00' });
      }
      // Refetch all dashboard data to ensure consistency
      const data = await dashboardService.getDashboardData(user.id);
      setStats(data);
    } catch (err: any) {
      alert(`Payment failed: ${err.message}`);
      console.error(err);
    }
  };

  const renderUrgentLoan = (loan: UrgentLoan) => {
    let statusText = '';
    let statusStyle = '';

    if (loan.status === 'OVERDUE') {
      statusText = `Overdue by ${Math.abs(loan.daysUntilDue)} day(s)`;
      statusStyle = styles.urgentOverdue;
    } else {
        const days = loan.daysUntilDue;
        if (days === 0) {
            statusText = 'Due today';
            statusStyle = styles.urgentDueSoon;
        } else {
            statusText = `Due in ${days} day(s)`;
            statusStyle = styles.urgentDueSoon;
        }
    }

    return (
        <div key={loan.bookTitle} className={styles.urgentLoanItem}>
            <p className={styles.urgentBookTitle}>{loan.bookTitle}</p>
            <p className={`${styles.urgentStatus} ${statusStyle}`}>{statusText}</p>
        </div>
    );
  };

  if (loading) {
    return <p className={styles.loading}>Loading dashboard...</p>;
  }

  if (error) {
    return <p className={styles.error}>{error}</p>;
  }

  if (!stats) {
    return <p className={styles.loading}>No data available.</p>;
  }

  return (
    <div className={styles.dashboardContainer}>
      <h2 className={styles.welcomeMessage}>
        Welcome back, <span>{user?.name}!</span>
      </h2>
      <div className={styles.statsGrid}>
        <div className={styles.statCard}>
          <p className={styles.statNumber}>{stats.activeLoansCount}</p>
          <p className={styles.statLabel}>Books on Loan</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statNumber}>{stats.overdueLoansCount}</p>
          <p className={styles.statLabel}>Overdue Books</p>
        </div>
        <div 
          className={`${styles.statCard} ${styles.clickable}`}
          onClick={handleOpenFinesModal}
          role="button"
          tabIndex={0}
        >
          <p className={styles.statNumber}>${parseFloat(stats.outstandingFines).toFixed(2)}</p>
          <p className={styles.statLabel}>Outstanding Fines</p>
        </div>
      </div>

      {stats.urgentLoans && stats.urgentLoans.length > 0 && (
          <div className={styles.urgentLoansContainer}>
              <h3 className={styles.urgentLoansTitle}>Up Next</h3>
              <div className={styles.urgentLoansList}>
                  {stats.urgentLoans.map(renderUrgentLoan)}
              </div>
          </div>
      )}

      {isFinesModalOpen && (
        <div className={styles.modalOverlay}>
            <div className={styles.modalContent}>
                <button className={styles.closeButton} onClick={() => setIsFinesModalOpen(false)}>X</button>
                <h2 className={styles.modalTitle}>My Outstanding Fines</h2>
                {loadingFines ? (
                    <p>Loading fines...</p>
                ) : (
                    <>
                        <div className={styles.finesList}>
                            {fines.length === 0 ? (
                                <p>You have no outstanding fines.</p>
                            ) : (
                                fines.map(fine => (
                                    <div key={fine.id} className={styles.fineItem}>
                                        <div className={styles.fineDetails}>
                                            <p className={styles.bookTitle}>{fine.bookTitle}</p>
                                            <p className={styles.fineReason}>{fine.reason}</p>
                                        </div>
                                        <p className={styles.fineAmount}>${parseFloat(fine.amount).toFixed(2)}</p>
                                    </div>
                                ))
                            )}
                        </div>
                        {fines.length > 0 && (
                            <div className={styles.summaryContainer}>
                                <div className={styles.totalRow}>
                                    <span className={styles.totalLabel}>Total Due</span>
                                    <span className={styles.totalAmount}>${parseFloat(stats.outstandingFines).toFixed(2)}</span>
                                </div>
                                <button onClick={handlePayFines} className={styles.payButton}>
                                    Pay Now
                                </button>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
      )}
    </div>
  );
};

export default UserDashboard;
