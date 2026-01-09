import apiClient from "../lib/apt-client-sp";

// get all task
export async function getTasks(page: number = 0, size: number = 10) {
  try {
    // Use GET for listing tasks. POST /tasks is typically the create endpoint
    // and requires a request body which caused `Required request body is missing` errors.
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