/* eslint-disable @typescript-eslint/no-explicit-any */
import { Edit2, Loader2, Lock, MapPin, Phone, User } from "lucide-react";
import { useEffect, useState } from "react";
import EditProfileModal from "../components/EditProfileModal";
import Notification from "../components/Notification";
import { IMAGE_URL } from "../service/Constant";
import {
  getUserProfile,
  updateUserProfile,
} from "../service/UserProfileService";
import type { UpdateUserProfileData, UserProfile } from "../types/type";
import ChangePasswordModal, {
  type IChangePassword,
} from "../components/ChangePasswordModal";
import { changeUserPassword } from "../service/UserService";

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserProfile>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isChangeOpen, setIsChangeOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string>("");
  const [errorMessage, setErrorMessage] = useState<string>("");

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await getUserProfile();
        setProfile(response);
      } catch (err) {
        setError((err as Error).message);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const handleEdit = () => {
    setIsEditOpen(true);
  };

  const handleSave = async (updated: UpdateUserProfileData, avatar?: File) => {
    try {
      setSaving(true);
      const response = await updateUserProfile(updated, avatar);
      console.log("Saving profile:", updated, avatar);
      setSuccessMessage("Profile updated successfully!");
      setProfile(response);
      setIsEditOpen(false);
    } catch (e: any) {
      console.error("Error saving profile:", e);
      setErrorMessage(
        e.message ||
          e.errors[0] ||
          "Failed to update profile. Please try again."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleSubmit = async (data: IChangePassword) => {
    try {
      await changeUserPassword(data.currentPassword, data.newPassword);
      setSuccessMessage("Password changed successfully!");
      setIsChangeOpen(false);
    } catch (e: any) {
      console.error("Error changing password:", e);
      setErrorMessage(
        e.message ||
          e.errors[0] ||
          "Failed to change password. Please try again."
      );
    }
  };

  return (
    <div className="p-6 max-w-3xl mx-auto ">
      {loading && (
        <div className="text-center py-8">
          <Loader2 className="animate-spin mx-auto w-8 h-8 text-blue-600" />
        </div>
      )}

      {error && (
        <div className="text-red-500 text-center bg-red-50 py-3 px-4 rounded-lg">
          {error}
        </div>
      )}

      {profile && (
        <div className="bg-white shadow-lg rounded-xl p-8 bg-linear-to-t from-sky-400 to-indigo-400">
          <div className="flex flex-col md:flex-row items-center md:items-start gap-6">
            <img
              src={
                profile.avatarUrl
                  ? IMAGE_URL + profile.avatarUrl
                  : "./../assets/default-avatar.jpg"
              }
              alt="Avatar"
              className="w-32 h-32 rounded-full border-4 border-gray-100 shadow-md object-cover"
            />

            <div className="flex-1 space-y-4 w-full">
              <div className="flex justify-between items-center gap-10">
                <h2 className="text-2xl font-bold mb-4 text-white">
                  Profile Information
                </h2>
                <div className="flex justify-end mb-4 gap-3">
                  <button
                    onClick={handleEdit}
                    className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors cursor-pointer"
                  >
                    <Edit2 className="w-4 h-4" />
                    Edit
                  </button>
                  <button
                    onClick={() => setIsChangeOpen(true)}
                    className="flex items-center px-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors cursor-pointer"
                  >
                    <Lock className="w-4 h-4" />
                    Change Password
                  </button>
                </div>
              </div>

              <div className="flex gap-3 items-center">
                <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center shrink-0">
                  <User className="w-5 h-5 text-blue-600" />
                </div>
                <div className="flex-1">
                  <span className="text-white font-medium text-sm">
                    Full Name
                  </span>
                  <p className="text-white font-semibold">
                    {profile.fullName || "Not Available"}
                  </p>
                </div>
              </div>

              <div className="flex gap-3 items-center">
                <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center shrink-0">
                  <Phone className="w-5 h-5 text-green-600" />
                </div>
                <div className="flex-1">
                  <span className="text-white font-medium text-sm">Phone</span>
                  <p className="text-white font-semibold">
                    {profile.phoneNumber || "Not Available"}
                  </p>
                </div>
              </div>

              <div className="flex gap-3 items-center">
                <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center shrink-0">
                  <MapPin className="w-5 h-5 text-purple-600" />
                </div>
                <div className="flex-1">
                  <span className="text-white font-medium text-sm">
                    Address
                  </span>
                  <p className="text-white font-semibold">
                    {profile.address || "Not Available"}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
      <EditProfileModal
        isOpen={isEditOpen}
        profile={profile}
        saving={saving}
        onClose={() => setIsEditOpen(false)}
        onSave={handleSave}
      />
      <ChangePasswordModal
        isOpen={isChangeOpen}
        saving={false}
        onClose={() => setIsChangeOpen(false)}
        onSubmit={handleSubmit}
      />
      <Notification
        message={successMessage}
        type="success"
        onClose={() => setSuccessMessage("")}
        duration={1500}
      />
      <Notification
        message={errorMessage}
        type="error"
        onClose={() => setErrorMessage("")}
        duration={1500}
      />
    </div>
  );
}
