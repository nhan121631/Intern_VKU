/* eslint-disable @typescript-eslint/no-explicit-any */
import React from "react";
import { createBrowserRouter, RouterProvider, Navigate } from "react-router";
import { AccessDeniedPage } from "./page/AccessDeniedPage";
import { MainLayout } from "./page/MainLayout";
import routes from "./routes";
import { useAuthStore } from "./stores/useAuthorStore";

export default function TaskWithZustand() {
  const { loggedInUser } = useAuthStore((state) => state);

  const userRoles: string[] =
    loggedInUser?.roles?.map((role: any) =>
      typeof role === "string" ? role.toLowerCase() : role.code?.toLowerCase()
    ) || [];
  const generatedRoutes: any[] = routes
    .map((route) => {
      const routeRoles: string[] =
        route.roles?.map((role: string) => role?.toLowerCase()) || [];
      const hasAccess = userRoles.some((role: string) => {
        return (
          role?.toLowerCase() === "administrators" ||
          routeRoles.includes(role?.toLowerCase())
        );
      });
      return hasAccess
        ? {
            path: route.path,
            element: route.element,
            index: route.index,
          }
        : null;
    })
    .filter(Boolean);
  routes.forEach((route) => {
    if (route.isPublic) {
      generatedRoutes.push({
        path: route.path,
        element: route.element,
        index: route.index,
      });
    }
  });
  const router = createBrowserRouter([
    {
      path: "/",
      element: <MainLayout />,
      children: [
        { index: true, element: <Navigate to="/home" replace /> },
        ...generatedRoutes,
      ],
    },
    {
      path: "*",
      element: <AccessDeniedPage />,
    },
  ]);

  return (
    <div>
      <React.Suspense fallback={<div>Loading...</div>}>
        <RouterProvider router={router} />
      </React.Suspense>
    </div>
  );
}
