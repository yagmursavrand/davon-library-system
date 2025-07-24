'use client';

import React, { useState, useEffect } from 'react';
import { User } from '../types/user';
import { userService } from '../services/userService';
import styles from './UserList.module.css';

export default function UserList() {
    const [users, setUsers] = useState<User[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            setIsLoading(true);
            setError(null);
            const allUsers = await userService.getAllUsers();
            setUsers(allUsers);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to fetch users');
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeleteUser = async (userId: string) => {
        if (!confirm('Are you sure you want to delete this user?')) {
            return;
        }

        try {
            await userService.deleteUser(userId);
            setUsers(users.filter(user => user.id !== userId));
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to delete user');
        }
    };

    if (isLoading) {
        return (
            <div className={styles.loading}>
                <h3>Loading users...</h3>
            </div>
        );
    }

    if (error) {
        return (
            <div className={styles.error}>
                <h3>Error: {error}</h3>
                <button onClick={fetchUsers} className={styles.retryButton}>
                    Retry
                </button>
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h2>User Management</h2>
                <button onClick={fetchUsers} className={styles.refreshButton}>
                    Refresh
                </button>
            </div>
            
            <div className={styles.stats}>
                <span>Total Users: {users.length}</span>
                <span>Admins: {users.filter(u => u.role === 'admin').length}</span>
                <span>Regular Users: {users.filter(u => u.role === 'user').length}</span>
            </div>

            <div className={styles.tableContainer}>
                <table className={styles.table}>
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Role</th>
                            <th>Created At</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map(user => (
                            <tr key={user.id} className={styles.tableRow}>
                                <td>{user.name}</td>
                                <td>{user.email}</td>
                                <td>
                                    <span className={`${styles.role} ${styles[user.role]}`}>
                                        {user.role}
                                    </span>
                                </td>
                                <td>{new Date(user.createdAt).toLocaleDateString()}</td>
                                <td>
                                    <div className={styles.actions}>
                                        <button 
                                            className={styles.viewButton}
                                            onClick={() => alert(`User ID: ${user.id}\nName: ${user.name}\nEmail: ${user.email}\nRole: ${user.role}\nCreated: ${new Date(user.createdAt).toLocaleString()}`)}
                                        >
                                            View
                                        </button>
                                        <button 
                                            className={styles.deleteButton}
                                            onClick={() => handleDeleteUser(user.id)}
                                            disabled={user.role === 'admin'}
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {users.length === 0 && (
                <div className={styles.empty}>
                    <h3>No users found</h3>
                    <p>There are no users in the system.</p>
                </div>
            )}
        </div>
    );
} 