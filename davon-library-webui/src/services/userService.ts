import { User, LoginCredentials, RegisterData, ValidationError } from '../types/user';

class UserService {
    private readonly API_BASE_URL = 'http://localhost:8082/api';

    async register(data: RegisterData): Promise<User> {
        const requestBody = {
            name: data.name,
            email: data.email,
            password: data.password,
        };

        const response = await fetch(`${this.API_BASE_URL}/users`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestBody),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Registration failed');
        }
        return response.json();
    }

    async login(credentials: LoginCredentials): Promise<User> {
        const response = await fetch(`${this.API_BASE_URL}/users/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(credentials),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Login failed');
        }
        return response.json();
    }

    async getUserById(id: string): Promise<User | null> {
        const response = await fetch(`${this.API_BASE_URL}/users/${id}`);
        if (!response.ok) {
            return null;
        }
        return response.json();
    }

    async getUserByEmail(email: string): Promise<User | null> {
        const response = await fetch(`${this.API_BASE_URL}/users?email=${email}`);
        if (!response.ok) {
            return null;
        }
        const users = await response.json();
        return users.find((user: User) => user.email === email) || null;
    }

    async updateUser(id: string, data: Partial<User>, adminId: string): Promise<User> {
        const response = await fetch(`${this.API_BASE_URL}/users/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${adminId}`,
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Update failed');
        }
        return response.json();
    }

    async deleteUser(id: string, adminId: string): Promise<void> {
        const response = await fetch(`${this.API_BASE_URL}/users/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${adminId}`,
            },
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Delete failed');
        }
    }

    async getAllUsers(adminId: string): Promise<User[]> {
        const response = await fetch(`${this.API_BASE_URL}/users`, {
            headers: {
                'Authorization': `Bearer ${adminId}`,
            },
        });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to fetch users');
        }
        return response.json();
    }
}

export const userService = new UserService();
