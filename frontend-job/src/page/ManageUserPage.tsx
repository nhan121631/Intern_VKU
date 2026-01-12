/* eslint-disable @typescript-eslint/no-explicit-any */
import { useEffect, useState } from "react";
import { getUsersPaginated, changeUserStatus } from "../service/UserService";
import UserTable from "../components/UserTable";
import Notification from "../components/Notification";

interface User {
  id: number;
  username: string;
  fullName: string;
  createdAt?: string | null;
  isActive: number;
}

export default function ManageUserPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(10);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [hasNext, setHasNext] = useState<boolean>(false);
  const [hasPrevious, setHasPrevious] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(false);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [changingStatusId, setChangingStatusId] = useState<number | null>(null);
  const [notification, setNotification] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);
  const [confirmModal, setConfirmModal] = useState<{
    userId: number;
    isActive: number;
  } | null>(null);

  const handlePrev = () => {
    setPage((p) => Math.max(0, p - 1));
  };
  const handleNext = () => {
    setPage((p) => p + 1);
  };

  const handleSizeChange = (s: number) => {
    setSize(s);
    setPage(0);
  };

  const fetchUsers = async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const res: any = await getUsersPaginated(page, size);
      console.log("getUsersPaginated response:", res);

      if (res && Array.isArray(res.data)) {
        setUsers(res.data as User[]);
        setHasNext(Boolean(res.hasNext));
        setHasPrevious(Boolean(res.hasPrevious));
        setTotalPages(
          Number(res.totalPages ?? Math.ceil((res.totalElements || 0) / size))
        );
      } else if (Array.isArray(res)) {
        setUsers(res as User[]);
        setHasNext(false);
        setHasPrevious(false);
        setTotalPages(1);
      } else {
        setUsers([]);
      }
    } catch (e: any) {
      console.error(e);
      setLoadError(e?.message ?? "Error fetching users");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size]);

  const handleChangeStatus = async (userId: number, isActive: number) => {
    // Show confirmation modal when deactivating (isActive = 0 means currently active, will change to 1)
    if (isActive === 0) {
      setConfirmModal({ userId, isActive });
      return;
    }
    // If activating, proceed directly
    await performStatusChange(userId, isActive);
  };

  const performStatusChange = async (userId: number, isActive: number) => {
    try {
      console.log(
        "Changing status for user:",
        userId,
        "Current isActive:",
        isActive
      );

      // Set loading state for this specific user
      setChangingStatusId(userId);

      // Determine new status (toggle): 0 = active, 1 = inactive
      const newStatus = isActive === 0 ? 1 : 0;

      // Call API to change status
      await changeUserStatus(userId, newStatus);

      // Update local state on success
      setUsers((prevUsers) =>
        prevUsers.map((user) =>
          user.id === userId ? { ...user, isActive: newStatus } : user
        )
      );

      // Show success notification
      setNotification({
        type: "success",
        message: `User ${
          newStatus === 0 ? "activated" : "deactivated"
        } successfully!`,
      });
    } catch (e: any) {
      console.error(e);

      // Show error notification
      setNotification({
        type: "error",
        message:
          e?.response?.data?.message ||
          e?.message ||
          "Error changing user status",
      });
    } finally {
      setChangingStatusId(null);
      setConfirmModal(null);
    }
  };
  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">User Management</h1>

      {/* Notification */}
      {notification && (
        <Notification
          message={notification.message}
          type={notification.type}
          duration={notification.type === "success" ? 3000 : 5000}
          onClose={() => setNotification(null)}
        />
      )}

      {/* Confirmation Modal */}
      {confirmModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold mb-4">Confirm Deactivation</h3>
            <p className="text-gray-600 mb-6">
              Are you sure you want to deactivate this user?
            </p>
            <div className="flex justify-end gap-3">
              <button
                onClick={() => setConfirmModal(null)}
                className="px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
              >
                Cancel
              </button>
              <button
                onClick={() =>
                  performStatusChange(
                    confirmModal.userId,
                    confirmModal.isActive
                  )
                }
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
              >
                Deactivate
              </button>
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <div className="relative">
          <div className="absolute inset-0 bg-white/80 flex items-center justify-center rounded z-10">
            <div className="flex flex-col items-center gap-2">
              <div className="w-8 h-8 border-4 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
              <span className="text-sm text-gray-600">Loading users...</span>
            </div>
          </div>
          <div className="h-40" />
        </div>
      ) : loadError ? (
        <div className="w-full flex items-center justify-center py-8">
          <div className="bg-red-50 border border-red-100 text-red-700 px-6 py-4 rounded-lg shadow-sm max-w-xl w-full">
            <div className="flex items-center gap-4">
              <div className="flex-1">
                <div className="font-semibold">Failed to load users</div>
                <div className="text-sm mt-1">{String(loadError)}</div>
              </div>
              <div>
                <button
                  onClick={fetchUsers}
                  className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700"
                >
                  Retry
                </button>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <UserTable
          users={users}
          page={page}
          size={size}
          totalPages={totalPages}
          hasNext={hasNext}
          hasPrevious={hasPrevious}
          onPrev={handlePrev}
          onNext={handleNext}
          onSizeChange={handleSizeChange}
          onChangeStatus={handleChangeStatus}
          changingStatusId={changingStatusId}
        />
      )}
    </div>
  );
}
