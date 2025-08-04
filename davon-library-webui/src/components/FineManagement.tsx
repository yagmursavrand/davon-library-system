// src/components/FineManagement.tsx
import React,
{
    useState,
    useEffect
} from 'react';
import { useAuth } from '../contexts/AuthContext';
import adminService, { FineData } from '../services/adminService';
import styles from './FineManagement.module.css';

const FineManagement: React.FC = () => {
    const { user } = useAuth();
    const [fines, setFines] = useState<FineData[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    const fetchFines = async () => {
        if (!user) return;

        setLoading(true);
        setError(null);
        try {
            const unpaidFines = await adminService.getUnpaidFines(user.id);
            setFines(unpaidFines);
        } catch (err) {
            setError('Failed to fetch fines. Please try again.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchFines();
    }, [user]);

    return (
        <div className={styles.fineManagementContainer}>
            <div className={styles.header}>
                <h2>Active Fines</h2>
                <button 
                    onClick={fetchFines} 
                    className={styles.refreshButton}
                    disabled={loading}
                >
                    {loading ? 'Loading...' : 'Refresh'}
                </button>
            </div>
            {error && <p className={styles.error}>{error}</p>}
            <div className={styles.tableContainer}>
                <table className={styles.fineTable}>
                    <thead>
                        <tr>
                            <th>User Name</th>
                            <th>Email</th>
                            <th>Amount</th>
                            <th>Reason</th>
                            <th>Issued Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        {fines.length > 0 ? (
                            fines.map((fine) => (
                                <tr key={fine.id}>
                                    <td>{fine.userName}</td>
                                    <td>{fine.userEmail}</td>
                                    <td>${fine.amount.toFixed(2)}</td>
                                    <td>{fine.reason}</td>
                                    <td>{new Date(fine.issuedDate).toLocaleDateString()}</td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan={5} className={styles.noFines}>
                                    {loading ? 'Loading fines...' : 'No active fines found.'}
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default FineManagement;
