/* eslint-disable @typescript-eslint/no-explicit-any */
import { useState } from "react";
import TaskListContainer from "../components/TaskListContainer";
import CreateTaskForm from "../components/CreateTaskForm";
import type { Task } from "../types/type";
import { exportTasks } from "../service/TaskService";
import { CheckCircle, Loader2, XCircle } from "lucide-react";

export default function OurTaskPage() {
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [createdTask, setCreatedTask] = useState<Task | null>(null);
  const [exportLoading, setExportLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // export to excel
  const handleExport = async () => {
    try {
      setExportLoading(true);
      setErrorMessage(null);
      setSuccessMessage(null);
      const blob = await exportTasks();
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `tasks_export.xlsx`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      setSuccessMessage("Tasks exported successfully!");
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (error: any) {
      console.error("Export failed:", error);
      setErrorMessage("Failed to export tasks. " + (error.message || ""));
      setTimeout(() => setErrorMessage(null), 5000);
    } finally {
      setExportLoading(false);
    }
  };
  return (
    <div className="p-6">
      {/* Success Message */}
      {successMessage && (
        <div className="fixed top-4 right-4 z-50 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg flex items-center gap-2">
          <CheckCircle className="w-5 h-5" />
          {successMessage}
        </div>
      )}

      {/* Error Message */}
      {errorMessage && (
        <div className="fixed top-4 right-4 z-50 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg flex items-center gap-2">
          <XCircle className="w-5 h-5" />
          {errorMessage}
        </div>
      )}

      <div className="mb-6 flex justify-between items-center">
        <h1 className="text-2xl font-bold mb-4">Our Task Page</h1>
        <div className="flex space-x-4">
          <button
            onClick={() => setIsCreateOpen(true)}
            className="bg-blue-500 dark:bg-blue-700 text-white px-4 py-2 rounded hover:bg-blue-600 dark:hover:bg-blue-800 cursor-pointer"
          >
            Create New Task
          </button>

          <button
            className={`px-4 py-2 rounded text-white transition-colors ${
              exportLoading
                ? "bg-gray-400 cursor-not-allowed"
                : "bg-green-500 hover:bg-green-600 dark:bg-green-700 dark:hover:bg-green-800 cursor-pointer"
            }`}
            onClick={handleExport}
            disabled={exportLoading}
          >
            {exportLoading ? (
              <span className="flex items-center gap-2">
                <Loader2 className="w-4 h-4 animate-spin" />
                Exporting...
              </span>
            ) : (
              "Export Tasks"
            )}
          </button>
        </div>
      </div>

      <TaskListContainer newTask={createdTask} />

      {isCreateOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div
            className="fixed inset-0 bg-black/50 backdrop-blur-sm"
            onClick={() => setIsCreateOpen(false)}
          />

          <div
            className="relative w-full max-w-3xl max-h-[90vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <CreateTaskForm
              onClose={() => setIsCreateOpen(false)}
              onCreated={(t) => setCreatedTask(t)}
            />
          </div>
        </div>
      )}
    </div>
  );
}
