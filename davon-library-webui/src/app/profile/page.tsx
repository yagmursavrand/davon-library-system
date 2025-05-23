'use client';

import React from 'react';
import { useAuth } from '../../contexts/AuthContext';
import UserProfileInfo from '../../components/UserProfileInfo';

export default function ProfilePage() {
    const { user, isLoading, error, isAuthenticated } = useAuth();

    // Optionally, you can redirect to login if not authenticated
    // or show a message

    return (
        <main style={{ maxWidth: 600, margin: '2rem auto', padding: '1rem' }}>
            <UserProfileInfo user={user} isLoading={isLoading} error={error} />
        </main>
    );
} 