/* eslint-disable @typescript-eslint/no-explicit-any */
import { useState } from "react";
import MyTaskListContainer from "../components/MyTaskListController";
import { exportTasksByUserId } from "../service/TaskService";
import { CheckCircle, Loader2, XCircle } from "lucide-react";

export default function MyTaskPage() {
  const [exportLoading, setExportLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  // export to excel
  const handleExport = async () => {
    try {
      setExportLoading(true);
      setErrorMessage(null);
      setSuccessMessage(null);
      const blob = await exportTasksByUserId();
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
        <h1 className="text-2xl font-bold mb-4">My Task Page</h1>
        <button
          className={`px-4 py-2 rounded text-white transition-colors ${
            exportLoading
              ? "bg-gray-400 cursor-not-allowed"
              : "bg-green-500 hover:bg-green-600 cursor-pointer"
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

      <MyTaskListContainer />
    </div>
  );
}
