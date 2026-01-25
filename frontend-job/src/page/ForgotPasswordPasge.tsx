/* eslint-disable @typescript-eslint/no-explicit-any */
import { useState } from "react";
import { useNavigate } from "react-router";
import ForgotPassForm from "../components/ForgotPassForm";
import InputOTPModal from "../components/InputOTPModal";
import Notification from "../components/Notification";
import {
  checkResetPasswordOTP,
  sendResetPasswordOTP,
  setNewPassword,
} from "../service/AuthService";
import ResetPasswordForm from "../components/ResetPasswordForm";

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [notif, setNotif] = useState<{
    message: string;
    type: "success" | "error";
  } | null>(null);

  async function handleForgotPassword(email: string) {
    try {
      // show OTP modal and keep email for verification
      await sendResetPasswordOTP(email);
      console.log("Forgot password email:", email);
      setPendingEmail(email);
      setShowOtp(true);
    } catch (error: any) {
      Promise.resolve().then(() => {
        let errorMessage = "Forgot password failed";
        if (
          error.message &&
          Array.isArray(error.message) &&
          error.message.length > 0
        ) {
          errorMessage = error.message[0];
        } else if (error.message && typeof error.message === "string") {
          errorMessage = error.message;
        } else if (typeof error === "string") {
          errorMessage = error;
        }

        setNotif({ message: errorMessage, type: "error" });
      });
    }
  }

  const [showOtp, setShowOtp] = useState(false);
  const [otpCode, setOtpCode] = useState("");
  const [pendingEmail, setPendingEmail] = useState<string | null>(null);
  const [optIsTrue, setOtpIsTrue] = useState(false);
  const [isVerifying, setIsVerifying] = useState(false);

  const handleVerify = async (code: string) => {
    if (!pendingEmail) return;
    console.log(pendingEmail, code);

    setIsVerifying(true);
    try {
      await checkResetPasswordOTP({ email: pendingEmail, otp: code });
      setShowOtp(false);
      setOtpIsTrue(true);
      setOtpCode(code);
      setNotif({
        message: "OTP verified. Please reset your password.",
        type: "success",
      });
      //   setTimeout(() => navigate("/login"), 700);
    } catch (e: any) {
      console.log("Verify email error:", e);
      setOtpIsTrue(false);
      setNotif({
        message: e.message || e.errors?.[0] || "Verification failed",
        type: "error",
      });
    } finally {
      setIsVerifying(false);
    }
  };
  const handleResetPassword = async (data: { newPassword: string }) => {
    if (!pendingEmail) return;
    try {
      await setNewPassword({
        email: pendingEmail,
        otp: otpCode,
        newPassword: data.newPassword,
      });
      setNotif({
        message: "Password reset successful. Please login.",
        type: "success",
      });
      setTimeout(() => navigate("/login"), 700);
    } catch (e: any) {
      console.log("Reset password error:", e);
      setNotif({
        message: e.message || e.errors?.[0] || "Reset password failed",
        type: "error",
      });
    }
  };

  return (
    <div className="min-h-[70vh] flex items-center justify-center p-6 from-sky-50 to-white">
      <div className="w-105 p-7 bg-white rounded-xl shadow-lg border border-gray-100">
        <h2 className="text-xl font-semibold text-gray-900 mb-4 text-center">
          Forgot Password
        </h2>
        {!optIsTrue && <ForgotPassForm onSubmit={handleForgotPassword} />}
        {optIsTrue && <ResetPasswordForm onSubmit={handleResetPassword} />}
      </div>
      {notif && (
        <Notification
          message={notif.message}
          type={notif.type}
          onClose={() => setNotif(null)}
        />
      )}
      <InputOTPModal
        open={showOtp}
        onClose={() => setShowOtp(false)}
        onSubmit={handleVerify}
        isLoading={isVerifying}
      />
    </div>
  );
}
