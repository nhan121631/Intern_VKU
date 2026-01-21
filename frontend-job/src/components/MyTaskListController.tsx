/* eslint-disable @typescript-eslint/no-explicit-any */
import { useEffect, useState } from "react";

import { deleteTask, getTaskById } from "../service/TaskService";
import { useAuthStore } from "../stores/useAuthorStore";
import type { Task } from "../types/type";
import EditTaskModal from "./EditTaskModal";
import Notification from "./Notification";
import TaskList from "./TaskList";
import {
  getMyTasks,
  getMyTaskStatus,
  searchMyTasks,
  updateTaskByUser,
} from "../service/MyTaskService";

type Filters = {
  status?: string;
  userId?: number | null;
  createdFrom?: string;
  createdTo?: string;
};
const MyTaskListContainer = () => {
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

  const loggedInUser = useAuthStore((state) => state.loggedInUser);
  const userId = Number(loggedInUser?.id ?? NaN);
  const fetchTasks = async () => {
    // don't attempt to fetch until we have a logged in user id
    if (!userId) return;
    // compatibility wrapper - actual loading done by `loadPage`
    await loadPage();
  };

  const [mode, setMode] = useState<"list" | "search" | "filter">("list");
  const [lastQuery, setLastQuery] = useState<string>("");
  const [lastStatus, setLastStatus] = useState<string>("");
  const [lastCreatedFrom, setLastCreatedFrom] = useState<string>("");
  const [lastCreatedTo, setLastCreatedTo] = useState<string>("");
  const [sortBy, setSortBy] = useState<string>("");
  const [order, setOrder] = useState<"asc" | "desc">("asc");

  const loadPage = async () => {
    if (!userId) return;
    setLoading(true);
    setLoadError(null);
    try {
      let res: any;
      if (mode === "search") {
        res = await searchMyTasks(page, size, lastQuery, userId, sortBy, order);
      } else if (mode === "filter") {
        res = await getMyTaskStatus({
          page,
          size,
          status: lastStatus,
          createAtFrom: lastCreatedFrom,
          createAtTo: lastCreatedTo,
          sortBy,
          order,
        });
      } else {
        res = await getMyTasks(page, size, userId, sortBy, order);
      }

      if (res && Array.isArray(res.data)) {
        setTasks(res.data as Task[]);
        setHasNext(Boolean(res.hasNext));
        setHasPrevious(Boolean(res.hasPrevious));
        setTotalPages(
          Number(res.totalPages ?? Math.ceil((res.totalElements || 0) / size)),
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
  }, [
    page,
    size,
    mode,
    lastQuery,
    lastStatus,
    loggedInUser?.id,
    lastCreatedFrom,
    lastCreatedTo,
    sortBy,
    order,
  ]);

  const handledDelete = async (taskId: number) => {
    try {
      // Call delete API
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
    setLastQuery(query);
    setMode("search");
    setPage(0);
  };

  const handleFilters = async (filters: Filters) => {
    if (
      (filters.status === "all" || !filters.status) &&
      !filters.userId &&
      !filters.createdFrom &&
      !filters.createdTo
    ) {
      setMode("list");
      setLastStatus("");
      setLastCreatedFrom("");
      setLastCreatedTo("");
      setPage(0);
      return;
    }

    setMode("filter");
    setPage(0);

    // Status
    if (filters.status && filters.status !== "all") {
      setLastStatus(filters.status);
    } else {
      setLastStatus("");
    }
    // Created From
    if (filters.createdFrom) {
      console.log("Set createdFrom to:", filters.createdFrom);
      setLastCreatedFrom(filters.createdFrom);
    } else {
      setLastCreatedFrom("");
    }
    // Created To
    if (filters.createdTo) {
      console.log("Set createdTo to:", filters.createdTo);
      setLastCreatedTo(filters.createdTo);
    } else {
      setLastCreatedTo("");
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
      const res: any = await updateTaskByUser(updated);
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
        prev.map((t) => (t.id === updated.id ? updatedTask : t)),
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
  const handleSort = (newSortBy: string, newOrder: "asc" | "desc") => {
    setSortBy(newSortBy);
    setOrder(newOrder);
  };

  return (
    <>
      <Notification
        message={errorMessage ?? ""}
        type="error"
        duration={5000}
        onClose={() => setErrorMessage(null)}
      />
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
        onSort={handleSort}
      />
      <EditTaskModal
        isOpen={isEditOpen}
        task={selectedTask}
        saving={saving}
        userId={userId}
        onClose={handleCloseEdit}
        onSave={handleSaveEdit}
      />
    </>
  );
};

export default MyTaskListContainer;
