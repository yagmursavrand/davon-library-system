// API Configuration
export const API_CONFIG = {
    BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8082/api',
    TIMEOUT: 10000, // 10 seconds
    RETRY_ATTEMPTS: 3,
};

// API Endpoints
export const API_ENDPOINTS = {
    // User endpoints
    USERS: '/users',
    USER_LOGIN: '/users/login',
    USER_REGISTER: '/users',
    
    // Book endpoints
    BOOKS: '/books',
    BOOK_SEARCH: '/books/search',
    BOOK_GENRE: '/books/genre',
    
    // Loan endpoints
    LOANS: '/loans',
    LOAN_BORROW: '/loans/borrow',
    LOAN_RETURN: '/loans/return',
    LOAN_RENEW: '/loans/renew',
    LOAN_OVERDUE: '/loans/overdue',
    
    // Member endpoints
    MEMBERS: '/members',
    MEMBER_LOANS: '/loans/member',
    
    // Admin endpoints
    ADMIN_USERS: '/users',
    ADMIN_BOOKS: '/books',
    ADMIN_LOANS: '/loans',
    
    // Transaction endpoints
    TRANSACTIONS: '/transactions',
    FINES: '/fines',
};

// HTTP Methods
export const HTTP_METHODS = {
    GET: 'GET',
    POST: 'POST',
    PUT: 'PUT',
    DELETE: 'DELETE',
    PATCH: 'PATCH',
};

// Content Types
export const CONTENT_TYPES = {
    JSON: 'application/json',
    FORM_DATA: 'multipart/form-data',
};

// Authentication
export const AUTH_CONFIG = {
    TOKEN_KEY: 'library_token',
    USER_KEY: 'library_user',
    REFRESH_TOKEN_KEY: 'library_refresh_token',
}; 