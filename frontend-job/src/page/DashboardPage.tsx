/* eslint-disable @typescript-eslint/no-explicit-any */
import { useEffect, useState } from "react";
import {
  BarChart,
  Bar,
  PieChart,
  Pie,
  Tooltip,
  XAxis,
  YAxis,
  ResponsiveContainer,
  Cell,
} from "recharts";
import type { TaskForUser, TaskSummary, UserFullName } from "../types/type";
import {
  getTasksByUser,
  getTasksByUserId,
  getTaskStatistics,
} from "../service/StatisticsService";
import { Loader2 } from "lucide-react";
import { getUserFullName } from "../service/UserService";

/* ================== CONSTANTS ================== */
const STATUS_COLORS: Record<string, string> = {
  OPEN: "#facc15",
  IN_PROGRESS: "#3b82f6",
  DONE: "#22c55e",
  CANCELED: "#ef4444",
};

/* ================== PAGE ================== */
export default function DashboardPage() {
  const [userId, setUserId] = useState<number>(1);
  const [users, setUsers] = useState<UserFullName[]>([]);
  const [taskSummary, setTaskSummary] = useState<TaskSummary[]>([]);
  const [userDetail, setUserDetail] = useState<Record<number, TaskSummary[]>>(
    {},
  );
  const [tasksByUser, setTasksByUser] = useState<TaskForUser[]>([]);
  const [loadingStats, setLoadingStats] = useState<boolean>(true);
  const [loadingUserDetail, setLoadingUserDetail] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [createdAtFrom, setCreatedAtFrom] = useState<string>("");
  const [createdAtTo, setCreatedAtTo] = useState<string>("");

  useEffect(() => {
    const shouldFetchUnfiltered = !createdAtFrom && !createdAtTo;
    const oneDateMissing =
      (createdAtFrom && !createdAtTo) || (!createdAtFrom && createdAtTo);

    if (oneDateMissing) {
      setError(null);
      return;
    }

    if (!shouldFetchUnfiltered) {
      if (createdAtFrom > createdAtTo) {
        setError("'Created From' must not be after 'Created To'.");
        return;
      }
      setError(null);
    }

    const fetchStatistics = async () => {
      try {
        setLoadingStats(true);
        const from = shouldFetchUnfiltered ? "" : createdAtFrom;
        const to = shouldFetchUnfiltered ? "" : createdAtTo;

        const res = await getTaskStatistics(from, to);
        setTaskSummary(res);
        const resByUser = await getTasksByUser(from, to);
        setTasksByUser(resByUser);
      } catch (e: any) {
        setError("Failed to load task statistics.");
        setError(e.message || e.errors?.[0] || "Unknown error");
      } finally {
        setLoadingStats(false);
      }
    };

    fetchStatistics();
  }, [createdAtFrom, createdAtTo]);

  // Fetch users for the dropdown
  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoadingUserDetail(true);
        const res = await getUserFullName();
        setUsers(res);
      } catch (e: any) {
        setError("Failed to load users.");
        setError(e.message || e.errors[0] || "Unknown error");
      } finally {
        setLoadingUserDetail(false);
      }
    };
    fetchUsers();
  }, []);

  // Fetch user detail when userId or date range changes
  useEffect(() => {
    const shouldFetchUnfiltered = !createdAtFrom && !createdAtTo;
    const oneDateMissing =
      (createdAtFrom && !createdAtTo) || (!createdAtFrom && createdAtTo);

    if (oneDateMissing) {
      // Wait until both dates are provided; clear any previous error.
      setError(null);
      return;
    }

    if (!shouldFetchUnfiltered) {
      // Both dates provided -> validate order
      if (createdAtFrom > createdAtTo) {
        setError("'Created From' must not be after 'Created To'.");
        return;
      }
      setError(null);
    }

    const fetchUserDetail = async () => {
      try {
        setLoadingUserDetail(true);
        const from = shouldFetchUnfiltered ? "" : createdAtFrom;
        const to = shouldFetchUnfiltered ? "" : createdAtTo;
        const res = await getTasksByUserId(userId.toString(), from, to);
        setUserDetail((prev) => ({ ...prev, [userId]: res }));
      } catch (e: any) {
        setError("Failed to load user detail.");
        setError(e.message || e.errors?.[0] || "Unknown error");
      } finally {
        setLoadingUserDetail(false);
      }
    };

    fetchUserDetail();
  }, [userId, createdAtFrom, createdAtTo]);

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    if (name === "createdAtFrom") {
      setCreatedAtFrom(value);
    }
    if (name === "createdAtTo") {
      setCreatedAtTo(value);
    }
  };

  // NOTE: We no longer block the whole page while stats are loading.
  // Individual sections show their own spinners (`loadingStats`, `loadingUserDetail`).
  if (error) {
    return (
      <div className="flex items-center justify-center p-6 text-red-600">
        Error: {error}
      </div>
    );
  }

  // Custom tick renderer for XAxis to support rotation and avoid TS type errors
  const CustomizedAxisTick = (props: any) => {
    const { x, y, payload } = props;
    return (
      <g transform={`translate(${x},${y})`}>
        <text
          x={0}
          y={0}
          dy={16}
          textAnchor="end"
          fontSize={12}
          transform="rotate(-30)"
        >
          {payload.value}
        </text>
      </g>
    );
  };

  return (
    <div className="p-6 space-y-8">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">Dashboard</h1>

        {/* created from and to */}
        <div className="flex gap-4">
          {/* Future enhancement: Date range picker for filtering statistics */}
          <div className="flex gap-2 items-center justify-center">
            <label className="text-gray-700 font-semibold">Created From:</label>
            <input
              type="date"
              name="createdAtFrom"
              value={createdAtFrom}
              onChange={handleDateChange}
              max={createdAtTo || undefined}
              className="border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div className="flex gap-2 items-center justify-center">
            <label className="text-gray-700 font-semibold">Created To:</label>
            <input
              type="date"
              name="createdAtTo"
              value={createdAtTo}
              onChange={handleDateChange}
              min={createdAtFrom || undefined}
              className="border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
      </div>
      {/* ================= USER DETAIL & SUMMARY (side-by-side) ================= */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* -------- Pie chart (Tasks by Status) -------- */}
        <div className="bg-white rounded-xl p-5 shadow">
          <h2 className="font-semibold mb-4">Tasks by Status</h2>

          {loadingStats ? (
            <div className="flex items-center justify-center p-6">
              <Loader2 className="animate-spin mr-2 h-8 w-8 text-blue-600" />
              <div className="p-6">Loading statistics...</div>
            </div>
          ) : (
            <>
              <ResponsiveContainer width="100%" height={240}>
                <PieChart>
                  <Pie
                    data={taskSummary}
                    dataKey="value"
                    nameKey="status"
                    innerRadius={55}
                    outerRadius={85}
                  >
                    {taskSummary.map((item) => (
                      <Cell
                        key={item.status}
                        fill={STATUS_COLORS[item.status]}
                      />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>

              {/* Legend */}
              <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
                {taskSummary.map((item) => (
                  <div key={item.status} className="flex items-center gap-2">
                    <span
                      className="w-3 h-3 rounded-full"
                      style={{ backgroundColor: STATUS_COLORS[item.status] }}
                    />
                    <span className="text-gray-600">{item.status}</span>
                    <span className="ml-auto font-semibold text-gray-800">
                      {item.value}
                    </span>
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        {/* -------- User Statistics (select + pie) -------- */}
        <div className="bg-white rounded-xl p-5 shadow space-y-4">
          <h2 className="font-semibold">User Statistics</h2>
          {loadingUserDetail ? (
            <div className="flex items-center justify-center p-6">
              <Loader2 className="animate-spin mr-2 h-8 w-8 text-blue-600" />
              <div className="p-6">Loading user details...</div>
            </div>
          ) : (
            <>
              <select
                value={userId}
                onChange={(e) => setUserId(Number(e.target.value))}
                className="border rounded-lg px-3 py-2 w-60 focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.fullName}
                  </option>
                ))}
              </select>

              <div>
                <ResponsiveContainer width="100%" height={240}>
                  <PieChart>
                    <Pie
                      data={userDetail[userId] ?? []}
                      dataKey="value"
                      nameKey="status"
                      outerRadius={85}
                    >
                      {(userDetail[userId] ?? []).map((item) => (
                        <Cell
                          key={item.status}
                          fill={STATUS_COLORS[item.status]}
                        />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>

                <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
                  {(userDetail[userId] ?? []).map((item) => (
                    <div key={item.status} className="flex items-center gap-2">
                      <span
                        className="w-3 h-3 rounded-full"
                        style={{ backgroundColor: STATUS_COLORS[item.status] }}
                      />
                      <span className="text-gray-600">{item.status}</span>
                      <span className="ml-auto font-semibold text-gray-800">
                        {item.value}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </>
          )}
        </div>
      </div>

      {/* -------- Bar chart full width -------- */}
      <div className="bg-white rounded-xl p-5 shadow">
        <h2 className="font-semibold mb-4">Tasks by User</h2>

        {loadingStats ? (
          <div className="flex items-center justify-center p-6">
            <Loader2 className="animate-spin mr-2 h-8 w-8 text-blue-600" />
            <div className="p-6">Loading statistics...</div>
          </div>
        ) : (
          <ResponsiveContainer width="100%" height={320}>
            <BarChart data={tasksByUser} margin={{ bottom: 40 }}>
              <XAxis
                dataKey="user"
                interval={0}
                height={60}
                tick={<CustomizedAxisTick />}
              />
              <YAxis />
              <Tooltip />
              <Bar dataKey="total" fill="#3b82f6" />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}
