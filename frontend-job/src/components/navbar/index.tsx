/* eslint-disable @typescript-eslint/no-explicit-any */
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import {
  collection,
  doc,
  onSnapshot,
  orderBy,
  query,
  updateDoc,
  where,
} from "firebase/firestore";
import { Bell, Sun, Moon } from "lucide-react";
import { useContext, useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { db } from "../../lib/fire-base";
import { getNameUser } from "../../service/UserService";
import { useAuthStore } from "../../stores/useAuthorStore";
import type { NameUserResponse } from "../../types/type";
import Notification from "../Notification";
import { ThemeContext } from "../../context/ThemeContext";

dayjs.extend(relativeTime);
dayjs.locale("en");

type Notification = {
  id: string;
  landlordId: string;
  type: string;
  createdAt: any;
  contractId?: number | string | undefined;
  message: string;
  isRead: boolean;
};

export const NavBar = () => {
  const loggedInUser = useAuthStore((state) => state.loggedInUser);
  const logOut = useAuthStore((state) => state.logOut);
  const [fullName, setFullName] = useState<string>("");

  const [notificationOpen, setNotificationOpen] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [displayedNotifications, setDisplayedNotifications] = useState<
    Notification[]
  >([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const notificationsPerPage = 5;
  const [isInitialLoad, setIsInitialLoad] = useState(true);
  const { isDark, setIsDark } = useContext(ThemeContext);
  const [notif, setNotif] = useState<{
    message: string;
    type: "success" | "error" | "info";
  } | null>(null);
  const handleClick = () => {
    setIsDark(!isDark);
    localStorage.setItem("theme", isDark ? "light" : "dark");
  };
  const navigate = useNavigate();

  useEffect(() => {
    if (!loggedInUser) return;

    const fetchFullName = async () => {
      try {
        const response = (await getNameUser()) as NameUserResponse;
        if (response) setFullName(response.fullName || "");
      } catch (e) {
        console.error(e);
      }
    };
    fetchFullName();
  }, [loggedInUser]);

  useEffect(() => {
    if (!loggedInUser) return;

    const q = query(
      collection(db, "notifications"),
      where("receiverId", "==", String(loggedInUser?.id)),
      orderBy("createdAt", "desc"),
    );
    const unsub = onSnapshot(q, (snapshot) => {
      const data = snapshot.docs.map(
        (docSnap) =>
          ({
            id: docSnap.id,
            ...docSnap.data(),
          }) as Notification,
      );

      // Only show popup for truly new notifications (not the initial load)
      if (!isInitialLoad) {
        // Only show popup if a new document is added AND it's not from cache
        snapshot.docChanges().forEach((change) => {
          if (change.type === "added" && !change.doc.metadata.fromCache) {
            console.log("aaaaa");
            setNotif({
              message: change.doc.data().message,
              type: "info",
            });
          }
        });
      } else {
        console.log("bbbb");

        setIsInitialLoad(false);
      }

      setNotifications(data);
      // Reset pagination when new notifications come
      setCurrentPage(1);
      setDisplayedNotifications(data.slice(0, notificationsPerPage));
    });

    return () => unsub();
  }, [loggedInUser, isInitialLoad, notificationsPerPage]);

  const handleLogout = () => {
    setTimeout(() => {
      logOut();
      navigate("/");
    }, 900);
  };
  const unreadCount = notifications.filter((n) => !n.isRead).length;

  const loadMoreNotifications = () => {
    if (isLoadingMore || displayedNotifications.length >= notifications.length)
      return;

    setIsLoadingMore(true);
    setTimeout(() => {
      const nextPage = currentPage + 1;
      const newDisplayedNotifications = notifications.slice(
        0,
        nextPage * notificationsPerPage,
      );
      setDisplayedNotifications(newDisplayedNotifications);
      setCurrentPage(nextPage);
      setIsLoadingMore(false);
    }, 500); // Simulate loading delay
  };
  const handleNotificationScroll = (e: React.UIEvent<HTMLDivElement>) => {
    const { scrollTop, scrollHeight, clientHeight } = e.currentTarget;
    if (scrollTop + clientHeight >= scrollHeight - 5) {
      loadMoreNotifications();
    }
  };

  const handleNotificationClick = async (notificationId: string) => {
    await updateDoc(doc(db, "notifications", notificationId), { isRead: true });
    setNotificationOpen(false);
    navigate("/my-task");
  };

  const handleMarkAllAsRead = async () => {
    setNotif({
      message: "All notifications marked as read",
      type: "success",
    });
    try {
      const unread = notifications.filter((n) => !n.isRead);
      for (const n of unread) {
        await updateDoc(doc(db, "notifications", n.id), { isRead: true });
      }
    } catch (err) {
      console.error("Error mark all as read:", err);
    }
  };

  const notificationContent = (
    <div className="w-80 bg-white dark:bg-gray-800 rounded shadow">
      {/* Header */}
      <div className="flex justify-between items-center p-3 border-b dark:border-gray-700">
        <h3 className="text-base font-semibold text-gray-800 dark:text-gray-100">
          Notifications
        </h3>
        <button
          onClick={handleMarkAllAsRead}
          className="text-sm text-blue-600 hover:underline"
        >
          Mark all as read
        </button>
      </div>

      {/* Notification list */}
      <div
        className="max-h-80 overflow-y-auto"
        onScroll={handleNotificationScroll}
      >
        {displayedNotifications.map((item) => (
          <div
            key={item.id}
            onClick={() => handleNotificationClick(item.id)}
            className={`cursor-pointer p-3 border-b dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700
            ${!item.isRead ? "bg-blue-50 dark:bg-blue-900/20" : ""}
          `}
          >
            {/* Title */}
            <div className="flex justify-between items-start">
              <span
                className={`text-sm ${
                  !item.isRead
                    ? "font-semibold text-gray-900 dark:text-white"
                    : "text-gray-700 dark:text-gray-300"
                }`}
              >
                {item.type === "TASK_ASSIGNED" && "New Task Assigned"}
              </span>

              {!item.isRead && (
                <span className="w-2 h-2 bg-blue-500 rounded-full mt-1 ml-2" />
              )}
            </div>

            {/* Message */}
            <div className="text-sm text-gray-600 dark:text-gray-300 mt-1">
              {item.message}
            </div>

            {/* Time */}
            <div className="text-xs text-gray-400 mt-1">
              {item.createdAt?.toDate
                ? dayjs(item.createdAt.toDate()).fromNow()
                : ""}
            </div>
          </div>
        ))}

        {/* Loading more */}
        {isLoadingMore && (
          <div className="flex items-center justify-center p-3 text-sm text-gray-500">
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-500 mr-2" />
            More...
          </div>
        )}
      </div>

      {/* Load more */}
      {displayedNotifications.length < notifications.length &&
        !isLoadingMore && (
          <div className="text-center p-3 border-t dark:border-gray-700">
            <button
              onClick={loadMoreNotifications}
              className="text-sm text-blue-600 hover:underline"
            >
              Load more notifications (
              {notifications.length - displayedNotifications.length} remaining)
            </button>
          </div>
        )}

      {/* All loaded */}
      {displayedNotifications.length >= notifications.length &&
        notifications.length > notificationsPerPage && (
          <div className="text-center p-3 border-t dark:border-gray-700">
            <span className="text-sm text-gray-500">
              All notifications have been loaded
            </span>
          </div>
        )}
    </div>
  );

  return (
    <nav className="bg-[#86a8e7] dark:bg-[#1f2937] text-white px-6 py-4 shadow-lg dark:shadow-gray-900 transition-colors duration-300">
      <div className="flex justify-between items-center">
        {/* Logo */}
        <div className="text-2xl font-bold tracking-wide">Task Management</div>

        {/* Right Section */}
        <div className="flex items-center gap-4">
          <div className="">
            <button
              onClick={handleClick}
              className="text-white dark:text-gray-200 hover:text-yellow-300 dark:hover:text-yellow-400 transition"
            >
              {isDark ? (
                <Sun className="h-6 w-6" />
              ) : (
                <Moon className="h-6 w-6" />
              )}
            </button>
          </div>
          {/* Notification */}
          {loggedInUser && (
            <div className="relative">
              <button
                onClick={() => setNotificationOpen((prev) => !prev)}
                className="relative focus:outline-none"
              >
                <Bell className="w-6 h-6 text-white dark:text-gray-200 hover:text-blue-200 dark:hover:text-blue-300 transition" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 min-w-5 h-5 px-1 text-xs bg-red-500 dark:bg-red-600 text-white rounded-full flex items-center justify-center font-bold shadow-md">
                    {unreadCount}
                  </span>
                )}
              </button>

              {notificationOpen && (
                <>
                  <div
                    className="fixed inset-0 z-40"
                    onClick={() => setNotificationOpen(false)}
                  />
                  <div className="absolute right-0 mt-2 z-50">
                    {notificationContent}
                  </div>
                </>
              )}
            </div>
          )}

          {/* User Info & Auth */}
          {loggedInUser ? (
            <div className="flex items-center gap-4">
              <span className="font-medium text-white dark:text-gray-200">
                Hello, {fullName}
              </span>
              <button
                onClick={handleLogout}
                className="bg-white dark:bg-gray-700 text-[#7f7fd5] dark:text-white px-4 py-2 rounded-full font-semibold hover:bg-blue-50 dark:hover:bg-gray-600 transition shadow-sm"
              >
                Log Out
              </button>
            </div>
          ) : (
            <button
              onClick={() => navigate("/login")}
              className="bg-white dark:bg-gray-700 text-[#7f7fd5] dark:text-white px-4 py-2 rounded-full font-semibold hover:bg-blue-50 dark:hover:bg-gray-600 transition shadow-sm"
            >
              Log In
            </button>
          )}
        </div>
      </div>

      {notif && (
        <Notification
          message={notif.message}
          type={notif.type}
          duration={1000}
          onClose={() => setNotif(null)}
        />
      )}
    </nav>
  );
};
