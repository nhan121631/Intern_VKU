import { yupResolver } from "@hookform/resolvers/yup";
import { useForm, type SubmitHandler } from "react-hook-form";
import { useNavigate } from "react-router";
import * as yup from "yup";

type Props = {
  onSubmit?: (email: string) => void;
};

const schema = yup.object({
  email: yup
    .string()
    .email("Invalid email format")
    .required("Email is required"),
});

export default function ForgotPassForm({ onSubmit }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<{ email: string }>({
    resolver: yupResolver(schema),
  });
  const navigate = useNavigate();

  const handleFormSubmit: SubmitHandler<{ email: string }> = async (data) => {
    if (onSubmit) onSubmit(data.email);
    else console.log(data);
  };

  return (
    <form
      onSubmit={handleSubmit(handleFormSubmit)}
      className="max-w-90 w-full font-sans"
    >
      <div className="mb-4">
        <label className="block mb-2 text-sm font-semibold text-gray-900">
          Email
        </label>
        <input
          type="email"
          {...register("email")}
          placeholder="Enter email"
          className="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-50 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {errors.email && (
          <p className="text-red-500 text-sm mt-1">{errors.email.message}</p>
        )}
      </div>

      <div className="flex justify-center">
        <button
          type="submit"
          className="px-4 py-2 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 w-1/2"
        >
          Send OTP
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
