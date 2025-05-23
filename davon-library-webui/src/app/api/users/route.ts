import { NextResponse } from 'next/server';
import { User, LoginCredentials, RegisterData } from '../../../types/user';

// In-memory database (server-side)
let users: User[] = [
  {
    id: '1',
    name: 'Admin User',
    email: 'admin@example.com',
    password: 'admin123', // Gerçek uygulamada hash'lenmeli!
    role: 'admin',
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
];

// Helper function to generate ID
const generateId = (): string => {
    return Math.random().toString(36).substr(2, 9);
};

// GET /api/users
export async function GET() {
    return NextResponse.json(users);
}

// POST /api/users/register
export async function POST(request: Request) {
    try {
        const data: RegisterData = await request.json();

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
        return NextResponse.json(newUser);
    } catch (error) {
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
}

// POST /api/users/login
export async function PUT(request: Request) {
    try {
        const credentials: LoginCredentials = await request.json();
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
    } catch (error) {
        return NextResponse.json(
            { error: 'Internal server error' },
            { status: 500 }
        );
    }
} 