
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

export interface PaginatedResponse<T> {
    data: T[]
    hasNext: boolean,
    hasPrevious: boolean,
    page: number,
    size: number,
    totalElements: number,
    totalPages: number
}