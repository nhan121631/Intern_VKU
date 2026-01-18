import { useEffect } from "react";

type Props = {
  message: string;
  type?: "success" | "error" | "info";
  duration?: number;
  onClose?: () => void;
};

export default function Notification({
  message,
  type = "success",
  duration = 1000,
  onClose,
}: Props) {
  useEffect(() => {
    if (!message) return;
    const t = setTimeout(() => onClose && onClose(), duration);
    return () => clearTimeout(t);
  }, [message, duration, onClose]);

  if (!message) return null;

  const bg =
    type === "success"
      ? "bg-emerald-600"
      : type === "error"
      ? "bg-rose-600"
      : "bg-blue-600";

  return (
    <div className="fixed top-4 left-1/2 transform -translate-x-1/2 z-50 pointer-events-none">
      <div
        className={`pointer-events-auto px-5 py-3 rounded-lg text-white shadow-lg ${bg} max-w-[90%] sm:max-w-md`}
      >
        {message}
      </div>
    </div>
  );
}
