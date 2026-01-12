import MyTaskListContainer from "../components/MyTaskListController";

export default function MyTaskPage() {
  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">My Task Page</h1>
      <MyTaskListContainer />
    </div>
  );
}
