'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import UserProfileInfo from '../../components/UserProfileInfo';
import BookCatalog from '../../components/BookCatalog';
import { useRouter, useSearchParams } from 'next/navigation';
import LoanManagement from '../../components/LoanManagement';
import UserDashboard from '../../components/UserDashboard';
import AdminDashboard from '../../components/AdminDashboard'; // Import the new AdminDashboard
import styles from './page.module.css';

// Define the possible tabs for each role
type MemberTab = 'dashboard' | 'books' | 'loans' | 'profile';
type AdminTab = 'admin_dashboard' | 'books' | 'profile';

export default function HomePage() {
    const { user, isLoading, error, logout } = useAuth();
    const router = useRouter();
    const searchParams = useSearchParams();

    // The active tab state can now hold tabs from either role.
    const [activeTab, setActiveTab] = useState<MemberTab | AdminTab>('dashboard');

    const isAdmin = user?.role === 'ADMIN';

    // Set initial tab based on URL parameter and user role
    useEffect(() => {
        const tabParam = searchParams.get('tab');
        const defaultTab = isAdmin ? 'admin_dashboard' : 'dashboard';
        
        // Validate the tab based on the user's role
        if (isAdmin) {
            if (tabParam === 'admin_dashboard' || tabParam === 'books' || tabParam === 'profile') {
                setActiveTab(tabParam);
            } else {
                setActiveTab(defaultTab);
            }
        } else {
            if (tabParam === 'dashboard' || tabParam === 'books' || tabParam === 'loans' || tabParam === 'profile') {
                setActiveTab(tabParam);
            } else {
                setActiveTab(defaultTab);
            }
        }
    }, [searchParams, isAdmin]);

    const handleLogout = () => {
        logout();
        window.location.href = 'http://127.0.0.1:5500/davon-library-landing-page/index.html';
    };

    // Render content based on the user's role and the active tab
    const renderContent = () => {
        switch (activeTab) {
            // Admin-specific views
            case 'admin_dashboard':
                return isAdmin ? <AdminDashboard /> : null; // Use the new AdminDashboard component

            // Member-specific views
            case 'dashboard':
                return !isAdmin ? <UserDashboard /> : null;
            case 'loans':
                return !isAdmin ? <LoanManagement /> : null;

            // Shared views
            case 'books':
                return <BookCatalog showAdminControls={isAdmin} />;
            case 'profile':
                return (
                    <div className={styles.profileBoxWrapper}>
                        <div className={styles.profileBox}>
                            <UserProfileInfo user={user} isLoading={isLoading} error={error} />
                        </div>
                    </div>
                );
            default:
                return null;
        }
    };

    // Render navigation tabs based on the user's role
    const renderTabs = () => {
        if (isAdmin) {
            return (
                <>
                    <button
                        onClick={() => setActiveTab('admin_dashboard')}
                        className={`${styles.tabButton} ${activeTab === 'admin_dashboard' ? styles.active : ''}`}
                    >
                        Admin Dashboard
                    </button>
                    <button
                        onClick={() => setActiveTab('books')}
                        className={`${styles.tabButton} ${activeTab === 'books' ? styles.active : ''}`}
                    >
                        Book Catalog
                    </button>
                    <button
                        onClick={() => setActiveTab('profile')}
                        className={`${styles.tabButton} ${activeTab === 'profile' ? styles.active : ''}`}
                    >
                        My Profile
                    </button>
                </>
            );
        } else {
            return (
                <>
                    <button
                        onClick={() => setActiveTab('dashboard')}
                        className={`${styles.tabButton} ${activeTab === 'dashboard' ? styles.active : ''}`}
                    >
                        Dashboard
                    </button>
                    <button
                        onClick={() => setActiveTab('books')}
                        className={`${styles.tabButton} ${activeTab === 'books' ? styles.active : ''}`}
                    >
                        Book Catalog
                    </button>
                    <button
                        onClick={() => setActiveTab('loans')}
                        className={`${styles.tabButton} ${activeTab === 'loans' ? styles.active : ''}`}
                    >
                        My Loans
                    </button>
                    <button
                        onClick={() => setActiveTab('profile')}
                        className={`${styles.tabButton} ${activeTab === 'profile' ? styles.active : ''}`}
                    >
                        My Profile
                    </button>
                </>
            );
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.headerRow}>
                <span className={styles.headerText}>Welcome to the Library System!</span>
                <button
                    onClick={handleLogout}
                    className={styles.logoutButton}
                >
                    Log out
                </button>
            </div>

            <div className={styles.tabContainer}>
                {renderTabs()}
            </div>

            <div className={styles.contentArea}>
                {renderContent()}
            </div>
        </div>
    );
}
