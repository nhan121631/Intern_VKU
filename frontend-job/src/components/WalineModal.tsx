/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { useEffect, useRef } from "react";
import { init } from "@waline/client";
import "@waline/client/style";
import { useAuthStore } from "../stores/useAuthorStore";
import { X } from "lucide-react";
import { createClient } from "@supabase/supabase-js";

interface WalineModalProps {
  open: boolean;
  onClose: () => void;
  taskId: number | null;
}

const WalineModal: React.FC<WalineModalProps> = ({ open, onClose, taskId }) => {
  // Use relative URL to go through Vercel serverless proxy
  // This avoids 403 CORS/auth issues when calling Waline directly
  // Waline will append /api/comment to this URL
  const SERVER_URL = import.meta.env.DEV
    ? import.meta.env.VITE_WALINE_SERVER_URL
    : ""; // In production, use relative path (Waline adds /api/comment)

  const SUPABASE_URL = import.meta.env.VITE_SUPABASE_URL;
  const SUPABASE_ANON_KEY = import.meta.env.VITE_SUPABASE_ANON_KEY;

  const containerRef = useRef<HTMLDivElement | null>(null);
  const walineInstanceRef = useRef<any>(null);
  const supabaseRef = useRef<any>(null);
  const debounceTimerRef = useRef<number | null>(null);
  const loggedInUser = useAuthStore((state) => state.loggedInUser);

  const isDark =
    typeof document !== "undefined" &&
    (document.documentElement.classList.contains("dark") ||
      window.matchMedia("(prefers-color-scheme: dark)").matches);

  useEffect(() => {
    if (open && taskId && containerRef.current) {
      if (walineInstanceRef.current) {
        walineInstanceRef.current.destroy();
      }

      walineInstanceRef.current = init({
        el: containerRef.current,
        serverURL: SERVER_URL,
        path: `/task/${taskId}`,
        lang: "en",
        locale: {
          placeholder: "Enter detailed discussion content here...",
        },
        meta: ["nick", "mail"],
        dark: Boolean(isDark),
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

      // Kết nối Supabase Realtime
      supabaseRef.current = createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

      console.log("Đang subscribe realtime cho path:", `/task/${taskId}`);
      const currentPath = `/task/${taskId}`;

      const channel = supabaseRef.current
        .channel(`comments-task-${taskId}`)
        .on(
          "postgres_changes",
          {
            event: "*",
            schema: "public",
            table: "wl_comment",
          },
          (payload: any) => {
            console.log("Comment event received:", payload);
            console.log("Payload new:", payload.new);
            console.log("Current path:", currentPath);

            if (payload.new && payload.new.url === currentPath) {
              console.log("✅ Comment mới cho task này!");

              // Clear timeout cũ nếu có
              if (debounceTimerRef.current) {
                window.clearTimeout(debounceTimerRef.current);
              }

              // Debounce 2s để Waline có thời gian clear form sau submit
              debounceTimerRef.current = window.setTimeout(() => {
                if (walineInstanceRef.current) {
                  console.log("Re-init Waline do có comment mới");
                  walineInstanceRef.current.destroy();
                  walineInstanceRef.current = null;

                  // Re-init Waline
                  walineInstanceRef.current = init({
                    el: containerRef.current,
                    serverURL: SERVER_URL,
                    path: `/task/${taskId}`,
                    lang: "en",
                    locale: {
                      placeholder: "Enter detailed discussion content here...",
                    },
                    meta: ["nick", "mail"],
                    dark: Boolean(isDark),
                    emoji: [
                      "//unpkg.com/@waline/emojis@1.1.0/weibo",
                      "//unpkg.com/@waline/emojis@1.1.0/bilibili",
                    ],
                  });

                  // Auto-fill user lại nếu cần
                  if (loggedInUser) {
                    setTimeout(() => {
                      const nickInput = containerRef.current?.querySelector(
                        'input[name="nick"]',
                      ) as HTMLInputElement;
                      const mailInput = containerRef.current?.querySelector(
                        'input[name="mail"]',
                      ) as HTMLInputElement;

                      if (nickInput)
                        nickInput.value = loggedInUser.username || "";
                      if (mailInput) mailInput.value = loggedInUser.email || "";
                    }, 500);
                  }
                }
              }, 2000);
            } else {
              console.log("Comment không phải cho task này, bỏ qua");
            }
          },
        )
        .subscribe((status: string, err?: any) => {
          console.log("Realtime channel status:", status);
          if (err) console.error("Realtime subscribe error:", err);
        });

      return () => {
        if (debounceTimerRef.current) {
          window.clearTimeout(debounceTimerRef.current);
        }
        if (walineInstanceRef.current) {
          walineInstanceRef.current.destroy();
        }
        if (supabaseRef.current) {
          supabaseRef.current.removeChannel(channel);
        }
      };
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, taskId, SERVER_URL, loggedInUser]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      <div className="relative bg-white dark:bg-slate-900 w-full max-w-5xl h-[90vh] rounded-2xl shadow-2xl flex flex-col overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 dark:border-slate-700 bg-gray-50 dark:bg-slate-800 z-10 shrink-0">
          <h3 className="text-xl font-bold text-gray-800 dark:text-gray-100">
            Discussion (Task #{taskId})
          </h3>
          <button
            onClick={onClose}
            className="text-gray-500 dark:text-gray-300 hover:text-red-600 hover:bg-gray-200 dark:hover:bg-slate-700 rounded-full p-2 transition-colors cursor-pointer"
          >
            <X size={20} />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto bg-white dark:bg-slate-900 text-gray-800 dark:text-gray-100 p-6 md:p-8">
          <div ref={containerRef} className="waline-container" />
        </div>
      </div>
    </div>
  );
};

export default WalineModal;
