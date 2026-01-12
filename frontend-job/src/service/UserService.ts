import apiClient from "../lib/apt-client-sp";

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
    // send params as query parameters (axios: use third arg config)
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