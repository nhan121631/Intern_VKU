import apiClient from "../lib/apt-client-sp";

export async function register(creds: {
  fullName: string;
  username: string;
  email: string;
  password: string;
}) {
  try {
    const res = await apiClient.post("/auth/register", creds);
    return res;
  } catch (e) {
    console.error("Create register error:", e);
    throw e; 
  }
}

export async function verifyEmail(payload: { email: string; otp: string }) {
  try {
    const res = await apiClient.post("/auth/verify-email", payload);
    return res;
  } catch (e) {
    console.error("Verify email error:", e);
    throw e;
  }
}