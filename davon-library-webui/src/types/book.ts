export interface Book {
    id: number;
    title: string;
    isbn: string;
    publicationYear: number;
    genre: string;
    status: BookStatus;
    authors?: Author[];
    inventory?: Inventory;
    loans?: Loan[];
}

export interface Author {
    id: number;
    name: string;
    biography?: string;
    books?: Book[];
}

export interface Inventory {
    id: number;
    book: Book;
    library: Library;
    quantity: number;
    availableQuantity: number;
}

export interface Library {
    id: number;
    name: string;
    address: string;
    phone: string;
    email: string;
    inventories?: Inventory[];
    members?: Member[];
}

export interface Loan {
    id: number;
    book: Book;
    member: Member;
    loanDate: string;
    dueDate: string;
    returnDate?: string;
    fineAmount: number;
    status: LoanStatus;
}

export interface Member {
    id: number;
    user: User;
    library: Library;
    membershipNumber: string;
    membershipStart: string;
    membershipEnd: string;
    totalFines: number;
    loans?: Loan[];
}

export interface User {
    id: number;
    name: string;
    email: string;
    role: string;
    profile?: Profile;
}

export interface Profile {
    id: number;
    user: User;
    address: string;
    phone: string;
    dateOfBirth: string;
}

export enum BookStatus {
    AVAILABLE = 'AVAILABLE',
    BORROWED = 'BORROWED',
    RESERVED = 'RESERVED',
    MAINTENANCE = 'MAINTENANCE'
}

export enum LoanStatus {
    ACTIVE = 'ACTIVE',
    RETURNED = 'RETURNED',
    OVERDUE = 'OVERDUE'
}

export interface BookSearchFilters {
    title?: string;
    author?: string;
    genre?: string;
    status?: BookStatus;
    publicationYear?: number;
} 