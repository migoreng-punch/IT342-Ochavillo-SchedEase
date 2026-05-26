import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../auth/context/AuthContext"; // Adjust path
import { Loader2 } from "lucide-react";

export default function ProviderRoute() {
  const { user, isAuthReady } = useAuth();

  if (!isAuthReady) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );
  }

  // 1. If they aren't logged in at all, send to login
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // 2. If they are logged in but NOT a provider, send to client dashboard
  if (user.role !== "PROVIDER") {
    return <Navigate to="/dashboard" replace />;
  }

  // 3. If they are a provider, render the requested page!
  return <Outlet />;
}
