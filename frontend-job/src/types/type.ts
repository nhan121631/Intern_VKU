// task type
export interface Task {
    assignedUserId: number;
    createdAt:   string;
    deadline: string;
    assignedFullName: string;
    allowUserUpdate: boolean;
    description: string;
    id: number;
    status: string;
    title: string;
}

// user full name type
export interface UserFullName{
    id: number;
    fullName: string;
}

// update task type
export interface UpdateTaskData 
    {
    id: number;
    title: string;
    description: string;
    deadline: string;
    allowUserUpdate: boolean;
    status: string;
    assignedUserId: number;
    assignedFullName?: string;
}

// create task type
export interface CreateTaskData 
    {
    title: string;
    description: string;
    deadline: string;
    assignedUserId: number;
    assignedFullName?: string;
    allowUserUpdate: boolean;
}

// user type
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

// task history response
export interface TaskHistoryResponse{
    id: number;
    updateBy?: string;
    updatedByName?: string;
    updatedAt: string;
    newData?: string; // JSON string of new data
    oldData?: string; // JSON string of old data
    roles?: string[];
}

// user profile type
export interface UserProfile {
    id: number;
    avatarUrl: string;
    fullName: string;
    phoneNumber: string;
    address: string;
}

// update user profile type
export interface UpdateUserProfileData {
    id: number;
    fullName: string;
    phoneNumber?: string;
    address?: string;

}