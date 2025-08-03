import { Book } from '../types/book';
import { User } from '../types/user';

export enum LoanStatus {
  ACTIVE = 'ACTIVE',
  RETURNED = 'RETURNED',
  OVERDUE = 'OVERDUE',
  RENEWED = 'RENEWED',
  LOST = 'LOST',
}

export interface Member {
  id: number;
  name: string;
  email: string;
  membershipNumber: string;
}

export interface Loan {
  id: number;
  loanDate: string;
  dueDate: string;
  returnDate?: string;
  fineAmount: string;
  status: string;
  bookTitle?: string;
  bookId?: number;
  member?: Member;
}

class LoanService {
  private API_BASE_URL = 'http://localhost:8082/api';

  async getLoansByMember(memberId: string): Promise<Loan[]> {
    const response = await fetch(`${this.API_BASE_URL}/loans/member/${memberId}`);

    if (!response.ok) {
      const errorText = await response.text();
      console.error('Failed to fetch loans:', errorText);
      throw new Error(`Failed to fetch loans: ${errorText}`);
    }
    return response.json();
  }

  async borrowBook(memberId: string, bookId: number): Promise<Loan> {
    const response = await fetch(`${this.API_BASE_URL}/members/${memberId}/borrow/${bookId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${memberId}`,
      },
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to borrow book');
    }
    return response.json();
  }
}

const loanService = new LoanService();
export default loanService;
