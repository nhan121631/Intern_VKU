/* eslint-disable @typescript-eslint/no-explicit-any */
import { useNavigate } from "react-router";
import { useAuthStore } from "../stores/useAuthorStore";
import { BarChart3, ClipboardList, Users } from "lucide-react";

export default function HomePage() {
  const { loggedInUser } = useAuthStore((state) => state);
  const navigate = useNavigate();
  const handleGetStarted = () => {
    if (!loggedInUser) {
      navigate("/login");
      return;
    }

    const t = setTimeout(() => {
      // Handle both string[] and object[] roles
      const roleNames = loggedInUser.roles.map((r: any) =>
        typeof r === "string" ? r : r.name,
      );

      if (roleNames.includes("Administrators")) {
        navigate("/our-task");
      } else {
        navigate("/my-task");
      }
    }, 300);

    return () => clearTimeout(t);
  };

  const handleViewMyTasks = () => {
    if (loggedInUser?.id) {
      navigate("/my-task");
    } else {
      navigate("/login");
    }
  };
  return (
    <div className="min-h-screen bg-linear-to-br from-blue-50 via-white to-purple-50">
      {/* Hero Section */}
      <section className="container mx-auto px-6 py-20 text-center">
        <div className="max-w-4xl mx-auto">
          <h1 className="text-5xl md:text-6xl font-bold text-gray-900 mb-6">
            Manage Your Tasks{" "}
            <span className="text-transparent bg-clip-text bg-linear-to-r from-blue-600 to-purple-600">
              Effortlessly
            </span>
          </h1>
          <p className="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
            Streamline your workflow, collaborate with your team, and achieve
            more with our intuitive task management platform.
          </p>
          <div className="flex gap-4 justify-center">
            <a
              onClick={handleGetStarted}
              className="bg-blue-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-blue-700 transition-colors cursor-pointer"
            >
              Get Started
            </a>
            <a
              onClick={handleViewMyTasks}
              className="bg-white text-blue-600 px-8 py-3 rounded-lg font-semibold border-2 border-blue-600 hover:bg-blue-50 transition-colors cursor-pointer"
            >
              View My Tasks
            </a>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="container mx-auto px-6 py-20">
        <h2 className="text-3xl font-bold text-center text-gray-900 mb-12">
          Why Choose Our Platform?
        </h2>
        <div className="grid md:grid-cols-3 gap-8">
          {/* Feature 1 */}
          <div className="bg-white rounded-xl p-8 shadow-lg hover:shadow-xl transition-shadow">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-4">
              <ClipboardList className="w-8 h-8 text-blue-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              Task Organization
            </h3>
            <p className="text-gray-600">
              Create, assign, and track tasks with ease. Keep everything
              organized in one place.
            </p>
          </div>

          {/* Feature 2 */}
          <div className="bg-white rounded-xl p-8 shadow-lg hover:shadow-xl transition-shadow">
            <div className="w-16 h-16 bg-purple-100 rounded-full flex items-center justify-center mb-4">
              <Users className="w-8 h-8 text-purple-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              Team Collaboration
            </h3>
            <p className="text-gray-600">
              Work together seamlessly. Assign tasks to team members and track
              progress.
            </p>
          </div>

          {/* Feature 3 */}
          <div className="bg-white rounded-xl p-8 shadow-lg hover:shadow-xl transition-shadow">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mb-4">
              <BarChart3 className="w-8 h-8 text-green-600" />
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              Track Progress
            </h3>
            <p className="text-gray-600">
              Monitor task status, view history, and export reports to stay on
              top of everything.
            </p>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="container mx-auto px-6 py-20">
        <div className="bg-linear-to-r from-blue-600 to-purple-600 rounded-2xl p-12 text-center text-white">
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            Ready to Get Started?
          </h2>
          <p className="text-xl mb-8 opacity-90">
            Join thousands of teams managing their tasks efficiently.
          </p>
          <a
            onClick={() => navigate("/register")}
            className="inline-block bg-white text-blue-600 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-colors cursor-pointer"
          >
            Create Your Account
          </a>
        </div>
      </section>
    </div>
  );
}
