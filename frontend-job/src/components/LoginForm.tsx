/* eslint-disable @typescript-eslint/no-explicit-any */
import { yupResolver } from "@hookform/resolvers/yup";
import { GoogleLogin, GoogleOAuthProvider } from "@react-oauth/google";
import { useForm, type SubmitHandler } from "react-hook-form";
import * as yup from "yup";

interface Credentials {
  username: string;
  password: string;
}

type Props = {
  onSubmit?: (creds: Credentials) => void;
  loginWithGoogle?: (credential: string) => void;
};

const schema = yup.object({
  username: yup
    .string()
    .min(5, "Username must be at least 5 characters")
    .required("Username is required"),
  password: yup
    .string()
    .min(8, "Password must be at least 8 characters")
    .required("Password is required"),
});

export default function LoginForm({ onSubmit, loginWithGoogle }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<Credentials>({
    resolver: yupResolver(schema),
  });

  const handleFormSubmit: SubmitHandler<Credentials> = async (data) => {
    if (onSubmit) onSubmit(data);
    else console.log(data);
  };

  const handleSuccess = async (credentialResponse: any) => {
    if (loginWithGoogle) {
      loginWithGoogle(credentialResponse.credential);
    } else {
      console.log("Google Credential:", credentialResponse.credential);
    }
  };

  const handleError = () => {
    console.log("Google Login Failed");
  };

  return (
    <form
      onSubmit={handleSubmit(handleFormSubmit)}
      className="max-w-[360px] w-full font-sans"
    >
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
      <div className="flex justify-center flex-col items-center">
        <button
          type="submit"
          className="px-4 py-2 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 w-1/2"
        >
          Login
        </button>
        <GoogleOAuthProvider clientId={import.meta.env.VITE_CLIENT_ID || ""}>
          <div className="mt-6 ">
            <GoogleLogin onSuccess={handleSuccess} onError={handleError} />
          </div>
        </GoogleOAuthProvider>
      </div>
      <div className="mt-4 text-center text-sm text-gray-600">
        <p>
          Don't have an account?{" "}
          <a href="/register" className="text-blue-600 hover:underline">
            Register here
          </a>
        </p>
      </div>
    </form>
  );
}
