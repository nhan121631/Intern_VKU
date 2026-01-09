
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

export interface PaginatedResponse<T> {
    data: T[]
    hasNext: boolean,
    hasPrevious: boolean,
    page: number,
    size: number,
    totalElements: number,
    totalPages: number
}