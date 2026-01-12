import CreateTaskPage from "../page/CreateTaskPage";
import HomePage from "../page/HomePage";
import LoginPage from "../page/LoginPage";
import ManageUserPage from "../page/ManageUserPage";
import MyTaskPage from "../page/MyTaskPage";
import OurTaskPage from "../page/OurTaskPage";
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
    path: "/create-task",
    showOnMenu: true,
    isPublic: false,
    name: "Create Task",
    index: true,
    element: <CreateTaskPage />,
    roles: ["administrators"],
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
];

export default routes;
