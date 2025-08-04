// src/components/AdminDashboard.tsx
'use client';

import React, { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import adminService, { AdminDashboardStats } from '../services/adminService';
import FineManagement from './FineManagement';
import UserList from './UserList';
import styles from './AdminDashboard.module.css';

const AdminDashboard: React.FC = () => {
  const { user } = useAuth(); // Use user object for ID
  const [stats, setStats] = useState<AdminDashboardStats | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStats = async () => {
      if (user) { // Check for user object instead of token
        try {
          setLoading(true);
          const dashboardStats = await adminService.getDashboardStats(user.id); // Pass user.id
          setStats(dashboardStats);
          setError(null);
        } catch (err) {
          setError('Failed to load dashboard statistics.');
          console.error(err);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchStats();
  }, [user]);

  return (
    <div className={styles.container}>
      <h2 className={styles.title}>Admin Overview</h2>
      
      {loading && <p className={styles.loading}>Loading statistics...</p>}
      {error && <p className={styles.error}>{error}</p>}
      
      {stats && (
        <div className={styles.statsGrid}>
          <div className={styles.statCard}>
            <p className={styles.statNumber}>{stats.totalUsers}</p>
            <p className={styles.statLabel}>Total Users</p>
          </div>
          <div className={styles.statCard}>
            <p className={styles.statNumber}>{stats.totalBooks}</p>
            <p className={styles.statLabel}>Total Books</p>
          </div>
          <div className={styles.statCard}>
            <p className={styles.statNumber}>{stats.activeLoans}</p>
            <p className={styles.statLabel}>Books on Loan</p>
          </div>
        </div>
      )}

      {/* Divider */}
      <hr className={styles.divider} />

      {/* User Management Section */}
      <div className={styles.userManagementSection}>
          <UserList />
      </div>

      {/* Divider */}
      <hr className={styles.divider} />

      {/* Fine Management Section */}
      <div className={styles.fineManagementSection}>
          <FineManagement />
      </div>
    </div>
  );
};

export default AdminDashboard;
