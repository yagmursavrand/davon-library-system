'use client';

import React, { useEffect } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useRouter } from 'next/navigation';
import UserList from '../../components/UserList';

export default function AdminPage() {
    const { user, isAuthenticated, isLoading } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (!isLoading) {
            if (!isAuthenticated) {
                router.push('/login');
                return;
            }
            
            if (user?.role !== 'admin') {
                router.push('/home');
                return;
            }
        }
    }, [user, isAuthenticated, isLoading, router]);

    if (isLoading) {
        return (
            <div style={{ 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center', 
                height: '100vh' 
            }}>
                <h2>Loading...</h2>
            </div>
        );
    }

    if (!isAuthenticated) {
        return null; // Will redirect to login
    }

    if (user?.role !== 'admin') {
        return null; // Will redirect to home
    }

    return (
        <main style={{ maxWidth: 1200, margin: '2rem auto', padding: '1rem' }}>
            <div style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center', 
                marginBottom: '2rem' 
            }}>
                <h1>Admin Dashboard</h1>
                <button
                    onClick={() => router.push('/home')}
                    style={{
                        padding: '0.5rem 1rem',
                        background: '#2196f3',
                        color: 'white',
                        border: 'none',
                        borderRadius: 6,
                        cursor: 'pointer',
                        fontSize: '1rem'
                    }}
                >
                    Back to Home
                </button>
            </div>
            <UserList />
        </main>
    );
} 