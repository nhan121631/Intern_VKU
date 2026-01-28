/* eslint-disable @typescript-eslint/no-explicit-any */
import { useState } from "react";
import { useNavigate } from "react-router";
import Notification from "../components/Notification";
import RegisterForm from "../components/RegisterForm";
import InputOTPModal from "../components/InputOTPModal";
import { register, verifyEmail } from "../service/AuthService";

export default function RegisterPage() {
  const navigate = useNavigate();
  const [notif, setNotif] = useState<{
    message: string;
    type: "success" | "error";
  } | null>(null);

  async function handleRegister(creds: {
    fullName: string;
    username: string;
    email: string;
    password: string;
  }) {
    try {
      await register(creds);
      // show OTP modal and keep email for verification
      setPendingEmail(creds.email);
      setShowOtp(true);
    } catch (error: any) {
      Promise.resolve().then(() => {
        let errorMessage = "Register failed";
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
  const [pendingEmail, setPendingEmail] = useState<string | null>(null);
  const [isVerifying, setIsVerifying] = useState(false);

  const handleVerify = async (code: string) => {
    if (!pendingEmail) return;
    console.log(pendingEmail, code);

    setIsVerifying(true);
    try {
      await verifyEmail({ email: pendingEmail, otp: code });
      setShowOtp(false);
      setNotif({ message: "Email verified. Please login.", type: "success" });
      setTimeout(() => navigate("/login"), 700);
    } catch (e: any) {
      console.log("Verify email error:", e);
      setNotif({
        message: e.message || e.errors?.[0] || "Verification failed",
        type: "error",
      });
    } finally {
      setIsVerifying(false);
    }
  };

  return (
    <div className="min-h-[70vh] flex items-center justify-center p-6 from-sky-50 to-white dark:from-gray-900 dark:to-gray-800 bg-linear-to-b">
      <div className="w-105 p-7 bg-white rounded-xl shadow-lg border border-gray-100 dark:bg-gray-800 dark:border-gray-700">
        <h2 className="text-xl font-semibold text-gray-900 mb-4 text-center dark:text-white">
          Register
        </h2>
        <RegisterForm onSubmit={handleRegister} />
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
