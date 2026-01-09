import apiClient from "../lib/apt-client-sp";

export async function register(creds: {
  fullName: string;
  username: string;
  password: string;
}) {
  try {
    const res = await apiClient.post("/auth/register", creds);
    return res;
  } catch (e) {
    console.error("Create register error:", e);
    throw e; // Re-throw to let caller handle the error
  }
}