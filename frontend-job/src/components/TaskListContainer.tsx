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
  const [error, setError] = useState<string | null>(null);
  const [hasNext, setHasNext] = useState<boolean>(false);

  const [hasPrevious, setHasPrevious] = useState<boolean>(false);
  const [isEditOpen, setIsEditOpen] = useState<boolean>(false);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [saving, setSaving] = useState<boolean>(false);
  const [successMessage, setSuccessMessage] = useState<string>("");

  const fetchTasks = async () => {
    setLoading(true);
    setError(null);
    try {
      const res: any = await getTasks(page, size);
      console.log("Fetched tasks response:", res);

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
      setError(e?.message ?? "Error fetching tasks");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTasks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size]);

  const handledDelete = async (taskId: number) => {
    try {
      // Call delete API
      await deleteTask(taskId);
      setTasks((prevTasks) => prevTasks.filter((task) => task.id !== taskId));
      setSuccessMessage("Task deleted successfully!");
    } catch (e: any) {
      console.error(e);
      setError(e?.message ?? "Error deleting task");
      return;
    }
  };
  const handleSearch = async (query: string) => {
    setLoading(true);
    setError(null);
    try {
      const res: any = await searchTasks(0, size, query);
      console.log("Search tasks response:", res);
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
      setError(e?.message ?? "Error searching tasks");
    } finally {
      setLoading(false);
    }
  };

  const handleFilters = async (status: string) => {
    if (status === "all") {
      fetchTasks();
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res: any = await getTaskStatus(0, size, status);
      console.log("Filter tasks response:", res);
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
      setError(e?.message ?? "Error filtering tasks");
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = async (taskId: number) => {
    try {
      const res: any = await getTaskById(taskId);
      const taskData: Task = res?.data ?? res;
      setSelectedTask(taskData);
      setIsEditOpen(true);
    } catch (e: any) {
      console.error("Error fetching task:", e);
      setError(e?.message ?? "Error fetching task");
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
      const updatedTask: Task = res?.data ?? res;

      // Cập nhật task trong list với dữ liệu từ server
      setTasks((prev) =>
        prev.map((t) => (t.id === updated.id ? updatedTask : t))
      );
      setIsEditOpen(false);
      setSelectedTask(null);
      setSuccessMessage("Task updated successfully!");
    } catch (e: any) {
      console.error("Error saving task:", e);
      setError(e?.message ?? "Error saving task");
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <Notification
        message={successMessage}
        type="success"
        duration={3000}
        onClose={() => setSuccessMessage("")}
      />
      <TaskList
        tasks={tasks}
        page={page}
        size={size}
        totalPages={totalPages}
        loading={loading}
        error={error}
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
