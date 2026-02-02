/* eslint-disable @typescript-eslint/no-explicit-any */
import { Calendar, MessagesSquare, Trash2, User, X } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { GEMINI_URL } from "../service/Constant";
import type { Task } from "./EditTaskModal";
import { useAuthStore } from "../stores/useAuthorStore";

interface ChatReply {
  isJsonArray: boolean;
  reply: string | Task[];
}
function MessageContent({ text }: { text: string | Task[] }) {
  if (typeof text === "string") {
    return <div className="whitespace-pre-line">{text}</div>;
  }

  // text is Task[]
  if (!Array.isArray(text) || text.length === 0) {
    return <div className="text-gray-500">Không có dữ liệu hiển thị</div>;
  }

  return (
    <div className="space-y-2">
      {text.map((task) => (
        <div
          key={task.id}
          className="p-3 border border-blue-200 rounded-lg bg-linear-to-br from-white to-blue-50 shadow-sm hover:shadow-md transition-shadow
          dark:from-gray-800 dark:to-gray-700 dark:border-gray-600"
        >
          <div className="flex items-start justify-between mb-2 gap-2">
            <div className="font-semibold text-gray-900 dark:text-white line-clamp-2 flex-1">
              {task.title}
            </div>
            <span
              className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                task.status === "OPEN"
                  ? "bg-yellow-100 text-yellow-800"
                  : task.status === "IN_PROGRESS"
                    ? "bg-blue-100 text-blue-800"
                    : task.status === "COMPLETED"
                      ? "bg-green-100 text-green-800"
                      : "bg-red-100 text-red-800"
              }`}
            >
              {task.status}
            </span>
          </div>
          <div className="text-sm text-gray-600 mb-2 line-clamp-2 dark:text-gray-300">
            {task.description}
          </div>
          <div className="flex flex-col gap-y-2 text-xs text-gray-500 dark:text-gray-400">
            <div className="flex items-center gap-1">
              <User className="w-4 h-4 text-blue-500" />
              <span>{task.assignedFullName || "Chưa phân công"}</span>
            </div>
            <div className="flex gap-x-0.5 items-center">
              <Calendar className="w-4 h-4 text-blue-500" />

              <div className="flex items-center gap-1">
                <span>Create At</span>
                <span>
                  {task.createdAt
                    ? new Date(task.createdAt).toLocaleDateString("en-US")
                    : "N/A"}
                </span>
              </div>
            </div>
            <div className="flex gap-x-0.5 items-center">
              <Calendar className="w-4 h-4 text-blue-500" />

              <div className="flex items-center gap-1">
                <span>Deadline</span>
                <span>
                  {task.deadline
                    ? new Date(task.deadline).toLocaleDateString("en-US")
                    : "N/A"}
                </span>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

function ChatAI() {
  const [open, setOpen] = useState(false);
  const [history, setHistory] = useState<
    Array<{ role: string; text: string | Task[] }>
  >(() => {
    const saved = sessionStorage.getItem("chatHistory");
    return saved ? JSON.parse(saved) : [];
  });
  const [showTyping, setShowTyping] = useState(false);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const access_token = useAuthStore((state) => state.access_token);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    sessionStorage.setItem("chatHistory", JSON.stringify(history));
    scrollToBottom();
  }, [history]);

  useEffect(() => {
    if (showTyping) {
      scrollToBottom();
    }
  }, [showTyping]);

  const sendMessage = async () => {
    if (!input.trim()) return;
    const userMessage = { role: "user", text: input.trim() };
    const newHistory = [...history, userMessage];
    setHistory(newHistory);
    setInput("");
    setShowTyping(true);
    setLoading(true);
    try {
      // Convert history to text-only format for backend
      const textHistory = newHistory.map((msg) => ({
        role: msg.role,
        text: typeof msg.text === "string" ? msg.text : "[Task List]",
      }));

      const response = await fetch(`${GEMINI_URL}/api/ai_chatbot`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${access_token}`,
        },
        body: JSON.stringify({ message: input.trim(), history: textHistory }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(
          `${response.status} ${response.statusText}: ${errorText}`,
        );
      }

      const data: ChatReply = await response.json();
      const botMessage = {
        role: "assistant",
        text: data.reply,
      };
      setHistory((prev) => [...prev, botMessage]);
    } catch (error) {
      console.error("Error fetching AI response:", error);
      const errorMessage = {
        role: "assistant",
        text: `Lỗi: ${error instanceof Error ? error.message : "Không thể kết nối với AI. Vui lòng thử lại sau."}`,
      };
      setHistory((prev) => [...prev, errorMessage]);
    } finally {
      setShowTyping(false);
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <>
      <button
        onClick={() => setOpen(true)}
        className={`fixed cursor-pointer bottom-8 right-6 z-50 rounded-full w-16 h-16 bg-linear-to-br bg-white shadow-xl flex items-center justify-center hover:scale-105 hover:shadow-2xl transition-transform duration-300 border-4 border-white/60 ${
          open
            ? "scale-0 opacity-0 pointer-events-none"
            : "scale-100 opacity-100"
        }`}
        aria-label="Open Ants AI Assistant"
      >
        <img
          src="./../assets/chat-ai.avif"
          alt="AI Assistant"
          className="w-10 h-10"
        />
      </button>

      {/* Chat Modal */}
      {open && (
        <div
          className={`fixed bottom-28 right-8 z-50 bg-white border border-gray-200 rounded-xl shadow-2xl max-w-sm w-full h-130 flex flex-col transition-all duration-300 scale-100 opacity-100
            dark:bg-gray-800 dark:border-gray-700`}
        >
          {/* Header */}
          <div
            className="px-4 py-3 border-b border-gray-200 bg-blue-600 text-white rounded-t-xl flex justify-between items-center
          dark:bg-gray-900 dark:border-gray-700"
          >
            <span className="font-semibold flex gap-2 items-center ">
              <MessagesSquare />
              AI Assistant
            </span>
            <div className="flex gap-2">
              <button
                onClick={() => {
                  setHistory([]);
                  sessionStorage.removeItem("chatHistory");
                }}
                className="text-white/80 hover:text-white cursor-pointer text-xl px-2 py-1 rounded hover:bg-white/10 transition dark:hover:bg-white/20"
                title="Clear chat"
              >
                <Trash2 />
              </button>
              <button
                onClick={() => setOpen(false)}
                className="text-white text-2xl font-bold cursor-pointer hover:text-gray-200 w-8 h-8 flex items-center justify-center dark:hover:bg-white/20 rounded transition"
              >
                <X />
              </button>
            </div>
          </div>

          {/* Messages Area */}
          <div className="flex-1 overflow-y-auto p-4 bg-gray-50 space-y-4 dark:bg-gray-700">
            {history.map((msg: any, idx: number) => (
              <div
                key={idx}
                className={`flex ${
                  msg.role === "user" ? "justify-end" : "justify-start"
                }`}
              >
                {/* Assistant Avatar */}
                {msg.role === "assistant" && (
                  <div className="flex items-end mr-2">
                    <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
                      <img
                        src="./../assets/chat-ai.avif"
                        alt="Bot"
                        width={20}
                        height={20}
                        className="rounded-full"
                      />
                    </div>
                  </div>
                )}

                {/* Message Bubble */}
                <div
                  className={`inline-block rounded-2xl px-4 py-3 max-w-[85%] shadow-sm ${
                    msg.role === "user"
                      ? "bg-blue-600 text-white rounded-br-md"
                      : "bg-white text-gray-800 border border-gray-200 rounded-bl-md dark:bg-gray-800 dark:text-gray-200 dark:border-gray-700"
                  } text-sm leading-relaxed`}
                >
                  <MessageContent text={msg.text} />
                </div>

                {/* User spacing */}
                {msg.role === "user" && <div className="w-2" />}
              </div>
            ))}

            {/* Typing Indicator */}
            {showTyping && (
              <div className="flex justify-start">
                <div className="flex items-end mr-2">
                  <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center dark:bg-gray-600">
                    <img
                      src="./../assets/chat-ai.avif"
                      alt="Bot"
                      width={20}
                      height={20}
                      className="rounded-full"
                    />
                  </div>
                </div>
                <div className="inline-block rounded-2xl rounded-bl-md px-4 py-3 bg-white border border-gray-200 shadow-sm dark:bg-gray-800 dark:border-gray-700  ">
                  <div className="flex space-x-1">
                    <div
                      className="w-2 h-2 bg-gray-500 rounded-full animate-bounce"
                      style={{ animationDelay: "0ms" }}
                    ></div>
                    <div
                      className="w-2 h-2 bg-gray-500 rounded-full animate-bounce"
                      style={{ animationDelay: "150ms" }}
                    ></div>
                    <div
                      className="w-2 h-2 bg-gray-500 rounded-full animate-bounce"
                      style={{ animationDelay: "300ms" }}
                    ></div>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>
          {/* Input Area */}
          <div className="border-t border-gray-200 p-3 bg-white rounded-b-xl dark:border-gray-600 dark:bg-gray-800 dark:text-white">
            <div className="flex gap-2">
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Enter your message..."
                className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-transparent"
                disabled={loading}
              />
              <button
                onClick={sendMessage}
                disabled={loading || !input.trim()}
                className="rounded-lg bg-blue-600 text-white px-4 py-2 text-sm font-semibold hover:bg-blue-700 transition disabled:opacity-50 cursor-pointer disabled:cursor-not-allowed"
              >
                {loading ? "..." : "Send"}
              </button>
            </div>
            <div className="text-xs text-gray-500 mt-2 text-center">
              <p>Question about finding tasks</p>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default ChatAI;
