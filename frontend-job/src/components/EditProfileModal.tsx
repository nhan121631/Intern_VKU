/* eslint-disable @typescript-eslint/no-explicit-any */
import { yupResolver } from "@hookform/resolvers/yup";
import { Camera, Check, Loader2, X } from "lucide-react";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import * as yup from "yup";
import { IMAGE_URL } from "../service/Constant";
import type { UpdateUserProfileData, UserProfile } from "../types/type";

const profileSchema = yup.object({
  id: yup.number().required(),
  fullName: yup
    .string()
    .required("Full name is required")
    .min(2, "Full name must be at least 2 characters")
    .max(50, "Full name must be at most 50 characters"),
  phoneNumber: yup
    .string()
    .test("is-phone", "Invalid phone number (10-15 digits)", (value) => {
      if (!value) return true;
      return /^\+?[0-9]{10,15}$/.test(value);
    })
    .default(undefined),
  address: yup.string().default(undefined),
  avatarUrl: yup.string().default(undefined),
});

type ProfileFormData = {
  id: number;
  fullName: string;
  phoneNumber?: string;
  address?: string;
  avatarUrl?: string;
};

type Props = {
  isOpen: boolean;
  profile?: UserProfile | null;
  saving?: boolean;
  onClose: () => void;
  onSave: (data: UpdateUserProfileData, avatar?: File) => Promise<void> | void;
};

export default function EditProfileModal({
  isOpen,
  profile,
  saving,
  onClose,
  onSave,
}: Props) {
  const [avatarFile, setAvatarFile] = useState<File | undefined>(undefined);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<ProfileFormData>({
    resolver: yupResolver(profileSchema) as any,
    defaultValues: {
      id: 0,
      fullName: "",
      phoneNumber: "",
      address: "",
      avatarUrl: "",
    },
  });

  // eslint-disable-next-line react-hooks/incompatible-library
  const currentAvatarUrl = watch("avatarUrl");

  useEffect(() => {
    if (isOpen && profile) {
      reset({
        id: profile.id || 0,
        fullName: profile.fullName || "",
        phoneNumber: profile.phoneNumber || "",
        address: profile.address || "",
        avatarUrl: profile.avatarUrl || "",
      });
      setAvatarFile(undefined);
    }
  }, [isOpen, profile, reset]);

  if (!isOpen) return null;

  const handleFileChange = (file?: File) => {
    if (!file) return;

    setAvatarFile(file);

    const reader = new FileReader();
    reader.onload = () => {
      const result = reader.result as string;
      setValue("avatarUrl", result, { shouldDirty: true });
    };
    reader.readAsDataURL(file);
  };

  // const handleRemoveAvatar = () => {
  //   setValue("avatarUrl", "");
  //   setAvatarFile(undefined);
  // };

  const onSubmit = async (data: ProfileFormData) => {
    const submitData: UpdateUserProfileData = {
      id: data.id,
      fullName: data.fullName,
      phoneNumber: data.phoneNumber || "",
      address: data.address || "",
    };

    console.log("Submitting:", submitData, avatarFile);
    await onSave(submitData, avatarFile);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div
        className="fixed inset-0 bg-black/60 backdrop-blur-sm"
        onClick={() => !saving && onClose()}
      />

      <div className="relative bg-white rounded-2xl shadow-2xl w-full max-w-lg p-6">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-xl font-bold text-gray-900">Edit Profile</h3>
          <button
            type="button"
            onClick={() => !saving && onClose()}
            className="text-gray-400 hover:text-gray-600 hover:bg-gray-100 p-2 rounded-lg transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
          <div className="flex items-center gap-4">
            <div className="relative">
              <div className="w-28 h-28 rounded-full overflow-hidden border-4 border-gray-100 bg-gray-100 flex items-center justify-center shadow-md">
                {currentAvatarUrl ? (
                  <img
                    src={
                      currentAvatarUrl.startsWith("data:")
                        ? currentAvatarUrl
                        : IMAGE_URL + currentAvatarUrl
                    }
                    alt="preview"
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <Camera className="w-8 h-8 text-gray-400" />
                )}
              </div>
            </div>

            <div className="flex-1">
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Avatar
              </label>
              <div className="flex flex-wrap gap-2">
                <input
                  type="file"
                  accept="image/*"
                  onChange={(e) => handleFileChange(e.target.files?.[0])}
                  className="hidden"
                  id="avatar-file-input"
                />
                <button
                  type="button"
                  onClick={() =>
                    document.getElementById("avatar-file-input")?.click()
                  }
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
                >
                  Choose file
                </button>
                {/* <button
                  type="button"
                  onClick={handleRemoveAvatar}
                  className="px-4 py-2 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 transition-colors text-sm font-medium"
                >
                  Remove
                </button> */}
              </div>
              <p className="text-xs text-gray-500 mt-2">PNG, JPG — up to 2MB</p>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Full name <span className="text-red-500">*</span>
              </label>
              <input
                {...register("fullName")}
                placeholder="Enter full name"
                className={`w-full px-4 py-2.5 border-2 rounded-lg focus:outline-none transition-all ${
                  errors.fullName
                    ? "border-red-500 focus:border-red-500 focus:ring-red-100"
                    : "border-gray-200 focus:border-blue-500 focus:ring-blue-100"
                }`}
              />
              {errors.fullName && (
                <p className="text-red-500 text-xs mt-1">
                  {errors.fullName.message}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Phone
              </label>
              <input
                {...register("phoneNumber")}
                placeholder="0123 456 789"
                className={`w-full px-4 py-2.5 border-2 rounded-lg focus:outline-none transition-all ${
                  errors.phoneNumber
                    ? "border-red-500 focus:border-red-500 focus:ring-red-100"
                    : "border-gray-200 focus:border-blue-500 focus:ring-blue-100"
                }`}
              />
              {errors.phoneNumber && (
                <p className="text-red-500 text-xs mt-1">
                  {errors.phoneNumber.message}
                </p>
              )}
            </div>
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Address
            </label>
            <input
              {...register("address")}
              placeholder="Street, City, Country"
              className="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all"
            />
          </div>

          <div className="flex justify-end gap-3 mt-6">
            <button
              type="button"
              onClick={() => !saving && onClose()}
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
        </form>
      </div>
    </div>
  );
}
