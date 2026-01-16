import apiClient from "../lib/apt-client-sp";
import type { NameUserResponse } from "../types/type";

// get task by id
export async function getUserFullName() {
  try {
    const res = await apiClient.get(`/users/get-name`);
    return res;
  }
  catch (e) {
    console.error(`Error fetching user full names:`, e);
    throw e;
  }
}

// get paginated users
export async function getUsersPaginated(page: number, size: number) {
  try {
    const res = await apiClient.get(`/users/get-all`, {
      params: {
        page,
        size,
      },
    });
    return res;
  }
  catch (e) {
    console.error(`Error fetching paginated users:`, e);
    throw e;
  }
}

// change user status
export async function changeUserStatus(userId: number, isActive: number) {
  try {
    const res = await apiClient.patch(`/users/change-status`, null, {
      params: {
        userId,
        isActive,
      },
    });
    return res;
  }
  catch (e) {
    console.error(`Error changing user status:`, e);
    throw e;
  }
}

// get name user
export async function getNameUser(): Promise<NameUserResponse> {
  try {
    const res = await apiClient.get<NameUserResponse>("/users/get-name-by-id");
    return res.data || res;
  } catch (e) {
    console.error("Error fetching name user:", e);
    throw e;
  }
}

// change user password
export async function changeUserPassword(oldPassword: string, newPassword: string) {
  try {
    const res = await apiClient.patch(`/users/change-password?oldPassword=${oldPassword}&newPassword=${newPassword}`);
    return res;
  }
  catch (e) {
    console.error(`Error changing user password:`, e);
    throw e;
  }
}