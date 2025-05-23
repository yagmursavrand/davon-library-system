'use client';

import React from 'react';
import { useAuth } from '../../contexts/AuthContext';
import UserProfileInfo from '../../components/UserProfileInfo';
import { useRouter } from 'next/navigation';

export default function HomePage() {
    const { user, isLoading, error, logout } = useAuth();
    const router = useRouter();

    const handleLogout = () => {
        logout();
        router.push('/davon-library-landing-page/index.html');
    };

    return (
        <main style={{ maxWidth: 600, margin: '2rem auto', padding: '1rem', position: 'relative' }}>
            <button
                onClick={handleLogout}
                style={{
                    position: 'absolute',
                    top: 20,
                    right: 20,
                    padding: '0.5rem 1.2rem',
                    background: '#c62828',
                    color: 'white',
                    border: 'none',
                    borderRadius: 6,
                    fontWeight: 600,
                    cursor: 'pointer',
                    fontSize: '1rem',
                    boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
                }}
            >
                Log out
            </button>
            <h1>Welcome to the Library System!</h1>
            <UserProfileInfo user={user} isLoading={isLoading} error={error} />
            {/* Diğer bölümler (BookLending, EventRegistration) buraya eklenecek */}
        </main>
    );
} 