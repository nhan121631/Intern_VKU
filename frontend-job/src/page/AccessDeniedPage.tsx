export const AccessDeniedPage = () => {
  return (
    <div className="flex items-center justify-center h-screen bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-96">
        <h2 className="text-2xl font-bold mb-6 text-center">Access Denied</h2>
        <p className="text-gray-600 text-center">
          You do not have permission to access this page.
        </p>
      </div>
    </div>
  );
};
