/* eslint-disable @typescript-eslint/no-explicit-any */
import { useEffect, useState } from "react";
import {
  deleteTask,
  getTasks,
  getTaskStatus,
  searchTasks,
  getTaskById,
  updateTask,
} from "../service/TaskService";
import type { Task } from "../types/type";
import TaskList from "./TaskList";
import EditTaskModal from "./EditTaskModal";
import Notification from "./Notification";

const TaskListContainer = () => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(10);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [hasNext, setHasNext] = useState<boolean>(false);

  const [hasPrevious, setHasPrevious] = useState<boolean>(false);
  const [isEditOpen, setIsEditOpen] = useState<boolean>(false);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [saving, setSaving] = useState<boolean>(false);
  const [successMessage, setSuccessMessage] = useState<string>("");

  const fetchTasks = async () => {
    // kept for compatibility but actual loading now handled by `loadPage`
    await loadPage();
  };

  const [mode, setMode] = useState<"list" | "search" | "filter">("list");
  const [lastQuery, setLastQuery] = useState<string>("");
  const [lastStatus, setLastStatus] = useState<string>("");

  const loadPage = async () => {
    setLoading(true);
    setLoadError(null);
    try {
      let res: any;
      if (mode === "search") {
        res = await searchTasks(page, size, lastQuery);
      } else if (mode === "filter") {
        res = await getTaskStatus(page, size, lastStatus);
      } else {
        res = await getTasks(page, size);
      }
      console.log("Load page response:", { mode, page, size, res });

      if (res && Array.isArray(res.data)) {
        setTasks(res.data as Task[]);
        setHasNext(Boolean(res.hasNext));
        setHasPrevious(Boolean(res.hasPrevious));
        setTotalPages(
          Number(res.totalPages ?? Math.ceil((res.totalElements || 0) / size))
        );
      } else if (Array.isArray(res)) {
        setTasks(res as Task[]);
        setHasNext(false);
        setHasPrevious(false);
        setTotalPages(1);
      } else {
        setTasks([]);
      }
    } catch (e: any) {
      console.error(e);
      setLoadError(e?.message ?? "Error loading tasks");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPage();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size, mode, lastQuery, lastStatus]);

  const handledDelete = async (taskId: number) => {
    try {
      await deleteTask(taskId);
      setTasks((prevTasks) => prevTasks.filter((task) => task.id !== taskId));
      setSuccessMessage("Task deleted successfully!");
    } catch (e: any) {
      console.error(e);
      setLoadError(e?.message ?? "Error deleting task");
      return;
    }
  };
  const handleSearch = async (query: string) => {
    // set search mode and query, reset to first page; useEffect will load
    setLastQuery(query);
    setMode("search");
    setPage(0);
  };

  const handleFilters = async (status: string) => {
    if (status === "all") {
      setMode("list");
      setLastStatus("");
      setPage(0);
      return;
    }
    // set filter mode and status, reset to first page; useEffect will load
    setLastStatus(status);
    setMode("filter");
    setPage(0);
  };

  const handleEdit = async (taskId: number) => {
    try {
      const res: any = await getTaskById(taskId);
      const taskData: Task = res?.data ?? res;
      setSelectedTask(taskData);
      setIsEditOpen(true);
    } catch (e: any) {
      console.error("Error fetching task:", e);
      setLoadError(e?.message || e?.errors[1] || "Error fetching task");
      setIsEditOpen(false);
    }
  };

  const handleCloseEdit = () => {
    setIsEditOpen(false);
    setSelectedTask(null);
  };

  const handleSaveEdit = async (updated: any) => {
    setSaving(true);
    console.log("Saving updated task:", updated);
    try {
      const res: any = await updateTask(updated);
      if (res && (res.errors || res.error)) {
        const msg = Array.isArray(res.errors)
          ? res.errors.join(", ")
          : res.error || res.message || "Error saving task";
        console.error("Validation error updating task:", res);
        setErrorMessage(msg);
        return;
      }

      const updatedTask: Task = res?.data ?? res;

      // Update task in list with server data
      setTasks((prev) =>
        prev.map((t) => (t.id === updated.id ? updatedTask : t))
      );
      setIsEditOpen(false);
      setSelectedTask(null);
      setSuccessMessage("Task updated successfully!");
    } catch (e: any) {
      console.error("Error saving task:", e);
      // Axios/interceptor may forward error body directly (e.g. {errors: [...]})
      const message =
        e?.message ||
        (Array.isArray(e?.errors) ? e.errors.join(", ") : e?.error) ||
        "Error saving task";
      setErrorMessage(message);
      setIsEditOpen(false);
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <Notification
        message={errorMessage ?? ""}
        type="error"
        duration={3000}
        onClose={() => setErrorMessage(null)}
      />
      <Notification
        message={successMessage}
        type="success"
        duration={2000}
        onClose={() => setSuccessMessage("")}
      />
      <TaskList
        tasks={tasks}
        page={page}
        size={size}
        totalPages={totalPages}
        loading={loading}
        error={loadError}
        hasNext={hasNext}
        hasPrevious={hasPrevious}
        onDelete={handledDelete}
        onSearch={handleSearch}
        onFilters={handleFilters}
        onPageChange={setPage}
        onEdit={handleEdit}
        onSizeChange={setSize}
        onRetry={fetchTasks}
      />
      <EditTaskModal
        isOpen={isEditOpen}
        task={selectedTask}
        saving={saving}
        onClose={handleCloseEdit}
        onSave={handleSaveEdit}
      />
    </>
  );
};

export default TaskListContainer;
