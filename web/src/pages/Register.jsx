import { Link } from "react-router-dom";
import { RegisterForm } from "../auth/components/RegisterForm"; // Adjust path if needed

export default function Register() {
  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4">
      <div className="w-full max-w-md bg-card shadow-soft rounded-xl p-8">
        
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-gray-900">
            Create your account
          </h1>
          <p className="text-muted mt-2 text-sm">
            Join SchedEase and manage appointments effortlessly
          </p>
        </div>

        {/* The extracted feature component */}
        <RegisterForm />

        <div className="mt-6 text-center text-sm text-muted">
          Already have an account?{" "}
          <Link
            to="/login"
            className="text-primary font-medium hover:underline"
          >
            Sign in
          </Link>
        </div>
        
      </div>
    </div>
  );
}