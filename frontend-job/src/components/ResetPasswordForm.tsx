import { yupResolver } from "@hookform/resolvers/yup";
import { useForm, type SubmitHandler } from "react-hook-form";
import { useNavigate } from "react-router";
import * as yup from "yup";

export interface ResetPasswordFormData {
  newPassword: string;
  confirmNewPassword: string;
}
type Props = {
  onSubmit?: (data: ResetPasswordFormData) => void;
};

const schema = yup.object({
  newPassword: yup
    .string()
    .min(8, "Password must be at least 8 characters")
    .matches(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
      "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    .required("New password is required"),
  confirmNewPassword: yup
    .string()
    .oneOf([yup.ref("newPassword")], "Passwords must match")
    .required("Please confirm your new password"),
});
export default function ResetPasswordForm({ onSubmit }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<ResetPasswordFormData>({
    resolver: yupResolver(schema),
  });
  const navigate = useNavigate();

  const handleFormSubmit: SubmitHandler<ResetPasswordFormData> = async (
    data
  ) => {
    if (onSubmit) onSubmit(data);
    else console.log(data);
  };

  return (
    <form
      onSubmit={handleSubmit(handleFormSubmit)}
      className="max-w-90 w-full font-sans"
    >
      <div className="mb-4">
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

      <div className="mb-4">
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

      <div className="flex justify-center">
        <button
          type="submit"
          className="px-4 py-2 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 w-1/2"
        >
          Reset Password
        </button>
      </div>

      <div className="mt-4 text-center text-sm text-gray-600">
        Remembered your password?{" "}
        <a
          onClick={() => {
            navigate("/login");
          }}
          className="text-blue-600 hover:underline"
        >
          Login here
        </a>
      </div>
    </form>
  );
}
