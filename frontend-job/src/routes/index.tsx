import HomePage from "../page/HomePage";
import LoginPage from "../page/LoginPage";
import ManageUserPage from "../page/ManageUserPage";
import MyTaskPage from "../page/MyTaskPage";
import OurTaskPage from "../page/OurTaskPage";
import ProfilePage from "../page/ProfilePage";
import RegisterPage from "../page/RegisterPage";

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
    path: "/home",
    showOnMenu: true,
    isPublic: true,
    name: "Home",
    index: true,
    element: <HomePage />,
    roles: [],
  },
  {
    path: "/our-task",
    showOnMenu: true,
    isPublic: false,
    name: "Our Task",
    index: true,
    element: <OurTaskPage />,
    roles: ["administrators"],
  },
  {
    path: "/my-task",
    showOnMenu: true,
    isPublic: false,
    name: "My Task",
    index: true,
    element: <MyTaskPage />,
    roles: ["administrators", "users"],
  },
  {
    path: "/manage-user",
    showOnMenu: true,
    isPublic: false,
    name: "Manage User",
    index: true,
    element: <ManageUserPage />,
    roles: ["administrators"],
  },
  {
    path: "/profile",
    showOnMenu: true,
    isPublic: false,
    name: "Profile",
    index: true,
    element: <ProfilePage />,
    roles: ["administrators", "users"],
  },
];

export default routes;
