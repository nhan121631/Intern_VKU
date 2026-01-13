
export interface Task {
    assignedUserId: number;
    createdAt:   string;
    deadline: string;
    assignedFullName: string;
    description: string;
    id: number;
    status: string;
    title: string;
}

export interface UserFullName{
    id: number;
    fullName: string;
}

export interface UpdateTaskData 
    {
    id: number;
    title: string;
    description: string;
    deadline: string;
    status: string;
    assignedUserId: number;
    assignedFullName?: string;
}

export interface CreateTaskData 
    {
    title: string;
    description: string;
    deadline: string;
    assignedUserId: number;
    assignedFullName?: string;
}

export interface User {
    id: number;
    username: string;
    fullName: string;
    createdAt?: string | null;
    isActive: number;
}

// name user 
export interface NameUserResponse {
    fullName: string | null;
}

// register response
export interface RegisterResponse {
    message: string;
    email: string;
    success: boolean;
}

// paginated response
export interface PaginatedResponse<T> {
    data: T[]
    hasNext: boolean,
    hasPrevious: boolean,
    page: number,
    size: number,
    totalElements: number,
    totalPages: number
}