export default function HomePage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-100 to-purple-200">
      <div className="bg-white rounded-2xl shadow-2xl p-10 max-w-lg w-full text-center">
        <h1 className="text-4xl font-extrabold text-blue-700 mb-4">
          Welcome to <span className="text-purple-600">Task Management</span>
        </h1>
        <p className="text-gray-600 mb-8">
          Organize your tasks, boost your productivity, and achieve your goals
          with our simple and beautiful task management app.
        </p>
        <img
          src="https://cdn-icons-png.flaticon.com/512/3176/3176363.png"
          alt="Task Management"
          className="mx-auto mb-8 w-32 h-32"
        />
      </div>
    </div>
  );
}
