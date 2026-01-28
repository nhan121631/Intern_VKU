import DashboardPage from "../page/DashboardPage";
import ForgotPasswordPage from "../page/ForgotPasswordPasge";
import HomePage from "../page/HomePage";
import LoginPage from "../page/LoginPage";
import ManageUserPage from "../page/ManageUserPage";
import MyTaskPage from "../page/MyTaskPage";
import OurTaskPage from "../page/OurTaskPage";
import ProfilePage from "../page/ProfilePage";
import RegisterPage from "../page/RegisterPage";
import UserDashboardPage from "../page/UserDashboardPage";
import {
  Home,
  ListTodo,
  BarChart3,
  ClipboardList,
  Users,
  User,
} from "lucide-react";

const routes = [
  {
    path: "/login",
    showOnMenu: false,
    isPublic: true,
    name: "Login",
    index: true,
    element: <LoginPage />,
    roles: [],
  },
  {
    path: "/register",
    showOnMenu: false,
    isPublic: true,
    name: "Register",
    index: true,
    element: <RegisterPage />,
    roles: [],
  },
  {
    path: "/forgot-password",
    showOnMenu: false,
    isPublic: true,
    name: "Forgot Password",
    index: true,
    element: <ForgotPasswordPage />,
    roles: [],
  },
  {
    path: "/",
    showOnMenu: true,
    isPublic: true,
    name: "Home",
    icon: Home,
    index: true,
    element: <HomePage />,
    roles: [],
  },
  {
    path: "/our-task",
    showOnMenu: true,
    isPublic: false,
    name: "Our Task",
    icon: ListTodo,
    index: true,
    element: <OurTaskPage />,
    roles: ["administrators"],
  },

  {
    path: "/dashboard",
    showOnMenu: true,
    isPublic: false,
    name: "Dashboard",
    icon: BarChart3,
    index: true,
    element: <DashboardPage />,
    roles: ["administrators"],
  },
  {
    path: "/my-task",
    showOnMenu: true,
    isPublic: false,
    name: "My Task",
    icon: ClipboardList,
    index: true,
    element: <MyTaskPage />,
    roles: ["administrators", "users"],
  },
  {
    path: "/user-dashboard",
    showOnMenu: true,
    isPublic: false,
    name: "My Dashboard",
    icon: BarChart3,
    index: true,
    element: <UserDashboardPage />,
    roles: ["users"],
  },

  {
    path: "/manage-user",
    showOnMenu: true,
    isPublic: false,
    name: "Manage User",
    icon: Users,
    index: true,
    element: <ManageUserPage />,
    roles: ["administrators"],
  },
  {
    path: "/profile",
    showOnMenu: true,
    isPublic: false,
    name: "Profile",
    icon: User,
    index: true,
    element: <ProfilePage />,
    roles: ["administrators", "users"],
  },
];

export default routes;
