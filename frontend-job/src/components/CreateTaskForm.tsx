/* eslint-disable @typescript-eslint/no-explicit-any */
import { yupResolver } from "@hookform/resolvers/yup";
import { useCallback, useEffect, useState } from "react";
import { useForm, type SubmitHandler } from "react-hook-form";
import { useNavigate } from "react-router";
import * as yup from "yup";
import { createTask } from "../service/TaskService";
import { getUserFullName } from "../service/UserService";
import Notification from "./Notification";
import type { UserFullName, Task } from "../types/type";
import { Check, Loader2 } from "lucide-react";
import { createTaskAssignedNotification } from "../service/Notification";

// Hoist schema outside component - it's static and doesn't need to be recreated on every render
const schema = yup
  .object({
    title: yup.string().required("Title is required"),
    description: yup.string().notRequired(),
    assignedUserId: yup
      .number()
      .transform((_v, o) => {
        if (o === "" || o === undefined || o === null) return undefined;
        return Number(o);
      })
      .typeError("Assignee must be a number")
      .required("Assignee is required"),
    status: yup.string().required("Status is required"),
    createdAt: yup.string().notRequired(),
    allowUserUpdate: yup.boolean().notRequired(),
    deadline: yup
      .string()
      .required("Deadline is required")
      .test(
        "is-today-or-future",
        "Deadline must be today or in the future",
        (value) => {
          if (!value) return false;
          try {
            const d = new Date(value);
            d.setHours(0, 0, 0, 0);
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            return d.getTime() >= today.getTime();
          } catch (e: any) {
            console.error(e);
            return false;
          }
        },
      ),
  })
  .required();

type FormValues = yup.InferType<typeof schema>;

// Hoist helper function outside component
const getTodayDate = () => new Date().toISOString().slice(0, 10);

export default function CreateTaskForm({
  onClose,
  onCreated,
}: {
  onClose?: () => void;
  onCreated?: (task: Task) => void;
}) {
  const navigate = useNavigate();
  const [userFullNames, setUserFullNames] = useState<UserFullName[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Use lazy initialization for today's date
  const [today] = useState(getTodayDate);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: yupResolver(schema) as any,
    defaultValues: {
      title: "",
      description: "",
      assignedUserId: 0,
      status: "OPEN",
      createdAt: today,
      deadline: "",
      allowUserUpdate: true,
    },
  });

  // Fetch user list on mount
  useEffect(() => {
    let isMounted = true;

    const fetchUsers = async () => {
      try {
        const res = await getUserFullName();
        if (isMounted) {
          setUserFullNames(Array.isArray(res) ? res : []);
        }
      } catch (e) {
        console.error(e);
      }
    };

    fetchUsers();
    return () => {
      isMounted = false;
    };
  }, []);

  // Memoize submit handler to prevent unnecessary re-renders
  const onSubmit: SubmitHandler<FormValues> = useCallback(
    async (data) => {
      setLoading(true);
      setError(null);

      try {
        const payload = {
          title: data.title,
          description: data.description ?? "",
          deadline: data.deadline,
          status: data.status,
          assignedUserId: Number(data.assignedUserId),
          allowUserUpdate: !!data.allowUserUpdate,
        };

        // Execute task creation and notification in parallel after we have the task ID
        const res: any = await createTask(payload);
        const created: Task = res?.data ?? res;

        // Fire notification (don't await - non-blocking)
        createTaskAssignedNotification(
          String(created.assignedUserId),
          String(created.id),
          `You have been assigned a new task: ${created.title}`,
        ).catch(console.error);

        setSuccess("Task created successfully");

        // Delay to show success message
        setTimeout(() => {
          if (onCreated) onCreated(created);
          if (onClose) onClose();
          else navigate("/our-task");
        }, 900);
      } catch (e: any) {
        console.error(e);
        setError(
          e?.message ||
            (e?.errors ? e.errors.join(", ") : "Error creating task"),
        );
      } finally {
        setLoading(false);
      }
    },
    [navigate, onClose, onCreated],
  );

  // Memoize cancel handler
  const handleCancel = useCallback(() => {
    if (onClose) {
      onClose();
    } else {
      navigate(-1);
    }
  }, [onClose, navigate]);

  return (
    <div className="max-w-2xl mx-auto p-6 bg-white rounded shadow dark:bg-gray-700 dark:text-gray-200">
      <h2 className="text-2xl font-semibold mb-4">Create Task</h2>
      {error && <div className="mb-4 text-red-600">{error}</div>}
      <form onSubmit={handleSubmit(onSubmit)}>
        <div className="mb-4 grid grid-cols-1 md:grid-cols-2 gap-4">
          <Notification
            message={success ?? ""}
            type="success"
            duration={900}
            onClose={() => setSuccess(null)}
          />
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1 dark:text-gray-300">
              Title <span className="text-red-500">*</span>
            </label>
            <input
              {...register("title")}
              placeholder="Enter task title"
              className="w-full border border-gray-200 rounded-md px-4 py-2 bg-white dark:bg-gray-800 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-200"
            />
            {errors.title && (
              <p className="text-red-500 text-sm mt-1">
                {errors.title.message}
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1 dark:text-gray-300">
              Created At
            </label>
            <input
              type="date"
              {...register("createdAt")}
              disabled
              className="w-full border border-gray-200 rounded-md px-4 py-2 bg-gray-50 dark:bg-gray-600 dark:text-gray-200 cursor-not-allowed"
            />
          </div>
        </div>

        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1 dark:text-gray-300">
            Description
          </label>
          <textarea
            {...register("description")}
            placeholder="This is a short description of the task (optional)"
            className="w-full border border-gray-200 rounded-md px-4 py-3 min-h-30 bg-white dark:bg-gray-800 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-200"
          />
        </div>

        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1 dark:text-gray-300 ">
            Assigned To
          </label>
          <select
            {...register("assignedUserId")}
            className="w-full border border-gray-200 rounded-md px-4 py-2 bg-white dark:bg-gray-800 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-200"
          >
            <option value="">Select assignee...</option>
            {userFullNames.map((u) => (
              <option key={u.id} value={u.id}>
                {u.fullName}
              </option>
            ))}
          </select>
          {errors.assignedUserId && (
            <p className="text-red-500 text-sm">
              {errors.assignedUserId.message}
            </p>
          )}
        </div>

        <div className="mb-4 grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1 dark:text-gray-300">
              Status
            </label>
            <select
              {...register("status")}
              className="w-full border border-gray-200 rounded-md px-4 py-2 bg-white dark:bg-gray-800 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-200"
            >
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="DONE">Done</option>
              <option value="CANCELED">Canceled</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1 dark:text-gray-300">
              Deadline
            </label>
            <input
              type="date"
              {...register("deadline")}
              className="w-full border border-gray-200 rounded-md px-4 py-2 bg-white dark:bg-gray-800 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-blue-200"
            />
            {errors.deadline && (
              <p className="text-red-500 text-sm mt-1">
                {errors.deadline.message}
              </p>
            )}
          </div>
        </div>
        <div className="mb-4">
          <label className="inline-flex items-center">
            <input
              type="checkbox"
              {...register("allowUserUpdate")}
              className="form-checkbox h-5 w-5 text-blue-600"
            />
            <span className="ml-2 text-gray-700 dark:text-gray-300">
              Allow assigned user to update the task
            </span>
          </label>
        </div>

        <hr className="my-4 border-gray-200" />

        <div className="flex justify-end items-center space-x-3">
          <button
            type="button"
            onClick={handleCancel}
            className="px-4 py-2 rounded-md bg-white border border-gray-200 text-gray-700 hover:bg-gray-50 cursor-pointer"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={loading}
            className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 cursor-pointer"
          >
            {loading ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Check className="w-4 h-4" />
            )}
            {loading ? "Creating..." : "Create Task"}
          </button>
        </div>
      </form>
    </div>
  );
}
