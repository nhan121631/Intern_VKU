/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { useEffect, useRef } from "react";
import { init } from "@waline/client";
import "@waline/client/style";
import { useAuthStore } from "../stores/useAuthorStore";
import { X } from "lucide-react";
interface WalineModalProps {
  open: boolean;
  onClose: () => void;
  taskId: number | null;
}

const WalineModal: React.FC<WalineModalProps> = ({ open, onClose, taskId }) => {
  const SERVER_URL = import.meta.env.VITE_WALINE_SERVER_URL;

  const containerRef = useRef<HTMLDivElement | null>(null);
  const walineInstanceRef = useRef<any>(null);
  const loggedInUser = useAuthStore((state) => state.loggedInUser);

  useEffect(() => {
    if (open && taskId && containerRef.current) {
      if (walineInstanceRef.current) {
        walineInstanceRef.current.destroy();
      }

      // 1. Khởi tạo Waline
      walineInstanceRef.current = init({
        el: containerRef.current,
        serverURL: SERVER_URL,
        path: `/task/${taskId}`,
        lang: "en",
        locale: {
          placeholder: "Enter detailed discussion content here...",
        },
        meta: ["nick", "mail"],
        dark: false,
        emoji: [
          "//unpkg.com/@waline/emojis@1.1.0/weibo",
          "//unpkg.com/@waline/emojis@1.1.0/bilibili",
        ],
      });

      if (loggedInUser) {
        setTimeout(() => {
          const nickInput = containerRef.current?.querySelector(
            'input[name="nick"]',
          ) as HTMLInputElement;
          const mailInput = containerRef.current?.querySelector(
            'input[name="mail"]',
          ) as HTMLInputElement;

          if (nickInput) {
            nickInput.value = loggedInUser.username || "";
            nickInput.dispatchEvent(new Event("input", { bubbles: true }));
          }

          if (mailInput) {
            mailInput.value = loggedInUser.email || "";
            mailInput.dispatchEvent(new Event("input", { bubbles: true }));
          }
        }, 500);
      }
    }

    return () => {
      if (walineInstanceRef.current) {
        walineInstanceRef.current.destroy();
      }
    };
  }, [open, taskId, SERVER_URL, loggedInUser]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      <div className="relative bg-white w-full max-w-5xl h-[90vh] rounded-2xl shadow-2xl flex flex-col overflow-hidden animate-in fade-in zoom-in duration-200">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 bg-gray-50 z-10 shrink-0">
          <h3 className="text-xl font-bold text-gray-800">
            Discussion (Task #{taskId})
          </h3>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-red-600 hover:bg-gray-200 rounded-full p-2 transition-colors cursor-pointer"
          >
            <X size={20} />
          </button>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto bg-white p-6 md:p-8">
          {/* Container chứa Waline */}
          <div ref={containerRef} className="waline-container" />
        </div>
      </div>
    </div>
  );
};

export default WalineModal;
