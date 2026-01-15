import apiClient from "../lib/apt-client-sp";
import type { CreateTaskData, UpdateTaskData } from "../types/type";

// get all task
export async function getTasks(page: number = 0, size: number = 10, sortBy: string = "id", order: string = "asc") {
  try {
    const res = await apiClient.get(`/tasks?page=${page}&size=${size}&sortBy=${sortBy}&order=${order}`);
    return res;
  } catch (e) {
    console.error("Error fetching tasks:", e);
    throw e; 
  }
}


// delete task by id
export async function deleteTask(taskId: number) {
  try {
    const res = await apiClient.delete(`/tasks?id=${taskId}`);
    return res;
  } catch (e) {
    console.error(`Error deleting task with id ${taskId}:`, e);
    throw e; 
  }
}

// get search task
export async function searchTasks(page: number = 0, size: number = 10, query: string, sortBy: string = "id", order: string = "asc") {
  try {
    const res = await apiClient.get(`/tasks/search-by-title?title=${encodeURIComponent(query)}&page=${page}&size=${size}&sortBy=${sortBy}&order=${order}`);
    return res; 
  } catch (e) {
    console.error(`Error searching tasks with query "${query}":`, e);
    throw e;
  }
}
// get filters status
export async function getTaskStatus(
  page: number = 0,
  size: number = 10,
  status?: string,
  userId?: number | null,
  sortBy: string = "id",
  order: string = "asc"
) {
  try {
    const params = new URLSearchParams();

    params.append("page", page.toString());
    params.append("size", size.toString());
    params.append("sortBy", sortBy);
    params.append("order", order);
    console.log("status" + status);
    if (status) {
      params.append("status", status);
    }

    if (userId !== null && userId !== undefined) {
      params.append("userId", userId.toString());
    }

    const res = await apiClient.get(`/tasks/filter-by-status?${params.toString()}`);
    return res;
  } catch (e) {
    console.error("Error filtering tasks:", e);
    throw e;
  }
}

// get task by id
export async function getTaskById(taskId: number) {
  try {
    const res = await apiClient.get(`/tasks/get-by-id?id=${taskId}`);
    return res;
  }
  catch (e) {
    console.error(`Error fetching task with id ${taskId}:`, e);
    throw e;
  }
}

// update task
export async function updateTask(taskData: UpdateTaskData) {
  try {
    const res = await apiClient.patch(`/tasks`, taskData);
    return res;
  }
  catch (e) {
    console.error(`Error updating task:`, e);
    throw e;
  }
}

// create task Omit<UpdateTaskData, 'id' | 'assignedFullName'>
export async function createTask(taskData: CreateTaskData) {
  try {
    const res = await apiClient.post(`/tasks`, taskData);
    return res;
  } catch (e) {
    console.error(`Error creating task:`, e);
    throw e;
  }
}

// export task to excel
export async function exportTasks() {
  try {
    const res = await apiClient.get("/tasks/export", {
      responseType: "blob",
    });
    return res.data || res;
  } catch (e) {
    console.error("Error exporting tasks:", e);
    throw e;
  }
}

// export task to excel by user id
export async function exportTasksByUserId() {
  try {
    const res = await apiClient.get(`/tasks/export-by-user`, {
      responseType: "blob",
    });
    return res.data || res;
  } catch (e) {
    console.error("Error exporting tasks by user id:", e);
    throw e;
  } 
}
