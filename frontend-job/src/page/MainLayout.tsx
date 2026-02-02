import { useState } from "react";
import { Outlet } from "react-router";
import { Sidebar } from "../components/Sidebar";
import { NavBar } from "../components/navbar";
import { ThemeContext } from "../context/ThemeContext";
import ChatAI from "../components/ChatAI";
import { useAuthStore } from "../stores/useAuthorStore";

export const MainLayout = () => {
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState<boolean>(() => {
    if (typeof window === "undefined") return false;
    return localStorage.getItem("theme") === "dark";
  });

  const loggedInUser = useAuthStore((state) => state.loggedInUser);
  return (
    <ThemeContext.Provider
      value={{ isDark: isDarkMode, setIsDark: setIsDarkMode }}
    >
      <div className={`flex flex-col min-h-screen ${isDarkMode ? "dark" : ""}`}>
        <div className="sticky top-0 z-50">
          <NavBar />
        </div>
        <div className="flex flex-1">
          <Sidebar onCollapseChange={setSidebarCollapsed} />
          <div
            className={`flex-1 transition-all duration-300 ${
              sidebarCollapsed ? "ml-20" : "ml-72"
            }`}
          >
            <div className="bg-white dark:bg-gray-800 dark:text-white min-h-screen flex flex-col p-6">
              <Outlet />
            </div>
          </div>
        </div>
        {loggedInUser && <ChatAI />}
      </div>
    </ThemeContext.Provider>
  );
};
