// src/services/adminService.ts

import { userService } from './userService';
import { bookService } from './bookService';
import loanService from './loanService';
import { User } from '../types/user';
import { Book } from '../types/book';
import { Loan } from '../services/loanService';

export interface AdminDashboardStats {
  totalUsers: number;
  totalBooks: number;
  activeLoans: number;
}

const getDashboardStats = async (adminId: string): Promise<AdminDashboardStats> => {
  try {
    // We can use Promise.all to fetch these in parallel for better performance
    const [users, books, loans] = await Promise.all([
      userService.getAllUsers(adminId),
      bookService.getAllBooks(),
      loanService.getAllLoans(adminId),
    ]);

    // Filter for active loans
    const activeLoans = loans.filter(
      (loan) => loan.status === 'ACTIVE' || loan.status === 'RENEWED'
    ).length;

    return {
      totalUsers: users.length,
      totalBooks: books.length,
      activeLoans: activeLoans,
    };
  } catch (error) {
    console.error('Failed to fetch admin dashboard stats:', error);
    // Re-throw the error to be handled by the component
    throw error;
  }
};

const adminService = {
  getDashboardStats,
};

export default adminService;
