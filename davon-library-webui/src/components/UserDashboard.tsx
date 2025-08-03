'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import loanService, { Loan, LoanStatus } from '../services/loanService';
import styles from './UserDashboard.module.css';

const UserDashboard: React.FC = () => {
  const { user, token } = useAuth();
  const [stats, setStats] = useState({
    activeLoans: 0,
    overdueLoans: 0,
    totalFines: 0,
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      if (user) {
        try {
          setLoading(true);
          const memberId = user.id;
          const loans = await loanService.getLoansByMember(memberId);

          const activeLoans = loans.filter(
            (loan) => loan.status === LoanStatus.ACTIVE || loan.status === LoanStatus.RENEWED
          ).length;

          const overdueLoans = loans.filter(
            (loan) => loan.status === LoanStatus.OVERDUE
          ).length;
          
                    const totalFines = loans.reduce((acc, loan) => acc + parseFloat(loan.fineAmount || '0'), 0);

          setStats({ activeLoans, overdueLoans, totalFines });
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

  if (loading) {
    return <p className={styles.loading}>Loading dashboard...</p>;
  }

  if (error) {
    return <p className={styles.error}>{error}</p>;
  }

  return (
    <div className={styles.dashboardContainer}>
      <h2 className={styles.welcomeMessage}>
        Welcome back, <span>{user?.name}!</span>
      </h2>
      <div className={styles.statsGrid}>
        <div className={styles.statCard}>
          <p className={styles.statNumber}>{stats.activeLoans}</p>
          <p className={styles.statLabel}>Books on Loan</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statNumber}>{stats.overdueLoans}</p>
          <p className={styles.statLabel}>Overdue Books</p>
        </div>
        <div className={styles.statCard}>
          <p className={styles.statNumber}>${parseFloat(stats.totalFines.toString()).toFixed(2)}</p>
          <p className={styles.statLabel}>Total Fines</p>
        </div>
      </div>
    </div>
  );
};

export default UserDashboard;
