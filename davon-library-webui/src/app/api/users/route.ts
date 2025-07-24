import { NextResponse } from 'next/server';
import { User, LoginCredentials, RegisterData } from '../../../types/user';
import { promises as fs } from 'fs';
import path from 'path';

// File path for storing users data
const USERS_FILE_PATH = path.join(process.cwd(), 'data', 'users.json');

// Helper function to ensure data directory exists
const ensureDataDirectory = async () => {
    const dataDir = path.dirname(USERS_FILE_PATH);
    try {
        await fs.access(dataDir);
    } catch {
        await fs.mkdir(dataDir, { recursive: true });
    }
};

// Helper function to load users from file
const loadUsers = async (): Promise<User[]> => {
    try {
        await ensureDataDirectory();
        const data = await fs.readFile(USERS_FILE_PATH, 'utf-8');
        return JSON.parse(data);
    } catch (error) {
        // If file doesn't exist, return default admin user
        return [
            {
                id: '1',
                name: 'Admin User',
                email: 'admin@example.com',
                password: 'admin123',
                role: 'admin',
                createdAt: new Date().toISOString(),
                updatedAt: new Date().toISOString()
            }
        ];
    }
};

// Helper function to save users to file
const saveUsers = async (users: User[]): Promise<void> => {
    await ensureDataDirectory();
    await fs.writeFile(USERS_FILE_PATH, JSON.stringify(users, null, 2));
};

// Helper function to generate ID
const generateId = (): string => {
    return Math.random().toString(36).substr(2, 9);
};

// GET /api/users
export async function GET() {
    try {
        const users = await loadUsers();
        return NextResponse.json(users);
    } catch (error) {
        return NextResponse.json(
            { error: 'Failed to load users' },
            { status: 500 }
        );
    }
}

// POST /api/users/register
export async function POST(request: Request) {
    try {
        const data: RegisterData = await request.json();
        const users = await loadUsers();

        // Validation
        if (!data.name.trim()) {
            return NextResponse.json(
                { error: 'Name is required' },
                { status: 400 }
            );
        }

        if (!data.email.trim()) {
            return NextResponse.json(
                { error: 'Email is required' },
                { status: 400 }
            );
        }

        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) {
            return NextResponse.json(
                { error: 'Invalid email format' },
                { status: 400 }
            );
        }

        if (users.some(user => user.email === data.email)) {
            return NextResponse.json(
                { error: 'Email already exists' },
                { status: 400 }
            );
        }

        if (!data.password) {
            return NextResponse.json(
                { error: 'Password is required' },
                { status: 400 }
            );
        }

        if (data.password.length < 6) {
            return NextResponse.json(
                { error: 'Password must be at least 6 characters long' },
                { status: 400 }
            );
        }

        if (data.password !== data.confirmPassword) {
            return NextResponse.json(
                { error: 'Passwords do not match' },
                { status: 400 }
            );
        }

        const newUser: User = {
            id: generateId(),
            name: data.name,
            email: data.email,
            password: data.password, // In a real app, this should be hashed
            role: 'user',
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
        };

        users.push(newUser);
        await saveUsers(users);
        
        return NextResponse.json(newUser);
    } catch (error) {
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
}

// PUT /api/users (login or update)
export async function PUT(request: Request) {
    try {
        const url = new URL(request.url);
        const id = url.searchParams.get('id');
        if (id) {
            // Update user by id
            const data = await request.json();
            const users = await loadUsers();
            const userIndex = users.findIndex(u => u.id === id);
            if (userIndex === -1) {
                return NextResponse.json(
                    { error: 'User not found' },
                    { status: 404 }
                );
            }
            users[userIndex] = {
                ...users[userIndex],
                ...data,
                updatedAt: new Date().toISOString(),
            };
            await saveUsers(users);
            return NextResponse.json(users[userIndex]);
        } else {
            // Login logic (existing)
        const credentials: LoginCredentials = await request.json();
        const users = await loadUsers();
        const user = users.find(u => u.email === credentials.email);

        if (!user) {
            return NextResponse.json(
                { error: 'User not found' },
                { status: 404 }
            );
        }

        if (user.password !== credentials.password) {
            return NextResponse.json(
                { error: 'Invalid password' },
                { status: 401 }
            );
        }

        return NextResponse.json(user);
        }
    } catch (error) {
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
}

// DELETE /api/users/[id]
export async function DELETE(request: Request) {
    try {
        const url = new URL(request.url);
        const id = url.searchParams.get('id');
        
        if (!id) {
            return NextResponse.json(
                { error: 'User ID is required' },
                { status: 400 }
            );
        }

        const users = await loadUsers();
        const userIndex = users.findIndex(u => u.id === id);
        
        if (userIndex === -1) {
            return NextResponse.json(
                { error: 'User not found' },
                { status: 404 }
            );
        }

        const userToDelete = users[userIndex];
        
        // Prevent deletion of admin users
        if (userToDelete.role === 'admin') {
            return NextResponse.json(
                { error: 'Cannot delete admin users' },
                { status: 403 }
            );
        }

        users.splice(userIndex, 1);
        await saveUsers(users);
        
        return NextResponse.json(
            { message: 'User deleted successfully' },
            { status: 200 }
        );
    } catch (error) {
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
} 

// PATCH /api/users/[id]
export async function PATCH(request: Request) {
    try {
        const url = new URL(request.url);
        const id = url.pathname.split('/').pop(); // get id from URL
        if (!id) {
            return NextResponse.json(
                { error: 'User ID is required' },
                { status: 400 }
            );
        }

        const data = await request.json();
        const users = await loadUsers();
        const userIndex = users.findIndex(u => u.id === id);

        if (userIndex === -1) {
            return NextResponse.json(
                { error: 'User not found' },
                { status: 404 }
            );
        }

        // Update user fields
        users[userIndex] = {
            ...users[userIndex],
            ...data,
            updatedAt: new Date().toISOString(),
        };

        await saveUsers(users);

        return NextResponse.json(users[userIndex]);
    } catch (error) {
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
} 