'use client';

import styles from './page.module.css';
import UserLoginForm from '@/components/UserLoginForm';

export default function Login() {
  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <h1 className={styles.title}>Login to Davon Library</h1>
        <p className={styles.welcome}>Welcome back! Please login to your account</p>
        <UserLoginForm />
      </main>
    </div>
  );
} 