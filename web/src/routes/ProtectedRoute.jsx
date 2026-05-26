import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/context/AuthContext";
import { Loader2 } from "lucide-react"; // Optional: A nice spinner

export default function ProtectedRoute({ children }) {
  const { user, isAuthReady } = useAuth();

  // 1. Wait for AuthContext to finish checking the session
  if (!isAuthReady) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );
  }

  // 2. If it's done checking and there's no user, kick them out
  if (!user) {
    return <Navigate to="/login" replace />; // Use 'replace' so they can't hit the back button to return here
  }

  // 4. Safe to render the page!
  return children;
}