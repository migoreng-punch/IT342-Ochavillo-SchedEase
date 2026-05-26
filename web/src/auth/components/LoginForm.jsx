import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useAuth } from "../context/AuthContext"; // Adjust path to your AuthContext
import { InputField } from "../../components/InputField";

const schema = z.object({
  username: z.string().min(3, "Username is required"),
  password: z.string().min(6, "Password is required"),
});

export function LoginForm() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState(null);
  
  const { login } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data) => {
    setLoading(true);
    setServerError(null);

    try {
      await login(data); 
      navigate("/homepage"); // Redirect on success
    } catch {
      setServerError("Invalid username or password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
      {serverError && (
        <div className="text-red-500 text-sm bg-red-50 p-3 rounded-lg">
          {serverError}
        </div>
      )}

      <InputField
        label="Username"
        name="username"
        register={register}
        error={errors.username}
      />

      <InputField
        label="Password"
        name="password"
        type="password"
        register={register}
        error={errors.password}
      />

      <button
        type="submit"
        disabled={loading}
        className="w-full bg-[var(--color-primary)] text-white py-3 rounded-lg hover:bg-[var(--color-primaryDark)] transition disabled:opacity-60 font-medium"
      >
        {loading ? "Signing in..." : "Sign In"}
      </button>
    </form>
  );
}