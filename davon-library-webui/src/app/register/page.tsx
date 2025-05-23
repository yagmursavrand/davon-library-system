'use client';

import styles from './page.module.css';
import UserRegisterForm from '@/components/UserRegistrationForm';

export default function Register() {
    return (
        <div className={styles.page}>
            <main className={styles.main}>
                <h1 className={styles.title}>Register to Davon Library</h1>
                <p className={styles.welcome}>Create your account to get started</p>
                <UserRegisterForm />
            </main>
        </div>
    );
} 