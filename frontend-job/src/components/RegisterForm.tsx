import { yupResolver } from "@hookform/resolvers/yup";
import { useForm, type SubmitHandler } from "react-hook-form";
import * as yup from "yup";

interface IRegister {
  fullName: string;
  username: string;
  password: string;
}

type Props = {
  onSubmit?: (creds: IRegister) => void;
};

const schema = yup.object({
  fullName: yup
    .string()
    .min(5, "Full name must be at least 5 characters")
    .required("Full name is required")
    .max(50, "Full name must be at most 50 characters"),
  username: yup
    .string()
    .min(5, "Username must be at least 5 characters")
    .required("Username is required"),
  password: yup
    .string()
    .min(8, "Password must be at least 8 characters")
    .required("Password is required"),
});

export default function RegisterForm({ onSubmit }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<IRegister>({
    resolver: yupResolver(schema),
  });

  const handleFormSubmit: SubmitHandler<IRegister> = async (data) => {
    if (onSubmit) onSubmit(data);
    else console.log(data);
  };

  return (
    <form
      onSubmit={handleSubmit(handleFormSubmit)}
      className="max-w-[360px] w-full font-sans"
    >
      <div className="mb-4">
        <label className="block mb-2 text-sm font-semibold text-gray-900">
          Full Name
        </label>
        <input
          type="text"
          {...register("fullName")}
          placeholder="Enter full name"
          className="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-50 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {errors.fullName && (
          <p className="text-red-500 text-sm mt-1">{errors.fullName.message}</p>
        )}
      </div>
      <div className="mb-4">
        <label className="block mb-2 text-sm font-semibold text-gray-900">
          Username
        </label>
        <input
          type="text"
          {...register("username")}
          placeholder="Enter username"
          className="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-50 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {errors.username && (
          <p className="text-red-500 text-sm mt-1">{errors.username.message}</p>
        )}
      </div>

      <div className="mb-4">
        <label className="block mb-2 text-sm font-semibold text-gray-900">
          Password
        </label>
        <input
          type="password"
          {...register("password")}
          placeholder="Enter password"
          className="w-full px-3 py-2 border border-gray-200 rounded-lg bg-gray-50 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        {errors.password && (
          <p className="text-red-500 text-sm mt-1">{errors.password.message}</p>
        )}
      </div>
      <div className="flex justify-center">
        <button
          type="submit"
          className="px-4 py-2 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 w-1/2"
        >
          Login
        </button>
      </div>

      <div className="mt-4 text-center text-sm text-gray-600">
        <p>
          Already have an account?{" "}
          <a href="/login" className="text-blue-600 hover:underline">
            Login here
          </a>
        </p>
      </div>
    </form>
  );
}
