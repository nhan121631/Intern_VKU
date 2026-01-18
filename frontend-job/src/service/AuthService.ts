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

// send OTP to email for password reset
export async function sendResetPasswordOTP(email: string) {
  try {
    const res = await apiClient.post(`/auth/forgot-password?email=${encodeURIComponent(email)}`);
    return res.data || res;
  } catch (e) {
    console.error("Send reset password OTP error:", e);
    throw e;
  }
}
// check OTP and reset password
export async function checkResetPasswordOTP(payload: {
  email: string;
  otp: string;
}) {
  try {
    const res = await apiClient.post("/auth/check-reset-password-otp", payload);
    return res.data || res;
  }
  catch (e) {
    console.error("Check reset password OTP error:", e);
    throw e;
  }
}
// reset password
export async function setNewPassword(payload: {
  email: string;
  otp: string;
  newPassword: string;
}) {
  try {
    const res = await apiClient.post("/auth/reset-password", payload);
    return res.data || res;
  }
  catch (e) {
    console.error("Set new password error:", e);
    throw e;
  }
}