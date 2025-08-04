// src/services/adminService.ts

import { userService } from './userService';
import { bookService } from './bookService';
import loanService from './loanService';
import { User } from '../types/user';
import { Book } from '../types/book';
import { Loan } from '../services/loanService';
import api from '../lib/axios';

export interface AdminDashboardStats {
  totalUsers: number;
  totalBooks: number;
  activeLoans: number;
}

export interface FineData {
  id: number;
  userName: string;
  userEmail: string;
  amount: number;
  reason: string;
  issuedDate: string;
}

const getDashboardStats = async (adminId: string): Promise<AdminDashboardStats> => {
  try {
    const [users, books, loans] = await Promise.all([
      userService.getAllUsers(adminId),
      bookService.getAllBooks(),
      loanService.getAllLoans(adminId),
    ]);

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
    throw error;
  }
};

const getUnpaidFines = async (adminId: string): Promise<FineData[]> => {
    try {
        const response = await api.get('/admins/fines/unpaid', {
            headers: {
                Authorization: `Bearer ${adminId}`,
            },
        });
        return response.data;
    } catch (error) {
        console.error('Failed to fetch unpaid fines:', error);
        throw error;
    }
}

const adminService = {
  getDashboardStats,
  getUnpaidFines,
};

export default adminService;
