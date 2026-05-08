import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { InputField } from "../../components/InputField";

const schema = z.object({
  firstName: z.string().min(2, "First name is required"),
  lastName: z.string().min(2, "Last name is required"),
  username: z.string().min(3, "Username must be at least 3 characters"),
  email: z.email("Invalid email"),
  password: z.string().min(6, "Password must be at least 6 characters"),
});

export function RegisterForm() {
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState(null);
  const navigate = useNavigate();

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
      const response = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(data),
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text);
      }

      navigate("/login");
    } catch (err) {
      setServerError(err.message);
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
        label="First Name"
        name="firstName"
        register={register}
        error={errors.firstName}
      />

      <InputField
        label="Last Name"
        name="lastName"
        register={register}
        error={errors.lastName}
      />

      <InputField
        label="Email"
        name="email"
        register={register}
        error={errors.email}
        type="email"
      />

      <InputField
        label="Password"
        name="password"
        register={register}
        error={errors.password}
        type="password"
      />

      <button
        type="submit"
        disabled={loading}
        className="w-full bg-[var(--color-primary)] text-white py-3 rounded-lg hover:bg-[var(--color-primaryDark)] transition disabled:opacity-60 font-medium"
      >
        {loading ? "Creating account..." : "Create Account"}
      </button>
    </form>
  );
}