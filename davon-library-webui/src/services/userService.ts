import { User, LoginCredentials, RegisterData, ValidationError } from '../types/user';

class UserService {
    private readonly API_BASE_URL = 'http://localhost:8082/api';

    async register(data: RegisterData): Promise<User> {
        // Create a new object for the request body, excluding confirmPassword
        const requestBody = {
            name: data.name,
            email: data.email,
            password: data.password,
        };

        const response = await fetch(`${this.API_BASE_URL}/users`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestBody), // Send the cleaned object
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Registration failed');
        }

        return response.json();
    }

    async login(credentials: LoginCredentials): Promise<User> {
        const response = await fetch(`${this.API_BASE_URL}/users/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(credentials),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Login failed');
        }

        return response.json();
    }

    async getUserById(id: string): Promise<User | null> {
        const response = await fetch(`${this.API_BASE_URL}/users/${id}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            return null;
        }
        return response.json();
    }

    async getUserByEmail(email: string): Promise<User | null> {
        const response = await fetch(`${this.API_BASE_URL}/users?email=${email}`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            return null;
        }
        const users = await response.json();
        return users.find((user: User) => user.email === email) || null;
    }

    async updateUser(id: string, data: Partial<User>): Promise<User> {
        const response = await fetch(`${this.API_BASE_URL}/users/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Update failed');
        }

        return response.json();
    }

    async deleteUser(id: string): Promise<void> {
        const response = await fetch(`${this.API_BASE_URL}/users/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Delete failed');
        }
    }

    async getAllUsers(): Promise<User[]> {
        const response = await fetch(`${this.API_BASE_URL}/users`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`,
            },
        });
        if (!response.ok) {
            throw new Error('Failed to fetch users');
        }
        return response.json();
    }
}

export const userService = new UserService();
