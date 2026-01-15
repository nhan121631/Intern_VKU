/* eslint-disable @typescript-eslint/no-explicit-any */
import React, { useEffect, useState } from "react";
import type { TaskHistoryResponse } from "../types/type";
import {
  getDetailHistory,
  getTaskHistory,
} from "../service/TaskHistoryService";
import { X } from "lucide-react";

type Props = {
  open: boolean;
  onClose: () => void;
  taskId: number | null;
};

const fmtDate = (s?: string) => {
  if (!s) return "";
  try {
    const d = new Date(s);
    if (isNaN(d.getTime())) return s;
    const pad = (n: number) => String(n).padStart(2, "0");
    const y = d.getFullYear();
    const m = pad(d.getMonth() + 1);
    const day = pad(d.getDate());
    const hh = pad(d.getHours());
    const mm = pad(d.getMinutes());
    const ss = pad(d.getSeconds());
    return `${y}-${m}-${day} ${hh}:${mm}:${ss}`;
  } catch {
    return s;
  }
};

const TaskHistoryModal: React.FC<Props> = ({ open, onClose, taskId }) => {
  const [history, setHistory] = useState<TaskHistoryResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [detail, setDetail] = useState<TaskHistoryResponse | null>(null);

  useEffect(() => {
    if (!open) {
      setHistory([]);
      setError(null);
      setLoading(false);
      return;
    }

    let cancelled = false;

    const fetchHistory = async () => {
      if (taskId === null) {
        setHistory([]);
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const res = (await getTaskHistory(taskId)) as TaskHistoryResponse[];
        if (!cancelled) setHistory(res);
      } catch (e: any) {
        console.error("Error fetching task history:", e);
        if (!cancelled) setError(e?.message || "Failed to load history");
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchHistory();

    return () => {
      cancelled = true;
    };
  }, [taskId, open]);

  if (!open) return null;
  const handleClose = () => {
    setDetail(null);
    onClose();
  };
  const handleFetchDetail = async (id: number) => {
    if (id === null) {
      setDetail(null);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = (await getDetailHistory(id)) as TaskHistoryResponse;
      setDetail(res);
    } catch (e: any) {
      console.error("Error fetching detail history:", e);
      setError(e?.message || "Failed to load detail history");
    } finally {
      setLoading(false);
    }
  };

  const parseMaybeJson = (s?: string) => {
    if (!s) return null;
    try {
      return JSON.parse(s);
    } catch {
      return s;
    }
  };

  const renderValue = (v: any) => {
    if (v === null || v === undefined) return "-";
    const pad = (n: number) => String(n).padStart(2, "0");
    if (
      Array.isArray(v) &&
      v.length >= 3 &&
      v.slice(0, 3).every((n) => typeof n === "number")
    ) {
      try {
        const y = Number(v[0]);
        const m = Number(v[1]);
        const d = Number(v[2]);
        return `${y}-${pad(m)}-${pad(d)}`;
      } catch {
        return String(v);
      }
    }
    if (typeof v === "string") {
      const parsed = new Date(v);
      if (!isNaN(parsed.getTime())) {
        const y = parsed.getFullYear();
        const m = pad(parsed.getMonth() + 1);
        const d = pad(parsed.getDate());
        return `${y}-${m}-${d}`;
      }
    }
    if (typeof v === "boolean") return v ? "Yes" : "No";
    if (typeof v === "object") return JSON.stringify(v);
    return String(v);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center px-4">
      <div
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={handleClose}
      />

      <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-2xl overflow-hidden">
        <div className="flex items-center justify-between p-4 border-b">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-blue-600 text-white flex items-center justify-center font-semibold">
              H
            </div>
            <div>
              <h3 className="text-lg font-semibold">Task History</h3>
              <p className="text-xs text-gray-500">
                Recent updates and who performed them
              </p>
            </div>
          </div>
          <button
            onClick={handleClose}
            aria-label="Close"
            className="rounded-md p-2 hover:bg-gray-100 text-gray-600"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-4">
          {loading ? (
            <div className="py-12 text-center text-gray-500">Loading...</div>
          ) : error ? (
            <div className="py-6 text-center text-red-600">{error}</div>
          ) : history.length === 0 ? (
            <div className="py-12 text-center text-gray-500">
              No history available.
            </div>
          ) : (
            <div className="overflow-y-auto max-h-80">
              <table className="min-w-full bg-white divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      STT
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Updated By
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Updated At
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-100">
                  {history.map((h) => (
                    <tr
                      key={h.id}
                      className="hover:bg-gray-50 cursor-pointer"
                      onClick={() => handleFetchDetail(h.id)}
                    >
                      <td className="px-4 py-3 text-sm text-gray-700">
                        {history.indexOf(h) + 1}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-800">
                        {h.updateBy || h.updatedByName}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600">
                        {fmtDate(h.updatedAt)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
        {detail && (
          <div className="p-4 border-t bg-gray-50">
            <h4 className="text-sm font-medium mb-2">Update details</h4>
            <div className="text-xs text-gray-600 mb-2">
              Updated by: {detail.updateBy || detail.updatedByName}
            </div>
            {detail.roles && (
              <div className="text-xs text-gray-600 mb-2">
                Roles: {detail.roles.join(", ")}
              </div>
            )}

            {(() => {
              const oldObj = parseMaybeJson(detail.oldData);
              const newObj = parseMaybeJson(detail.newData);
              const oldIsObj =
                oldObj && typeof oldObj === "object" && !Array.isArray(oldObj);
              const newIsObj =
                newObj && typeof newObj === "object" && !Array.isArray(newObj);

              if (!oldIsObj && !newIsObj) {
                return (
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <div className="text-xs font-semibold mb-1">Old</div>
                      <div className="text-sm text-gray-800">
                        {renderValue(oldObj)}
                      </div>
                    </div>
                    <div>
                      <div className="text-xs font-semibold mb-1">New</div>
                      <div className="text-sm text-gray-800">
                        {renderValue(newObj)}
                      </div>
                    </div>
                  </div>
                );
              }

              const keys = Array.from(
                new Set([
                  ...(oldIsObj ? Object.keys(oldObj) : []),
                  ...(newIsObj ? Object.keys(newObj) : []),
                ])
              );

              return (
                <div className="overflow-auto">
                  <table className="min-w-full text-sm">
                    <thead>
                      <tr className="text-left text-xs text-gray-500">
                        <th className="px-2 py-1">Field</th>
                        <th className="px-2 py-1">Old</th>
                        <th className="px-2 py-1">New</th>
                      </tr>
                    </thead>
                    <tbody>
                      {keys.map((k) => {
                        const ov = oldIsObj ? (oldObj as any)[k] : undefined;
                        const nv = newIsObj ? (newObj as any)[k] : undefined;
                        const changed =
                          JSON.stringify(ov) !== JSON.stringify(nv);
                        return (
                          <tr
                            key={k}
                            className={changed ? "bg-white" : "bg-transparent"}
                          >
                            <td className="px-2 py-2 font-medium text-gray-700">
                              {k}
                            </td>
                            <td
                              className={`px-2 py-2 text-gray-700 ${
                                changed ? "line-through text-red-500" : ""
                              }`}
                            >
                              {renderValue(ov)}
                            </td>
                            <td
                              className={`px-2 py-2 ${
                                changed
                                  ? "text-green-700 font-semibold"
                                  : "text-gray-700"
                              }`}
                            >
                              {renderValue(nv)}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              );
            })()}
          </div>
        )}
        <div className="flex justify-end gap-2 p-4 border-t">
          <button
            onClick={handleClose}
            className="px-4 py-2 bg-gray-100 rounded-md hover:bg-gray-200"
          >
            Close
          </button>
          <button
            onClick={handleClose}
            className="px-4 py-2 bg-blue-600 text-white rounded-md"
          >
            OK
          </button>
        </div>
      </div>
    </div>
  );
};

export default TaskHistoryModal;
