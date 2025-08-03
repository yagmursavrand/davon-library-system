export interface Author {
  id: number;
  name: string;
  bio?: string;
}

export enum BookStatus {
  AVAILABLE = 'AVAILABLE',
  BORROWED = 'BORROWED',
  RESERVED = 'RESERVED',
  MAINTENANCE = 'MAINTENANCE',
  LOST = 'LOST',
}

export interface Book {
  id: number;
  title: string;
  isbn: string;
  publicationYear: number;
  genre: string;
  status: BookStatus;
  authors: Author[];
  borrowerName?: string; 
}
