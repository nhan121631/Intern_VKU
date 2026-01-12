import React from "react";

interface User {
  id: number;
  username: string;
  fullName: string;
  createdAt?: string | null;
  isActive: number;
}

interface Props {
  users: User[];
  page: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
  onPrev?: () => void;
  onNext?: () => void;
  size?: number;
  onSizeChange?: (size: number) => void;
  onChangeStatus?: (userId: number, isActive: number) => void;
  changingStatusId?: number | null;
}

function parseToDate(s?: string | null) {
  if (!s) return null;
  const m = s.match(
    /^([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2})(?:\.(\d+))?(Z)?/
  );
  if (!m) return new Date(s);
  const base = m[1];
  const frac = (m[2] || "").substring(0, 3).padEnd(3, "0");
  const tz = m[3] || "";
  return new Date(`${base}.${frac}${tz}`);
}

function formatDate(s?: string | null) {
  const d = parseToDate(s);
  if (!d || Number.isNaN(d.getTime())) return "-";
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

const UserTable: React.FC<Props> = ({
  users,
  page,
  totalPages,
  hasNext,
  hasPrevious,
  onPrev,
  onNext,
  size,
  onSizeChange,
  onChangeStatus,
  changingStatusId,
}) => {
  return (
    <div>
      <div className="flex items-center justify-end mb-3">
        <label className="mr-2 text-sm text-gray-600">Page size:</label>
        <select
          value={size ?? 10}
          onChange={(e) => onSizeChange && onSizeChange(Number(e.target.value))}
          className="border rounded px-2 py-1"
        >
          <option value={5}>5</option>
          <option value={10}>10</option>
          <option value={15}>15</option>
        </select>
      </div>
      <div className="overflow-x-auto bg-white rounded shadow">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-2 text-left text-sm font-medium text-gray-600">
                ID
              </th>
              <th className="px-4 py-2 text-left text-sm font-medium text-gray-600">
                Username
              </th>
              <th className="px-4 py-2 text-left text-sm font-medium text-gray-600">
                Full name
              </th>
              <th className="px-4 py-2 text-left text-sm font-medium text-gray-600">
                Created At
              </th>
              <th className="px-4 py-2 text-left text-sm font-medium text-gray-600">
                Active
              </th>
              <th className="px-4 py-2 text-left text-sm font-medium text-gray-600">
                Action
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-100">
            {users.length === 0 ? (
              <tr>
                <td
                  colSpan={6}
                  className="px-4 py-6 text-center text-sm text-gray-500"
                >
                  No users found.
                </td>
              </tr>
            ) : (
              users.map((u) => (
                <tr key={u.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm text-gray-700">{u.id}</td>
                  <td className="px-4 py-3 text-sm text-gray-800">
                    {u.username}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-700">
                    {u.fullName}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {formatDate(u.createdAt)}
                  </td>
                  <td className="px-4 py-3 text-sm">
                    {u.isActive === 0 ? (
                      <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs">
                        Active
                      </span>
                    ) : (
                      <span className="px-2 py-1 bg-red-100 text-red-800 rounded-full text-xs">
                        Inactive
                      </span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-sm">
                    {u.isActive === 0 ? (
                      <button
                        type="button"
                        className="px-2 py-1 bg-red-600 text-white rounded text-sm hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
                        aria-label={`Deactivate user ${u.username}`}
                        onClick={() =>
                          onChangeStatus && onChangeStatus(u.id, u.isActive)
                        }
                        disabled={changingStatusId === u.id}
                      >
                        {changingStatusId === u.id && (
                          <div className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                        )}
                        Deactivate
                      </button>
                    ) : (
                      <button
                        type="button"
                        className="px-2 py-1 bg-green-600 text-white rounded text-sm hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
                        aria-label={`Activate user ${u.username}`}
                        onClick={() =>
                          onChangeStatus && onChangeStatus(u.id, u.isActive)
                        }
                        disabled={changingStatusId === u.id}
                      >
                        {changingStatusId === u.id && (
                          <div className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                        )}
                        Activate
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="flex items-center justify-between mt-4">
        <div>
          Page {page + 1} of {totalPages || 1}
        </div>
        <div className="space-x-2">
          <button
            onClick={onPrev}
            className="px-3 py-1 bg-blue-500 text-white rounded disabled:opacity-50"
            disabled={page === 0 || !hasPrevious}
          >
            Previous
          </button>
          <button
            onClick={onNext}
            className="px-3 py-1 bg-blue-500 text-white rounded disabled:opacity-50"
            disabled={!hasNext}
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};

export default UserTable;
