/* eslint-disable @typescript-eslint/no-explicit-any */
import { useEffect, useState } from "react";
import { deleteTask, getTasks } from "../service/TaskService";
import type { Task } from "../types/type";
import TaskList from "./TaskList";

const TaskListContainer = () => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(10);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [hasNext, setHasNext] = useState<boolean>(false);
  const [hasPrevious, setHasPrevious] = useState<boolean>(false);

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
    } catch (e: any) {
      console.error(e);
      setError(e?.message ?? "Error deleting task");
      return;
    }
  };

  return (
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
      onPageChange={setPage}
      onSizeChange={setSize}
      onRetry={fetchTasks}
    />
  );
};

export default TaskListContainer;
