import apiClient from "../lib/apt-client-sp";
import type { TaskHistoryResponse } from "../types/type";

export async function getTaskHistory(taskId: number) {
  try {
    const res = await apiClient.get(`/task-histories/by-task-id?taskId=${taskId}`) as TaskHistoryResponse[]; 
    return res;
  } catch (e) {
    console.error(`Error fetching history for task with id ${taskId}:`, e);
    throw e;
  }
}

export async function getDetailHistory(id: number) {
    try {
        const res = await apiClient.get(`/task-histories/detail-by-id?id=${id}`) as TaskHistoryResponse;
        return res;
    } catch (e) {
        console.error(`Error fetching detail history with id ${id}:`, e);
        throw e;
    }
}