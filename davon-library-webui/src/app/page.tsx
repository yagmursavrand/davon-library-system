'use client';

import styles from './page.module.css';
import UserRegistrationForm from '@/components/UserRegistrationForm';

export default function Home() {
  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <h1 className={styles.title}>Davon Library System</h1>
        <p className={styles.welcome}>Welcome to our library management system</p>
        <UserRegistrationForm />
      </main>
    </div>
  );
} 