import { useEffect, useState } from "react";
import LoginForm from "../components/LoginForm";
import Notification from "../components/Notification";
import { useAuthStore } from "../stores/useAuthorStore";
import { useNavigate } from "react-router";

export default function LoginPage() {
  const { login, loginWithGoogle, error, loggedInUser, loading } = useAuthStore(
    (state) => state
  );
  const navigate = useNavigate();
  const [notif, setNotif] = useState<{
    message: string;
    type: "success" | "error";
  } | null>(null);

  // Clear error and loading state on mount
  useEffect(() => {
    useAuthStore.setState({ error: null, loading: false });
  }, []);

  useEffect(() => {
    if (loggedInUser) {
      Promise.resolve().then(() =>
        setNotif({ message: "Login successful", type: "success" })
      );
      const t = setTimeout(() => navigate("/home"), 300);
      return () => {
        clearTimeout(t);
      };
    }
  }, [loggedInUser, navigate]);

  useEffect(() => {
    if (error) {
      Promise.resolve().then(() => {
        setNotif({ message: error.errors, type: "error" });
      });
    }
  }, [error]);

  function handleLogin(creds: { username: string; password: string }) {
    login({
      username: creds.username,
      password: creds.password,
      navigate: navigate,
    });
  }

  function handleLoginWithGoogle(credential: string) {
    loginWithGoogle({
      credential,
      navigate,
    });
  }

  return (
    <div className="min-h-[70vh] flex items-center justify-center p-6 from-sky-50 to-white">
      <div className="w-105  p-7 bg-white rounded-xl shadow-lg border border-gray-100 relative">
        {loading && (
          <div className="absolute inset-0 bg-white/80 flex items-center justify-center rounded-xl z-10">
            <div className="flex flex-col items-center gap-2">
              <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
              <span className="text-sm text-gray-600">Loading...</span>
            </div>
          </div>
        )}
        <h2 className="text-xl font-semibold text-gray-900 mb-4 text-center">
          Login
        </h2>
        <LoginForm
          onSubmit={handleLogin}
          loginWithGoogle={handleLoginWithGoogle}
        />
      </div>
      {notif && (
        <Notification
          message={notif.message}
          type={notif.type}
          onClose={() => setNotif(null)}
        />
      )}
    </div>
  );
}
