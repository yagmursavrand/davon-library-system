import { User, LoginCredentials, RegisterData, ValidationError } from '../types/user';

class UserService {
    private readonly API_URL = '/api/users';

    async register(data: RegisterData): Promise<User> {
        const response = await fetch(this.API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Registration failed');
        }

        return response.json();
    }

    async login(credentials: LoginCredentials): Promise<User> {
        const response = await fetch(this.API_URL, {
            method: 'PUT',
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
        const response = await fetch(`${this.API_URL}/${id}`);
        if (!response.ok) {
            return null;
        }
        return response.json();
    }

    async getUserByEmail(email: string): Promise<User | null> {
        const response = await fetch(`${this.API_URL}?email=${email}`);
        if (!response.ok) {
            return null;
        }
        const users = await response.json();
        return users.find((user: User) => user.email === email) || null;
    }

    async updateUser(id: string, data: Partial<User>): Promise<User> {
        const response = await fetch(`${this.API_URL}/${id}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
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
        const response = await fetch(`${this.API_URL}?id=${id}`, {
            method: 'DELETE',
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.error || 'Delete failed');
        }
    }

    async getAllUsers(): Promise<User[]> {
        const response = await fetch(this.API_URL);
        if (!response.ok) {
            throw new Error('Failed to fetch users');
        }
        return response.json();
    }
}

export const userService = new UserService(); 