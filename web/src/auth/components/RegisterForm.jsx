import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { InputField } from "../../components/InputField";

// 🚨 1. Import your AuthContext here!
import { useAuth } from "../context/AuthContext"; // Adjust this path to wherever your context lives

// 🚨 Added phoneNumber and address to the validation schema
const schema = z.object({
  firstName: z.string().min(2, "First name is required"),
  lastName: z.string().min(2, "Last name is required"),
  username: z.string().min(3, "Username must be at least 3 characters"),
  email: z.email("Invalid email"),
  phoneNumber: z
    .string()
    .regex(
      /^09\d{9}$/,
      "Please enter a valid 11-digit number (e.g., 09123456789)",
    ),
  address: z.string().min(5, "Please enter your full address"), // New!
  password: z.string().min(6, "Password must be at least 6 characters"),
});

export function RegisterForm() {
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState(null);
  const [role, setRole] = useState("CLIENT");
  const navigate = useNavigate();

  // 1. Pull the new autoLogin function from context
  const { autoLogin } = useAuth();

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

    const payload = {
      ...data,
      role: role,
    };

    try {
      const response = await fetch("http://localhost:8080/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        let errorMsg = "Registration failed. Please try again.";
        try {
          const errData = await response.json();
          errorMsg = errData.message || errorMsg;
        } catch {
          errorMsg = await response.text();
        }
        throw new Error(errorMsg);
      }

      const responseData = await response.json();

      // 2. Use autoLogin, passing just the token string from your backend's LoginResponse!
      autoLogin(responseData.accessToken);

      navigate("/homepage");
    } catch (err) {
      setServerError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
      {/* Role Selection Toggle */}
      <div className="flex gap-3 mb-6">
        <button
          type="button"
          onClick={() => setRole("CLIENT")}
          className={`flex-1 py-2.5 px-4 rounded-lg font-medium text-sm transition-all border ${
            role === "CLIENT"
              ? "bg-blue-600 text-white shadow-md border-transparent"
              : "bg-white text-gray-700 border border-gray-200 hover:bg-gray-50"
          }`}
        >
          Book Appointments
        </button>

        <button
          type="button"
          onClick={() => setRole("PROVIDER")}
          className={`flex-1 py-2.5 px-4 rounded-lg font-medium text-sm transition-all border ${
            role === "PROVIDER"
              ? "bg-blue-600 text-white shadow-md border-transparent"
              : "bg-white text-gray-700 border border-gray-200 hover:bg-gray-50"
          }`}
        >
          Service Provider
        </button>
      </div>

      {serverError && (
        <div className="text-red-500 text-sm bg-red-50 p-3 rounded-lg border border-red-100">
          {serverError}
        </div>
      )}

      <InputField
        label="Username"
        name="username"
        register={register}
        error={errors.username}
      />

      <div className="grid grid-cols-2 gap-4">
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
      </div>

      {/* 🚨 NEW: Phone Number Field */}
      <InputField
        label="Phone Number"
        name="phoneNumber"
        register={register}
        error={errors.phoneNumber}
        type="tel"
        // 🚨 Pass the custom onChange into our new validation prop!
        validation={{
          onChange: (e) => {
            // 1. Strip out everything that is NOT a number
            let formatted = e.target.value.replace(/\D/g, "");

            // 2. Cap the length at 11 digits
            if (formatted.length > 11) {
              formatted = formatted.slice(0, 11);
            }

            // 3. Update the input value in the UI so React Hook Form catches it
            e.target.value = formatted;
          },
        }}
      />

      {/* 🚨 NEW: Address Field */}
      <InputField
        label="Address"
        name="address"
        register={register}
        error={errors.address}
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
        className="w-full bg-[var(--color-primary)] text-white py-3 rounded-lg hover:bg-[var(--color-primaryDark)] transition disabled:opacity-60 font-medium mt-6"
      >
        {loading ? "Creating account..." : "Create Account"}
      </button>
    </form>
  );
}
