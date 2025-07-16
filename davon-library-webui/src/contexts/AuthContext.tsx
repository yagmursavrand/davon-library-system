'use client';
import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, LoginCredentials, RegisterData, AuthState } from '../types/user';
import { userService } from '../services/userService';

interface AuthContextType extends AuthState {
    login: (credentials: LoginCredentials) => Promise<void>;
    register: (data: RegisterData) => Promise<void>;
    logout: () => void;
    setUser: (user: User | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [state, setState] = useState<AuthState>({
        user: null,
        isAuthenticated: false,
        isLoading: true,
        error: null
    });

    useEffect(() => {
        // Check if user is stored in localStorage
        const storedUser = localStorage.getItem('davon_library_current_user');
        if (storedUser) {
            setState({
                user: JSON.parse(storedUser),
                isAuthenticated: true,
                isLoading: false,
                error: null
            });
        } else {
            setState(prev => ({ ...prev, isLoading: false }));
        }
    }, []);

    const login = async (credentials: LoginCredentials) => {
        try {
            setState(prev => ({ ...prev, isLoading: true, error: null }));
            const user = await userService.login(credentials);
            localStorage.setItem('davon_library_current_user', JSON.stringify(user));
            setState({
                user,
                isAuthenticated: true,
                isLoading: false,
                error: null
            });
        } catch (error) {
            setState(prev => ({
                ...prev,
                isLoading: false,
                error: error instanceof Error ? error.message : 'An error occurred'
            }));
            throw error;
        }
    };

    const register = async (data: RegisterData) => {
        try {
            setState(prev => ({ ...prev, isLoading: true, error: null }));
            const user = await userService.register(data);
            localStorage.setItem('davon_library_current_user', JSON.stringify(user));
            setState({
                user,
                isAuthenticated: true,
                isLoading: false,
                error: null
            });
        } catch (error) {
            setState(prev => ({
                ...prev,
                isLoading: false,
                error: error instanceof Error ? error.message : 'An error occurred'
            }));
            throw error;
        }
    };

    const logout = () => {
        localStorage.removeItem('davon_library_current_user');
        setState({
            user: null,
            isAuthenticated: false,
            isLoading: false,
            error: null
        });
    };

    const setUser = (user: User | null) => {
        setState(prev => ({ ...prev, user }));
    };

    return (
        <AuthContext.Provider value={{ ...state, login, register, logout, setUser }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}; 