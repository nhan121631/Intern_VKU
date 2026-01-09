import HomePage from "../page/HomePage";
import LoginPage from "../page/LoginPage";
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
];

export default routes;
