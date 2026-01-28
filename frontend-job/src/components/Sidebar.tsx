/* eslint-disable @typescript-eslint/no-explicit-any */
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { useState } from "react";
import { NavLink } from "react-router";
import routes from "../routes";
import { useAuthStore } from "../stores/useAuthorStore";
import Notification from "./Notification";

dayjs.extend(relativeTime);
dayjs.locale("en");

type SidebarProps = {
  onCollapseChange?: (collapsed: boolean) => void;
};

export const Sidebar = ({ onCollapseChange }: SidebarProps) => {
  const [collapsed, setCollapsed] = useState(false);
  const loggedInUser = useAuthStore((state) => state.loggedInUser);

  const [notif, setNotif] = useState<{
    message: string;
    type: "success" | "error" | "info";
  } | null>(null);

  const userRoles: string[] =
    loggedInUser?.roles?.map((role: any) =>
      typeof role === "string" ? role.toLowerCase() : role.code?.toLowerCase(),
    ) || [];
  return (
    <>
      <aside
        className={`fixed left-0 top-auto h-[calc(100vh-64px)] bg-[#86a8e7] dark:bg-[#2d3748] text-white dark:text-gray-100 shadow-2xl dark:shadow-gray-900 transition-all duration-300 z-40 ${
          collapsed ? "w-20" : "w-72"
        }`}
      >
        {/* Header */}
        <div
          className={`flex items-center p-4 border-b border-white/30 dark:border-gray-700 backdrop-blur-sm ${
            collapsed ? "justify-center" : "justify-between"
          }`}
        >
          {!collapsed && (
            <h1 className="text-xl font-bold tracking-wide dark:text-white">
              Panel
            </h1>
          )}
          <button
            onClick={() => {
              const newCollapsed = !collapsed;
              setCollapsed(newCollapsed);
              onCollapseChange?.(newCollapsed);
            }}
            className="p-2 rounded-xl hover:bg-white/30 dark:hover:bg-gray-700 active:bg-white/40 dark:active:bg-gray-600 transition-all duration-200 backdrop-blur-sm shadow-lg"
          >
            {collapsed ? (
              <ChevronRight className="w-5 h-5" />
            ) : (
              <ChevronLeft className="w-5 h-5" />
            )}
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 overflow-y-auto py-4 px-3">
          <ul className="space-y-2">
            {routes.map((route) => {
              if (route.showOnMenu === false) return null;

              const routeRoles: string[] =
                route.roles?.map((role: string) => role?.toLowerCase()) || [];

              const hasAccess =
                route.isPublic ||
                routeRoles.length === 0 ||
                userRoles.some(
                  (role: string) =>
                    role === "administrators" ||
                    routeRoles.includes(role?.toLowerCase()),
                );

              if (!hasAccess) return null;

              return (
                <li key={route.path}>
                  <NavLink
                    to={route.path}
                    className={({ isActive }) =>
                      `flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200
                     ${
                       isActive
                         ? "bg-white dark:bg-gray-800 text-purple-700 dark:text-purple-400 shadow-lg scale-105"
                         : "hover:bg-white/25 dark:hover:bg-gray-700/50 hover:scale-105 active:scale-95"
                     } ${collapsed ? "justify-center" : ""}`
                    }
                    title={collapsed ? route.name : undefined}
                  >
                    {route.icon && <route.icon className="w-5 h-5 shrink-0" />}
                    {!collapsed && (
                      <span className="truncate">{route.name}</span>
                    )}
                  </NavLink>
                </li>
              );
            })}
          </ul>
        </nav>
      </aside>

      {notif && (
        <Notification
          message={notif.message}
          type={notif.type}
          duration={1000}
          onClose={() => setNotif(null)}
        />
      )}
    </>
  );
};
