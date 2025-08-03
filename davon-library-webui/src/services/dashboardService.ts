// src/services/dashboardService.ts
import { User } from '../types/user';

export interface UrgentLoan {
  bookTitle: string;
  dueDate: string;
  daysUntilDue: number;
  status: 'OVERDUE' | 'DUE SOON';
}

export interface DashboardStats {
  activeLoansCount: number;
  overdueLoansCount: number;
  outstandingFines: string;
  urgentLoans: UrgentLoan[];
}

class DashboardService {
  private API_BASE_URL = 'http://localhost:8082/api';

  async getDashboardData(memberId: string): Promise<DashboardStats> {
    const response = await fetch(`${this.API_BASE_URL}/members/${memberId}/dashboard`, {
      headers: {
        // Assuming the user's ID is used as the Bearer token for authentication
        'Authorization': `Bearer ${memberId}`,
      },
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(errorText || 'Failed to fetch dashboard data');
    }

    return response.json();
  }
}

const dashboardService = new DashboardService();
export default dashboardService;
