import apiClient from "../lib/apt-client-sp";
import type { UpdateTaskData } from "../types/type";

// get all task
export async function getTasks(page: number = 0, size: number = 10) {
  try {
    const res = await apiClient.get(`/tasks?page=${page}&size=${size}`);
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
export async function searchTasks(page: number = 0, size: number = 10, query: string) {
  try {
    const res = await apiClient.get(`/tasks/search-by-title?title=${encodeURIComponent(query)}&page=${page}&size=${size}`);
    return res; 
  } catch (e) {
    console.error(`Error searching tasks with query "${query}":`, e);
    throw e;
  }
}
// get filters status
export async function getTaskStatus(page: number = 0, size: number = 10, status: string){
  try {
    const res = await apiClient.get(`/tasks/filter-by-status?status=${encodeURIComponent(status)}&page=${page}&size=${size}`);
    return res;
  }
  catch (e) {
    console.error(`Error filtering tasks with status "${status}":`, e);
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

// create task
export async function createTask(taskData: Omit<UpdateTaskData, 'id' | 'assignedFullName'>) {
  try {
    const res = await apiClient.post(`/tasks`, taskData);
    return res;
  } catch (e) {
    console.error(`Error creating task:`, e);
    throw e;
  }
}


