/* eslint-disable @typescript-eslint/no-explicit-any */
import { Loader2 } from "lucide-react";
import { useEffect, useState } from "react";
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import { getTaskMe } from "../service/StatisticsService";
import type { TaskSummary } from "../types/type";

const STATUS_COLORS: Record<string, string> = {
  OPEN: "#facc15",
  IN_PROGRESS: "#3b82f6",
  DONE: "#22c55e",
  CANCELED: "#ef4444",
};

export default function UserDashboardPage() {
  const [loading, setLoading] = useState<boolean>(false);
  const [userDetail, setUserDetail] = useState<TaskSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [createdAtFrom, setCreatedAtFrom] = useState<string>("");
  const [createdAtTo, setCreatedAtTo] = useState<string>("");
  // today's date in `YYYY-MM-DD` format for input max validation
  function pad(n: number) {
    return n.toString().padStart(2, "0");
  }
  const today = new Date();
  const todayStr = `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(
    today.getDate(),
  )}`;

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
        setLoading(true);
        const from = shouldFetchUnfiltered ? "" : createdAtFrom;
        const to = shouldFetchUnfiltered ? "" : createdAtTo;

        const res = await getTaskMe(from, to);
        setUserDetail(res);
      } catch (e: any) {
        setError((e as any)?.message || "Failed to load task statistics.");
      } finally {
        setLoading(false);
      }
    };

    fetchStatistics();
  }, [createdAtFrom, createdAtTo]);

  // Initial fetch is handled by the main effect above which runs
  // on mount and whenever the date filters change. No separate
  // unfiltered fetch effect is necessary.

  const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    // Prevent selecting future dates
    if (value && value > todayStr) {
      setError("Date cannot be in the future");
      return;
    }

    setError(null);
    if (name === "createdAtFrom") {
      setCreatedAtFrom(value);
    }
    if (name === "createdAtTo") {
      setCreatedAtTo(value);
    }
  };

  if (error) {
    return (
      <div className="flex items-center justify-center p-6 text-red-600">
        Error: {error}
      </div>
    );
  }

  return (
    <div className="p-6 space-y-8">
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">My Dashboard</h1>

        {/* created from and to */}
        <div className="flex gap-4">
          {/* Future enhancement: Date range picker for filtering statistics */}
          <div className="flex gap-2 items-center justify-center">
            <label className="text-gray-700 font-semibold dark:text-gray-300">
              Created From:
            </label>
            <input
              type="date"
              name="createdAtFrom"
              value={createdAtFrom}
              onChange={handleDateChange}
              max={createdAtTo || todayStr}
              className="border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
            />
          </div>
          <div className="flex gap-2 items-center justify-center">
            <label className="text-gray-700 font-semibold dark:text-gray-300">
              Created To:
            </label>
            <input
              type="date"
              name="createdAtTo"
              value={createdAtTo}
              onChange={handleDateChange}
              min={createdAtFrom || undefined}
              max={todayStr}
              className="border rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
            />
          </div>
        </div>
      </div>
      {/* ================= USER DETAIL & SUMMARY (side-by-side) ================= */}
      <div className="flex justify-center min-w-full">
        <div className="bg-white rounded-xl p-4 shadow space-y-4 w-full mx-auto max-w-2xl dark:bg-gray-700">
          <h2 className="font-semibold">User Statistics</h2>
          {loading ? (
            <div className="flex items-center justify-center p-6">
              <Loader2 className="animate-spin mr-2 h-8 w-8 text-blue-600" />
              <div className="p-6 dark:text-gray-300">Loading...</div>
            </div>
          ) : (
            <>
              <div className="w-full flex flex-col items-center">
                <ResponsiveContainer width="100%" height={420}>
                  <PieChart>
                    <Pie
                      data={userDetail}
                      dataKey="value"
                      nameKey="status"
                      outerRadius={160}
                    >
                      {userDetail.map((item) => (
                        <Cell
                          key={item.status}
                          fill={STATUS_COLORS[item.status]}
                        />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>

                <div className="mt-4 grid grid-cols-2 gap-3 text-sm w-full max-w-md dark:text-gray-300">
                  {userDetail.map((item) => (
                    <div key={item.status} className="flex items-center gap-2">
                      <span
                        className="w-3 h-3 rounded-full"
                        style={{ backgroundColor: STATUS_COLORS[item.status] }}
                      />
                      <span className="text-gray-600 dark:text-gray-300">
                        {item.status}
                      </span>
                      <span className="ml-auto font-semibold text-gray-800 dark:text-gray-300">
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
    </div>
  );
}
