import React, { useState } from 'react';
import styles from './UserProfileInfo.module.css';
import { User } from '../types/user';
import { userService } from '../services/userService';
import { useAuth } from '../contexts/AuthContext';

interface UserProfileInfoProps {
    user: User | null;
    isLoading: boolean;
    error?: string | null;
}

const UserProfileInfo: React.FC<UserProfileInfoProps> = ({ user, isLoading, error }) => {
    const { setUser } = useAuth(); // <-- get setUser from context
    const [editing, setEditing] = useState(false);
    const [form, setForm] = useState({ name: user?.name || '', email: user?.email || '' });
    const [formError, setFormError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);

    if (isLoading) return <div className={styles.loading}>Loading profile...</div>;
    if (error) return <div className={styles.error}>{error}</div>;
    if (!user) return <div className={styles.empty}>No user data found.</div>;

    const handleEditClick = () => {
        setForm({ name: user.name, email: user.email });
        setEditing(true);
        setFormError(null);
    };

    const handleDelete = async () => {
        if (window.confirm('Are you sure you want to delete your account?')) {
            try {
                setLoading(true);
                await userService.deleteUser(user.id);
                // Optionally, redirect or log out
                window.location.reload();
            } catch (err: any) {
                setFormError(err.message || 'Delete failed');
            }
            setLoading(false);
        }
    };

    const handleFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleFormSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setFormError(null);
        try {
            const updatedUser = await userService.updateUser(user.id, form);
            setUser(updatedUser); // update context, UI updates instantly!
            setEditing(false);
        } catch (err: any) {
            setFormError(err.message || 'Update failed');
        }
        setLoading(false);
    };

    if (editing) {
        return (
            <form className={styles.profileBox} onSubmit={handleFormSubmit}>
                <h2 className={styles.title}>Edit Profile</h2>
                <div className={styles.infoRow}>
                    <label>Name:</label>
                    <input
                        name="name"
                        value={form.name}
                        onChange={handleFormChange}
                        required
                        className={styles.input}
                    />
                </div>
                <div className={styles.infoRow}>
                    <label>Email:</label>
                    <input
                        name="email"
                        value={form.email}
                        onChange={handleFormChange}
                        required
                        type="email"
                        className={styles.input}
                    />
                </div>
                {formError && <div className={styles.error}>{formError}</div>}
                <div className={styles.buttonRow}>
                    <button type="submit" className={styles.editButton} disabled={loading}>Save</button>
                    <button type="button" className={styles.deleteButton} onClick={() => setEditing(false)} disabled={loading}>Cancel</button>
                </div>
            </form>
        );
    }

    return (
        <div className={styles.profileBox}>
            <h2 className={styles.title}>Profile</h2>
            <div className={styles.infoRow}><span>Name:</span> {user.name}</div>
            <div className={styles.infoRow}><span>Email:</span> {user.email}</div>
            <div className={styles.infoRow}><span>Role:</span> {user.role}</div>
            <div className={styles.infoRow}><span>Created At:</span> {new Date(user.createdAt).toLocaleString()}</div>
            <div className={styles.buttonRow}>
                <button className={styles.editButton} onClick={handleEditClick}>Edit Profile</button>
                <button className={styles.deleteButton} onClick={handleDelete} disabled={loading}>Delete Account</button>
            </div>
            {formError && <div className={styles.error}>{formError}</div>}
        </div>
    );
};

export default UserProfileInfo; 