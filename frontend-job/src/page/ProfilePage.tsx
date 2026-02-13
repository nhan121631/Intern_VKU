import {
  Edit2,
  Loader2,
  Lock,
  MapPin,
  Phone,
  User,
  type LucideIcon,
} from "lucide-react";
import { memo, useCallback, useEffect, useState } from "react";
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

// Constants (rendering-hoist-jsx)
const NOT_AVAILABLE = "Not Available";
const DEFAULT_AVATAR = "./../assets/default-avatar.jpg";

// Types
interface ProfileFieldProps {
  icon: LucideIcon;
  iconBgColor: string;
  iconColor: string;
  label: string;
  value: string;
}

interface NotificationState {
  message: string;
  type: "success" | "error" | "";
}

// Memoized ProfileField component (rerender-memo)
const ProfileField = memo<ProfileFieldProps>(
  ({ icon: Icon, iconBgColor, iconColor, label, value }) => (
    <div className="flex gap-3 items-center">
      <div
        className={`w-10 h-10 ${iconBgColor} rounded-lg flex items-center justify-center shrink-0`}
      >
        <Icon className={`w-5 h-5 ${iconColor}`} />
      </div>
      <div className="flex-1">
        <span className="text-white dark:text-gray-300 font-medium text-sm">
          {label}
        </span>
        <p className="text-white dark:text-gray-100 font-semibold">
          {value || NOT_AVAILABLE}
        </p>
      </div>
    </div>
  ),
);
ProfileField.displayName = "ProfileField";

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserProfile>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isChangeOpen, setIsChangeOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  // Consolidated notification state (rerender-derived-state-no-effect)
  const [notification, setNotification] = useState<NotificationState>({
    message: "",
    type: "",
  });

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await getUserProfile();
        setProfile(response);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load profile");
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  // Stable callbacks (rerender-functional-setstate)
  const showNotification = useCallback(
    (message: string, type: "success" | "error") => {
      setNotification({ message, type });
    },
    [],
  );

  const handleSave = useCallback(
    async (updated: UpdateUserProfileData, avatar?: File) => {
      setSaving(true);
      try {
        const response = await updateUserProfile(updated, avatar);
        setProfile(response);
        setIsEditOpen(false);
        showNotification("Profile updated successfully!", "success");
      } catch (err) {
        const errorMessage =
          err instanceof Error
            ? err.message
            : "Failed to update profile. Please try again.";
        showNotification(errorMessage, "error");
      } finally {
        setSaving(false);
      }
    },
    [showNotification],
  );

  const handlePasswordChange = useCallback(
    async (data: IChangePassword) => {
      try {
        await changeUserPassword(data.currentPassword, data.newPassword);
        setIsChangeOpen(false);
        showNotification("Password changed successfully!", "success");
      } catch (err) {
        const errorMessage =
          err instanceof Error
            ? err.message
            : "Failed to change password. Please try again.";
        showNotification(errorMessage, "error");
      }
    },
    [showNotification],
  );

  const closeNotification = useCallback(() => {
    setNotification({ message: "", type: "" });
  }, []);

  // Early exit pattern (js-early-exit)
  if (loading) {
    return (
      <div className="p-6 max-w-3xl mx-auto">
        <div className="text-center py-8">
          <Loader2 className="animate-spin mx-auto w-8 h-8 text-blue-600" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6 max-w-3xl mx-auto">
        <div className="text-red-500 text-center bg-red-50 py-3 px-4 rounded-lg dark:bg-red-900 dark:text-red-200">
          {error}
        </div>
      </div>
    );
  }

  if (!profile) {
    return null;
  }

  return (
    <div className="p-6 max-w-3xl mx-auto">
      <div className="bg-white shadow-lg rounded-xl p-8 bg-linear-to-t from-sky-400 to-indigo-400 dark:from-transparent dark:to-transparent dark:bg-gray-700 text-white dark:text-gray-100">
        <div className="flex flex-col md:flex-row items-center md:items-start gap-6">
          <img
            src={
              profile.avatarUrl ? IMAGE_URL + profile.avatarUrl : DEFAULT_AVATAR
            }
            alt={`${profile.fullName || "User"}'s avatar`}
            className="w-32 h-32 rounded-full border-4 border-gray-100 dark:border-gray-700 shadow-md object-cover"
          />

          <div className="flex-1 space-y-4 w-full">
            <div className="flex justify-between items-center gap-10">
              <h2 className="text-2xl font-bold mb-4 text-white dark:text-gray-100">
                Profile Information
              </h2>
              <div className="flex justify-end mb-4 gap-3">
                <button
                  onClick={() => setIsEditOpen(true)}
                  className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors dark:bg-blue-600 dark:hover:bg-blue-700"
                  aria-label="Edit profile"
                >
                  <Edit2 className="w-4 h-4" />
                  Edit
                </button>
                <button
                  onClick={() => setIsChangeOpen(true)}
                  className="flex items-center px-4 gap-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors dark:bg-blue-600 dark:hover:bg-blue-700"
                  aria-label="Change password"
                >
                  <Lock className="w-4 h-4" />
                  Change Pass
                </button>
              </div>
            </div>

            <ProfileField
              icon={User}
              iconBgColor="bg-blue-100 dark:bg-blue-900"
              iconColor="text-blue-600 dark:text-blue-300"
              label="Full Name"
              value={profile.fullName}
            />

            <ProfileField
              icon={Phone}
              iconBgColor="bg-green-100 dark:bg-green-900"
              iconColor="text-green-600 dark:text-green-300"
              label="Phone"
              value={profile.phoneNumber}
            />

            <ProfileField
              icon={MapPin}
              iconBgColor="bg-purple-100 dark:bg-purple-900"
              iconColor="text-purple-600 dark:text-purple-300"
              label="Address"
              value={profile.address}
            />
          </div>
        </div>
      </div>
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
        onSubmit={handlePasswordChange}
      />

      {notification.type !== "" && (
        <Notification
          message={notification.message}
          type={notification.type}
          onClose={closeNotification}
          duration={1500}
        />
      )}
    </div>
  );
}
