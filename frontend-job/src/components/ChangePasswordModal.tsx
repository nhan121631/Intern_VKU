import { Check, Loader2, X } from "lucide-react";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";

export interface IChangePassword {
  currentPassword: string;
  newPassword: string;
  confirmNewPassword: string;
}
const schema = yup.object({
  currentPassword: yup.string().required("Current password is required"),
  newPassword: yup
    .string()
    .min(8, "New password must be at least 8 characters")
    .matches(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
      "New password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    .required("New password is required"),
  confirmNewPassword: yup
    .string()
    .oneOf([yup.ref("newPassword")], "Passwords must match")
    .required("Please confirm your new password"),
});

type Props = {
  isOpen: boolean;
  saving?: boolean;
  onClose: () => void;
  onSubmit?: (data: IChangePassword) => void;
};

export default function ChangePasswordModal({
  isOpen,
  saving,
  onClose,
  onSubmit,
}: Props) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<IChangePassword>({
    resolver: yupResolver(schema),
  });

  useEffect(() => {
    if (!isOpen) {
      reset();
    }
  }, [isOpen, reset]);
  if (!isOpen) return null;

  const onSubmitForm = async (data: IChangePassword) => {
    console.log("Change password data:", data);
    if (onSubmit) onSubmit(data);
    reset();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <form onSubmit={handleSubmit(onSubmitForm)} className="w-full max-w-md">
        <div
          className="fixed inset-0 bg-black/60 backdrop-blur-sm"
          onClick={() => {
            if (!saving) onClose();
          }}
        />

        <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-lg p-6">
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-xl font-bold text-gray-900">Change Password</h3>
            <button
              type="button"
              onClick={() => {
                if (!saving) onClose();
              }}
              className="text-gray-400 hover:text-gray-600 hover:bg-gray-100 p-2 rounded-lg transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          <div className="space-y-5">
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-900">
                Current Password
              </label>
              <input
                type="password"
                {...register("currentPassword")}
                placeholder="Enter current password"
                className="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-50 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              {errors.currentPassword && (
                <p className="text-red-500 text-sm mt-1">
                  {errors.currentPassword.message}
                </p>
              )}
            </div>
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-900">
                New Password
              </label>
              <input
                type="password"
                {...register("newPassword")}
                placeholder="Enter new password"
                className="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-50 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              {errors.newPassword && (
                <p className="text-red-500 text-sm mt-1">
                  {errors.newPassword.message}
                </p>
              )}
            </div>
            <div>
              <label className="block mb-2 text-sm font-semibold text-gray-900">
                Confirm New Password
              </label>
              <input
                type="password"
                {...register("confirmNewPassword")}
                placeholder="Confirm new password"
                className="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-50 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              {errors.confirmNewPassword && (
                <p className="text-red-500 text-sm mt-1">
                  {errors.confirmNewPassword.message}
                </p>
              )}
            </div>
          </div>

          <div className="flex justify-end gap-3 mt-6">
            <button
              type="button"
              onClick={() => {
                if (!saving) onClose();
              }}
              className="px-5 py-2.5 rounded-lg bg-gray-100 hover:bg-gray-200 transition-colors font-medium"
              disabled={saving}
            >
              Cancel
            </button>

            <button
              type="submit"
              disabled={saving}
              className="px-5 py-2.5 rounded-lg bg-blue-600 text-white hover:bg-blue-700 transition-colors flex items-center gap-2 font-medium disabled:opacity-50"
            >
              {saving ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" /> Saving...
                </>
              ) : (
                <>
                  <Check className="w-4 h-4" /> Save
                </>
              )}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
