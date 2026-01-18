import { addDoc, collection, serverTimestamp } from "firebase/firestore";
import { db } from "../lib/fire-base";

export const createTaskAssignedNotification = async (
  userId: string,
  taskId: string,
  message: string,
) => {
  try {
    await addDoc(collection(db, "notifications"), {
      receiverId: userId,
      taskId,
      type: "TASK_ASSIGNED",
      message,
      isRead: false,
      createdAt: serverTimestamp(),
    });
  } catch (error) {
    console.error("Lỗi khi tạo notification:", error);
  }
};
