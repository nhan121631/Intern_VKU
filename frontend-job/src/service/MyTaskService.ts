import apiClient from "../lib/apt-client-sp";
import type { FilterTaskData, UpdateTaskData } from "../types/type";

// get all task
export async function getMyTasks(page: number = 0, size: number = 10, userId: string | number | undefined, sortBy: string, order: "asc" | "desc") {
  try {
    const res = await apiClient.get(`/tasks/by-user?page=${page}&size=${size}&userId=${userId}&sortBy=${sortBy}&order=${order}`);
    return res;
  } catch (e) {
    console.error("Error fetching tasks:", e);
    throw e; 
  }
}

// get filters status
export async function getMyTaskStatus(filterTaskData: FilterTaskData){
  try {
    const res = await apiClient.post(`/tasks/by-user/filter-status`, filterTaskData);
    return res;
  }
  catch (e) {
    console.error(`Error filtering tasks with status "${filterTaskData.status}":`, e);
    throw e;
  }
}
// get search task
export async function searchMyTasks(page: number = 0, size: number = 10, query: string, userId: string | number | undefined, sortBy: string, order: "asc" | "desc") {
  try {
    const res = await apiClient.get(`/tasks/by-user/search-title?title=${encodeURIComponent(query)}&page=${page}&size=${size}&userId=${userId}&sortBy=${sortBy}&order=${order}`);
    return res;
  } catch (e) {
    console.error(`Error searching tasks with query "${query}":`, e);
    throw e;
  }
}
// update task
export async function updateTaskByUser(taskData: UpdateTaskData) {
  try {
    const res = await apiClient.patch(`/tasks/by-user/update`, taskData);
    return res;
  }
  catch (e) {
    console.error(`Error updating task:`, e);
    throw e;
  }
}
