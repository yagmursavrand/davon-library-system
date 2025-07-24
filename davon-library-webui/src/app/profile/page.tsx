'use client';

import React from 'react';
import { useAuth } from '../../contexts/AuthContext';
import UserProfileInfo from '../../components/UserProfileInfo';

export default function ProfilePage() {
    const { user, isLoading, error } = useAuth();
    return (
        <main style={{ maxWidth: 600, margin: '2rem auto', padding: '1rem' }}>
            <UserProfileInfo user={user} isLoading={isLoading} error={error} />
        </main>
    );
} 