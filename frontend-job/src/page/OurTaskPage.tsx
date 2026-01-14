import { useState } from "react";
import TaskListContainer from "../components/TaskListContainer";
import CreateTaskForm from "../components/CreateTaskForm";
import type { Task } from "../types/type";

export default function OurTaskPage() {
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [createdTask, setCreatedTask] = useState<Task | null>(null);

  return (
    <div className="p-6">
      <div className="mb-6 flex justify-between items-center">
        <h1 className="text-2xl font-bold mb-4">Our Task Page</h1>
        <button
          onClick={() => setIsCreateOpen(true)}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 cursor-pointer"
        >
          Create New Task
        </button>
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
