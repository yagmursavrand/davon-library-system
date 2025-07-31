import { Loan, LoanStatus } from '../types/book';

class LoanService {
    private readonly API_BASE_URL = 'http://localhost:8082/api';

    async getAllLoans(): Promise<Loan[]> {
        const response = await fetch(`${this.API_BASE_URL}/loans`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to fetch loans');
        }
        return response.json();
    }

    async getLoanById(id: string): Promise<Loan | null> {
        const response = await fetch(`${this.API_BASE_URL}/loans/${id}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            return null;
        }
        return response.json();
    }

    async getLoansByMember(memberId: string): Promise<Loan[]> {
        const response = await fetch(`${this.API_BASE_URL}/loans/member/${memberId}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to fetch member loans');
        }
        return response.json();
    }

    async getActiveLoansByMember(memberId: string): Promise<Loan[]> {
        const response = await fetch(`${this.API_BASE_URL}/loans/member/${memberId}/active`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to fetch active loans');
        }
        return response.json();
    }

    async borrowBook(bookId: string, memberId: string, loanPeriod: number = 14): Promise<Loan> {
        const response = await fetch(`${this.API_BASE_URL}/loans/borrow`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
            body: JSON.stringify({
                bookId: parseInt(bookId),
                memberId: parseInt(memberId),
                loanPeriod: loanPeriod
            }),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to borrow book');
        }

        return response.json();
    }

    async returnBook(loanId: string): Promise<Loan> {
        const response = await fetch(`${this.API_BASE_URL}/loans/${loanId}/return`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to return book');
        }

        return response.json();
    }

    async renewLoan(loanId: string, additionalDays: number = 7): Promise<Loan> {
        const response = await fetch(`${this.API_BASE_URL}/loans/${loanId}/renew`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
            body: JSON.stringify({
                additionalDays: additionalDays
            }),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Failed to renew loan');
        }

        return response.json();
    }

    async getOverdueLoans(): Promise<Loan[]> {
        const response = await fetch(`${this.API_BASE_URL}/loans/overdue`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to fetch overdue loans');
        }
        return response.json();
    }

    async getLoansByStatus(status: LoanStatus): Promise<Loan[]> {
        const response = await fetch(`${this.API_BASE_URL}/loans/status/${status}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to fetch loans by status');
        }
        return response.json();
    }
}

export const loanService = new LoanService(); 