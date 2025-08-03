export type UserRole = 'USER' | 'ADMIN' | 'MEMBER';

export interface User {
    id: string;
    name: string;
    email: string;
    password: string;
    role: UserRole;
    createdAt: string;
    updatedAt: string;
}

export interface LoginCredentials {
    email: string;
    password: string;
}

export interface AuthState {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    error: string | null;
}

export interface RegisterData {
    name: string;
    email: string;
    password: string;
    confirmPassword: string;
}

export interface ValidationError {
    field: string;
    message: string;
} 