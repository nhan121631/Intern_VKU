/* eslint-disable @typescript-eslint/no-explicit-any */
import { useEffect, useState } from "react";
import { useForm, type SubmitHandler } from "react-hook-form";
import { getUserFullName } from "../service/UserService";
import type { UpdateTaskData, UserFullName } from "../types/type";
import * as yup from "yup";
import { yupResolver } from "@hookform/resolvers/yup";
import { AlertCircle, Check, Loader2, X } from "lucide-react";

const schema = yup
  .object({
    title: yup.string().required("Title is required"),
    description: yup.string().notRequired(),
    // Accept select value as string and transform to number
    assignedUserId: yup
      .number()
      .transform((_value, originalValue) => {
        if (
          originalValue === "" ||
          originalValue === undefined ||
          originalValue === null
        )
          return undefined;
        return Number(originalValue);
      })
      .typeError("Assignee must be a number")
      .required("Assignee is required"),
    allowUserUpdate: yup.boolean().notRequired(),
    status: yup.string().required(),
    deadline: yup.string().required("Deadline is required"),
  })
  .required();

type FormValues = yup.InferType<typeof schema>;

type TaskStatus = "OPEN" | "IN_PROGRESS" | "DONE" | "CANCELED" | string;

export interface Task {
  id: number;
  title: string;
  description?: string;
  assignedFullName?: string;
  assignedUserId?: number;
  allowUserUpdate?: boolean;
  status?: TaskStatus;
  deadline?: string | null;
  createdAt?: string;
}
type Props = {
  isOpen: boolean;
  userId?: number | string | undefined;
  task?: Task | null;
  saving?: boolean;
  onClose: () => void;
  onSave: (data: UpdateTaskData) => Promise<void> | void;
};

export default function EditTaskModal({
  isOpen,
  task,
  saving,
  userId,
  onClose,
  onSave,
}: Props) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: yupResolver(schema) as any,
    defaultValues: {
      title: "",
      description: "",
      assignedUserId: 0,
      allowUserUpdate: true,
      status: "OPEN",
      deadline: "",
    },
  });

  const [userFullNames, setUserFullNames] = useState<UserFullName[]>([]);

  // Disable assignee select only when a specific `userId` prop is provided
  // (used when creating a task for a fixed user). When editing an existing
  // task we allow reassigning by keeping the select enabled.
  const isAssigneeDisabled = Boolean(userId !== undefined);
  const selectedAssigneeName =
    task?.assignedFullName ||
    (userFullNames.find((u) => u.id === Number(task?.assignedUserId))
      ?.fullName ??
      "");

  useEffect(() => {
    if (task) {
      reset({
        title: task.title ?? "",
        description: task.description ?? "",
        assignedUserId: task.assignedUserId ?? 0,
        status: (task.status as TaskStatus) ?? "OPEN",
        allowUserUpdate: task.allowUserUpdate ?? true,
        deadline: task.deadline ? String(task.deadline).slice(0, 10) : "",
      });
    } else {
      reset({
        title: "",
        description: "",
        assignedUserId: 0,
        status: "OPEN",
        deadline: "",
      });
    }
  }, [task, reset]);

  const submit: SubmitHandler<FormValues> = async (data) => {
    if (!task) return;

    // Tìm fullName của user được chọn từ dropdown
    const selectedUser = userFullNames.find(
      (user) => user.id === Number(data.assignedUserId),
    );

    const payload: UpdateTaskData = {
      id: task.id,
      title: data.title,
      description: data.description ?? "",
      deadline: data.deadline,
      status: data.status ?? (task.status as string) ?? "OPEN",
      allowUserUpdate: data.allowUserUpdate ?? task.allowUserUpdate ?? true,
      assignedUserId: Number(data.assignedUserId),
      assignedFullName: selectedUser?.fullName,
    };

    await onSave(payload);
  };
  useEffect(() => {
    if (!userId) {
      const fetchUserFullNames = async () => {
        try {
          const res = await getUserFullName();
          setUserFullNames(Array.isArray(res) ? res : []);
        } catch (error) {
          console.error("Error fetching user full names:", error);
          setUserFullNames([]);
        }
      };

      fetchUserFullNames();
    }
  }, [userId]);

  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div
        className="fixed inset-0 bg-black/50 backdrop-blur-sm transition-opacity"
        onClick={() => {
          if (!saving) onClose();
        }}
      />

      <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <form onSubmit={handleSubmit(submit)} className="p-8">
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-2xl font-bold text-gray-800">Edit Task</h3>
            <button
              type="button"
              onClick={() => {
                if (!saving) onClose();
              }}
              className="text-gray-400 hover:text-gray-600 transition-colors cursor-pointer"
            >
              <X className="w-6 h-6" />
            </button>
          </div>

          <div className="mb-5">
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Title <span className="text-red-500">*</span>
            </label>
            <input
              {...register("title", { required: "Title is required" })}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
              placeholder="Enter task title"
            />
            {errors.title && (
              <p className="text-red-500 text-sm mt-1.5 flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {errors.title.message}
              </p>
            )}
          </div>

          <div className="mb-5">
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Description
            </label>
            <textarea
              {...register("description")}
              rows={4}
              className="w-full px-4 py-2.5 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all resize-none"
              placeholder="Enter task description (optional)"
            />
          </div>

          <div className="mb-5">
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Assigned To
            </label>
            {isAssigneeDisabled ? (
              <input
                readOnly
                value={selectedAssigneeName}
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg bg-gray-100 focus:outline-none transition-all"
              />
            ) : (
              <select
                {...register("assignedUserId")}
                className={`w-full px-4 py-2.5 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all cursor-pointer`}
              >
                <option value="">Select assignee...</option>
                {(userFullNames || []).map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.fullName}
                  </option>
                ))}
              </select>
            )}

            {errors.assignedUserId && (
              <p className="text-red-500 text-sm mt-1.5 flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {errors.assignedUserId.message}
              </p>
            )}
          </div>

          <div className="mb-6 grid grid-cols-1 md:grid-cols-2 gap-5">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Status
              </label>
              <select
                {...register("status")}
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all cursor-pointer"
              >
                <option value="OPEN">Open</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="DONE">Done</option>
                <option value="CANCELED">Canceled</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Deadline
              </label>
              <input
                type="date"
                {...register("deadline")}
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
              />
            </div>
          </div>
          {!userId && (
            <div className="mb-4">
              <label className="inline-flex items-center">
                <input
                  type="checkbox"
                  {...register("allowUserUpdate")}
                  className="form-checkbox h-5 w-5 text-blue-600"
                />
                <span className="ml-2 text-gray-700">
                  Allow assigned user to update the task
                </span>
              </label>
            </div>
          )}

          <div className="flex justify-end gap-3 pt-6 border-t border-gray-200">
            <button
              type="button"
              className="px-6 py-2.5 rounded-lg font-medium bg-gray-100 text-gray-700 hover:bg-gray-200 transition-colors cursor-pointer"
              onClick={() => {
                if (!saving) onClose();
              }}
              disabled={saving}
            >
              Cancel
            </button>

            <button
              type="submit"
              disabled={saving}
              className="px-6 py-2.5 rounded-lg font-medium bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-sm hover:shadow-md flex items-center gap-2 cursor-pointer"
            >
              {saving ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Saving...
                </>
              ) : (
                <>
                  <Check className="w-4 h-4" />
                  Save Changes
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
