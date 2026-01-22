import apiClient from "../lib/apt-client-sp";

export async function getTaskStatistics(createdAtFrom?: string, createdAtTo?: string) {
  try {
    const res = await apiClient.get(`/statistics/summary-by-status`, {
      params: { createdAtFrom, createdAtTo }
    });
    return res.data || res;
  } catch (e) {
    console.error("Error fetching task statistics:", e);
    throw e; 
  } 
}

export async function getTasksByUser(createdAtFrom?: string, createdAtTo?: string) {
  try {
    const res = await apiClient.get(`/statistics/summary-by-user`, {
      params: { createdAtFrom, createdAtTo }
    });
    return res.data || res;
  } catch (e) {
    console.error("Error fetching tasks by user statistics:", e);
    throw e; 
  } 
}

export async function getTasksByUserId(userId: string, createdAtFrom?: string, createdAtTo?: string) {
  try {
    const res = await apiClient.get(`/statistics/summary-by-user-id`, {
      params: { userId, createdAtFrom, createdAtTo }
    });
    return res.data || res;
  } catch (e) {
    console.error("Error fetching tasks by user ID statistics:", e);
    throw e;
  }
}

export async function getTaskMe(createdAtFrom?: string, createdAtTo?: string) {
  try {
    const res = await apiClient.get(`/statistics/summary-me`, {
      params: { createdAtFrom, createdAtTo }
    });
    return res.data || res;
  } catch (e) {
    console.error("Error fetching my task statistics:", e);
    throw e; 
  }
}