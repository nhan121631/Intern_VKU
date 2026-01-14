/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { useEffect, useState } from "react";
import type { Task, UserFullName } from "../types/type";
import ConfirmModal from "./ConfirmModal";
import TaskHistoryModal from "./TaskHistoryModal";
import { ArrowDown, ArrowUp, ChevronsUpDown, HistoryIcon } from "lucide-react";
import { getUserFullName } from "../service/UserService";

interface TaskListProps {
  tasks: Task[];
  page: number;
  size: number;
  totalPages: number;
  loading: boolean;
  error: string | null;
  isOurTask?: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
  onPageChange: (page: number) => void;
  onSizeChange: (size: number) => void;
  onDelete: (taskId: number) => void;
  onFilters: (filters: Filters) => void;
  onSearch: (query: string) => void;
  onEdit: (taskId: number) => void;
  onRetry: () => void;
  onSort?: (sortBy: string, order: "asc" | "desc") => void;
}
type Filters = {
  status?: string;
  userId?: number | null;
};
export const TaskList: React.FC<TaskListProps> = ({
  tasks,
  page,
  size,
  totalPages,
  loading,
  error,
  hasNext,
  hasPrevious,
  isOurTask = false,
  onDelete,
  onPageChange,
  onSizeChange,
  onFilters,
  onEdit,
  onSearch,
  onRetry,
  onSort,
}) => {
  const [dataSearch, setDataSearch] = useState<string>("");
  const [filterStatus, setFilterStatus] = useState<string>("all");
  const [filterUserId, setFilterUserId] = useState<number | null>(null);
  const [historyOpen, setHistoryOpen] = useState<boolean>(false);
  const [historyData, setHistoryData] = useState<any[]>([]);

  function parseToDate(s?: string) {
    if (!s) return null;
    const m = s.match(/^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})(?:\.(\d+))?(Z)?/);
    if (!m) {
      const d = new Date(s);
      return isNaN(d.getTime()) ? null : d;
    }
    const base = m[1];
    const frac = (m[2] || "").substring(0, 3).padEnd(3, "0");
    const tz = m[3] || "";
    const iso = `${base}.${frac}${tz}`;
    const d = new Date(iso);
    return isNaN(d.getTime()) ? null : d;
  }

  function pad(n: number) {
    return n.toString().padStart(2, "0");
  }

  function formatDate(s?: string) {
    const d = parseToDate(s);
    if (!d) return "";
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  }
  const handleDelete = (taskId: number) => {
    setDeleteTarget(taskId);
  };
  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);

  const doConfirmDelete = () => {
    if (deleteTarget !== null) onDelete(deleteTarget);
    setDeleteTarget(null);
  };

  const handleSearch = () => {
    onSearch(dataSearch);
    setFilterStatus("all");
  };
  const handleFilters = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const { name, value } = e.target;

    let newStatus = filterStatus;
    let newUserId = filterUserId;

    if (name === "status") {
      newStatus = value;
      setFilterStatus(value);
    }

    if (name === "userId") {
      newUserId = value ? Number(value) : null;
      setFilterUserId(newUserId);
    }

    setDataSearch("");

    onFilters({
      status: newStatus === "all" ? undefined : newStatus,
      userId: newUserId,
    });
  };

  const handleEdit = (taskId: number) => {
    onEdit(taskId);
  };
  const [sortBy, setSortBy] = useState<
    "id" | "title" | "createdAt" | "deadline"
  >("id");
  const [order, setOrder] = useState<"asc" | "desc">("asc");
  const handleSort = (newSortBy: string, newOrder: "asc" | "desc") => {
    setSortBy(newSortBy as any);
    setOrder(newOrder);
    if (onSort) {
      onSort(newSortBy, newOrder);
    }
    console.log("Sorting by:", newSortBy, newOrder);
  };

  const [userFullNames, setUserFullNames] = useState<UserFullName[]>([]);
  useEffect(() => {
    if (isOurTask) {
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
  }, [isOurTask]);

  const cancelConfirmDelete = () => setDeleteTarget(null);
  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-semibold">Tasks</h2>
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2">
            <input
              type="text"
              placeholder="Search..."
              value={dataSearch}
              onChange={(e) => setDataSearch(e.target.value)}
              className="border rounded px-2 py-1"
            />
            <button
              className="px-3 py-1 bg-blue-500 text-white rounded"
              onClick={handleSearch}
            >
              Search
            </button>
          </div>

          <div className="flex items-center space-x-2">
            <label>Filters:</label>
            <select
              name="status"
              className="border rounded px-2 py-1"
              value={filterStatus}
              onChange={handleFilters}
            >
              <option value="all">All</option>
              <option value="OPEN">Open</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="DONE">Done</option>
              <option value="CANCELED">Canceled</option>
            </select>

            {isOurTask && (
              <select
                name="userId"
                className="border rounded px-2 py-1"
                value={filterUserId ?? ""}
                onChange={handleFilters}
              >
                <option value="">All Users</option>
                {userFullNames.map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.fullName}
                  </option>
                ))}
              </select>
            )}
          </div>
          <div className="flex items-center space-x-2">
            <label>Page size:</label>
            <select
              value={size}
              onChange={(e) => {
                onSizeChange(Number(e.target.value));
                onPageChange(0);
              }}
              className="border rounded px-2 py-1"
            >
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
            </select>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="relative">
          <div className="absolute inset-0 bg-white/80 flex items-center justify-center rounded z-10">
            <div className="flex flex-col items-center gap-2">
              <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
              <span className="text-sm text-gray-600">Loading...</span>
            </div>
          </div>
          <div className="h-40" />
        </div>
      ) : error ? (
        <div className="w-full flex items-center justify-center py-8">
          <div className="bg-red-50 border border-red-100 text-red-700 px-6 py-4 rounded-lg shadow-sm max-w-xl w-full">
            <div className="flex items-center gap-4">
              <div>
                <svg
                  className="w-8 h-8"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M12 9v4"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                  <path
                    d="M12 17h.01"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />
                  <circle
                    cx="12"
                    cy="12"
                    r="9"
                    stroke="currentColor"
                    strokeWidth="2"
                  />
                </svg>
              </div>
              <div className="flex-1">
                <div className="font-semibold">Failed to load tasks</div>
                <div className="text-sm mt-1">{String(error)}</div>
              </div>
              <div>
                <button
                  onClick={onRetry}
                  className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700"
                >
                  Retry
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : tasks.length === 0 ? (
        <div className="w-full flex items-center justify-center py-12">
          <div className="text-center text-gray-600">
            <svg
              className="mx-auto mb-4 w-16 h-16 text-gray-300"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M3 7h18"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
              <path
                d="M5 7v10a2 2 0 002 2h10a2 2 0 002-2V7"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
              <path
                d="M9 3v4"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
              <path
                d="M15 3v4"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
            <div className="text-lg font-semibold">No tasks yet</div>
            <div className="text-sm mt-2">
              There are currently no tasks to show.
            </div>
          </div>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full bg-white divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th
                  onClick={() => {
                    if (sortBy !== "id") {
                      handleSort("id", "asc");
                    } else {
                      handleSort("id", order === "asc" ? "desc" : "asc");
                    }
                  }}
                  className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer select-none"
                >
                  <div className="flex items-center gap-1">
                    ID
                    {sortBy === "id" ? (
                      order === "asc" ? (
                        <ArrowUp size={14} />
                      ) : (
                        <ArrowDown size={14} />
                      )
                    ) : (
                      <ChevronsUpDown size={14} className="opacity-40" />
                    )}
                  </div>
                </th>
                <th
                  onClick={() => {
                    if (sortBy !== "title") {
                      handleSort("title", "asc");
                    } else {
                      handleSort("title", order === "asc" ? "desc" : "asc");
                    }
                  }}
                  className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer select-none"
                >
                  <div className="flex items-center gap-1">
                    Title
                    {sortBy === "title" ? (
                      order === "asc" ? (
                        <ArrowUp size={14} />
                      ) : (
                        <ArrowDown size={14} />
                      )
                    ) : (
                      <ChevronsUpDown size={14} className="opacity-40" />
                    )}
                  </div>
                </th>

                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Assigned
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Descripton
                </th>

                <th
                  onClick={() => {
                    if (sortBy !== "createdAt") {
                      handleSort("createdAt", "asc");
                    } else {
                      handleSort("createdAt", order === "asc" ? "desc" : "asc");
                    }
                  }}
                  className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer select-none"
                >
                  <div className="flex items-center gap-1">
                    Created At
                    {sortBy === "createdAt" ? (
                      order === "asc" ? (
                        <ArrowUp size={14} />
                      ) : (
                        <ArrowDown size={14} />
                      )
                    ) : (
                      <ChevronsUpDown size={14} className="opacity-40" />
                    )}
                  </div>
                </th>
                <th
                  onClick={() => {
                    if (sortBy !== "deadline") {
                      handleSort("deadline", "asc");
                    } else {
                      handleSort("deadline", order === "asc" ? "desc" : "asc");
                    }
                  }}
                  className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider cursor-pointer select-none"
                >
                  <div className="flex items-center gap-1">
                    Deadline
                    {sortBy === "deadline" ? (
                      order === "asc" ? (
                        <ArrowUp size={14} />
                      ) : (
                        <ArrowDown size={14} />
                      )
                    ) : (
                      <ChevronsUpDown size={14} className="opacity-40" />
                    )}
                  </div>
                </th>

                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-100">
              {tasks.map((t, idx) => (
                <tr
                  key={t.id}
                  className={
                    idx % 2 === 0
                      ? "bg-white hover:bg-gray-50"
                      : "bg-gray-50 hover:bg-gray-100"
                  }
                >
                  <td className="px-4 py-3 text-sm text-gray-700">{t.id}</td>
                  <td className="px-4 py-3 text-sm text-gray-800 font-medium">
                    {t.title}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-700">
                    {t.assignedFullName}
                  </td>
                  <td className="px-4 py-3 text-sm">
                    <span
                      className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-medium ${
                        t.status === "OPEN"
                          ? "bg-yellow-100 text-yellow-800"
                          : t.status === "IN_PROGRESS"
                          ? "bg-blue-100 text-blue-800"
                          : t.status === "DONE"
                          ? "bg-green-100 text-green-800"
                          : t.status === "CANCELED"
                          ? "bg-red-100 text-red-800"
                          : "bg-gray-100 text-gray-800"
                      }`}
                    >
                      {t.status === "OPEN"
                        ? "Open"
                        : t.status === "IN_PROGRESS"
                        ? "In Progress"
                        : t.status === "DONE"
                        ? "Done"
                        : t.status === "CANCELED"
                        ? "Canceled"
                        : t.status}
                    </span>
                  </td>
                  {/* line-clamp-3 */}
                  <td className="px-4 py-3 text-sm text-gray-700 max-w-xs">
                    {t.description}
                  </td>

                  <td className="px-4 py-3 text-sm text-gray-600">
                    {formatDate(t.createdAt)}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-700">
                    {formatDate(t.deadline)}
                  </td>
                  <td className="px-4 py-3 text-sm flex space-x-2">
                    <button
                      className={`bg-blue-600 hover:bg-blue-700 text-white px-4 py-1.5 rounded-full text-sm shadow-sm transition cursor-pointer ${
                        !t.allowUserUpdate && !isOurTask
                          ? "opacity-50 cursor-not-allowed"
                          : ""
                      }`}
                      onClick={() => handleEdit(t.id)}
                      disabled={!t.allowUserUpdate && !isOurTask}
                    >
                      Edit
                    </button>
                    <button
                      className="bg-red-600 hover:bg-red-700 text-white px-4 py-1.5 rounded-full text-sm shadow-sm transition cursor-pointer"
                      onClick={() => handleDelete(t.id)}
                    >
                      Delete
                    </button>
                    {isOurTask && (
                      <button
                        className="bg-green-600 hover:bg-green-700 text-white px-4 py-1.5 rounded-full text-sm shadow-sm transition cursor-pointer"
                        title="View History Update"
                        onClick={() => {
                          // use fake data for now (no API call)
                          setHistoryData([
                            {
                              id: 3,
                              updateBy: "Admin",
                              updatedAt: "2026-01-14T16:27:50.716439",
                            },
                            {
                              id: 5,
                              updateBy: "Admin",
                              updatedAt: "2026-01-14T16:58:26.680155",
                            },
                          ]);
                          setHistoryOpen(true);
                        }}
                      >
                        <HistoryIcon size={16} />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmModal
        open={deleteTarget !== null}
        title="Delete task"
        description="This action cannot be undone. Do you want to continue?"
        confirmLabel="Delete"
        cancelLabel="Cancel"
        onConfirm={doConfirmDelete}
        onCancel={cancelConfirmDelete}
      />

      <TaskHistoryModal
        open={historyOpen}
        onClose={() => setHistoryOpen(false)}
        history={historyData}
      />

      <div className="flex items-center justify-between mt-4">
        <div>
          Page {Math.min(page + 1, totalPages || 1)} of {totalPages || 1}
        </div>
        <div className="space-x-2">
          <button
            onClick={() => onPageChange(Math.max(0, page - 1))}
            disabled={page === 0 || !hasPrevious}
            className="px-3 py-1 bg-blue-500 text-white rounded disabled:opacity-50"
          >
            Previous
          </button>
          <button
            onClick={() => onPageChange(page + 1)}
            disabled={!hasNext}
            className="px-3 py-1 bg-blue-500 text-white rounded disabled:opacity-50"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};

export default TaskList;
