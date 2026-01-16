import apiClient from "../lib/apt-client-sp";
import type { UpdateUserProfileData, UserProfile } from "../types/type";

// get user profile
export async function getUserProfile() : Promise<UserProfile> {
  try {
    const res = await apiClient.get("/users/get-profile");
    return res.data || res;
  } catch (e) {
    console.error("Error fetching user profile:", e);
    throw e;
  }
}
// update user profile
export async function updateUserProfile(
  data: Partial<UpdateUserProfileData>, 
  avatarFile?: File | null 
): Promise<UserProfile> {
  try {
    const formData = new FormData();

    if (avatarFile) {
      formData.append("avatar", avatarFile);
    }
    const jsonBlob = new Blob([JSON.stringify(data)], {
      type: "application/json",
    });

    formData.append("userProfileRequest", jsonBlob);

    console.log("FormData entries:", Array.from(formData.entries()));

    const res = await apiClient.patch("/users/update-profile", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });

    return res.data || res;
  } catch (e) {
    console.error("Error updating user profile:", e);
    throw e;
  }
}