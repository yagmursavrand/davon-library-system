import React from 'react';
import styles from './UserProfileInfo.module.css';
import { User } from '../types/user';

interface UserProfileInfoProps {
    user: User | null;
    isLoading: boolean;
    error?: string | null;
}

const UserProfileInfo: React.FC<UserProfileInfoProps> = ({ user, isLoading, error }) => {
    if (isLoading) {
        return <div className={styles.loading}>Loading profile...</div>;
    }
    if (error) {
        return <div className={styles.error}>{error}</div>;
    }
    if (!user) {
        return <div className={styles.empty}>No user data found.</div>;
    }
    return (
        <div className={styles.profileBox}>
            <h2 className={styles.title}>Profile</h2>
            <div className={styles.infoRow}><span>Name:</span> {user.name}</div>
            <div className={styles.infoRow}><span>Email:</span> {user.email}</div>
            <div className={styles.infoRow}><span>Role:</span> {user.role}</div>
            <div className={styles.infoRow}><span>Created At:</span> {new Date(user.createdAt).toLocaleString()}</div>
        </div>
    );
};

export default UserProfileInfo; 