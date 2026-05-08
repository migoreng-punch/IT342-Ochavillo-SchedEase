import { Link } from "react-router-dom";
import { LoginForm } from "../auth/components/LoginForm"; // Adjust path

export default function Login() {
  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4">
      <div className="w-full max-w-md bg-card shadow-soft rounded-xl p-8">
        
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-bold text-gray-900">
            SchedEase
          </h1>
          <p className="text-muted mt-2 text-sm">
            Sign in to your SchedEase account
          </p>
        </div>

        {/* The extracted feature component */}
        <LoginForm />

        <div className="mt-6 text-center text-sm text-muted">
          Don’t have an account?{" "}
          <Link
            to="/register"
            className="text-primary font-medium hover:underline"
          >
            Create one
          </Link>
        </div>

      </div>
    </div>
  );
}