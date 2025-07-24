'use client';

import React from 'react';
import { useAuth } from '../../contexts/AuthContext';
import UserProfileInfo from '../../components/UserProfileInfo';
import { useRouter } from 'next/navigation';
import styles from './page.module.css';

export default function HomePage() {
    const { user, isLoading, error, logout } = useAuth();
    const router = useRouter();

    const handleLogout = () => {
        logout();
        window.location.href = 'http://127.0.0.1:5500/davon-library-landing-page/index.html';
    };

    return (
        <div className={styles.container}>
            <div className={styles.headerRow}>
                <span className={styles.headerText}>Welcome to the Library System!</span>
                {user?.role === 'admin' && (
                    <button
                        onClick={() => router.push('/admin')}
                        className={styles.logoutButton}
                        style={{ background: '#2196f3', marginRight: '1rem' }}
                    >
                        Admin Dashboard
                    </button>
                )}
                <button
                    onClick={handleLogout}
                    className={styles.logoutButton}
                >
                    Log out
                </button>
            </div>
            <div className={styles.profileBoxWrapper}>
                <div className={styles.profileBox}>
                    <UserProfileInfo user={user} isLoading={isLoading} error={error} />
                </div>
            </div>
        </div>
    );
} 