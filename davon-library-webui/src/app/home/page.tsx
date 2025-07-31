'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import UserProfileInfo from '../../components/UserProfileInfo';
import BookCatalog from '../../components/BookCatalog';
import { useRouter, useSearchParams } from 'next/navigation';
import styles from './page.module.css';

export default function HomePage() {
    const { user, isLoading, error, logout } = useAuth();
    const router = useRouter();
    const searchParams = useSearchParams();
    const [activeTab, setActiveTab] = useState<'profile' | 'books'>('books');

    // Set initial tab based on URL parameter
    useEffect(() => {
        const tabParam = searchParams.get('tab');
        if (tabParam === 'profile') {
            setActiveTab('profile');
        } else {
            setActiveTab('books');
        }
    }, [searchParams]);

    const handleLogout = () => {
        logout();
        window.location.href = 'http://127.0.0.1:5500/davon-library-landing-page/index.html';
    };

    return (
        <div className={styles.container}>
            <div className={styles.headerRow}>
                <span className={styles.headerText}>Welcome to the Library System!</span>
                <div className="flex gap-4">
                    {user?.role === 'admin' && (
                        <button
                            onClick={() => router.push('/admin')}
                            className={styles.logoutButton}
                            style={{ background: '#2196f3' }}
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
            </div>

            {/* Navigation Tabs */}
            <div className={styles.tabContainer}>
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
            </div>

            {/* Content Area */}
            <div className={styles.contentArea}>
                {activeTab === 'books' ? (
                    <BookCatalog showAdminControls={user?.role === 'admin'} />
                ) : (
                    <div className={styles.profileBoxWrapper}>
                        <div className={styles.profileBox}>
                            <UserProfileInfo user={user} isLoading={isLoading} error={error} />
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
} 