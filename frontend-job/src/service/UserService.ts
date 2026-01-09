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