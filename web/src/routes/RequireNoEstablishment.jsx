import { useState, useEffect } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { Loader2 } from "lucide-react";
import { useAuth } from "../auth/context/AuthContext";
import { useAxiosPrivate } from "../api/interceptor"; 

export function RequireNoEstablishment({ children }) {
  const { user, isAuthReady } = useAuth();
  const axiosPrivate = useAxiosPrivate();
  const location = useLocation();
  
  // States: "checking", "unverified", "invalid_role", "has_establishment", "no_establishment"
  const [status, setStatus] = useState("checking");

  useEffect(() => {
    let isMounted = true;

    const runSecurityChecks = async () => {
      // 1. Role Check: If they aren't a provider, bounce them.
      if (!user || user.role !== "PROVIDER") {
        if (isMounted) setStatus("invalid_role"); 
        return;
      }

      // 🚨 2. Verification Check: (The merged logic)
      if (!user.isEmailVerified) {
        if (isMounted) setStatus("unverified");
        return;
      }

      // 3. Database Check: Ping the backend to see if an establishment exists
      try {
        await axiosPrivate.get("/api/establishments/me");
        // If it succeeds, they ALREADY have one! Kick them out.
        if (isMounted) setStatus("has_establishment");
      } catch (error) {
        // If it throws a 404, they don't have one! Let them in.
        if (isMounted) setStatus("no_establishment");
      }
    };

    if (isAuthReady) {
      runSecurityChecks();
    }

    return () => {
      isMounted = false;
    };
  }, [user, isAuthReady, axiosPrivate]);

  // --- RENDER LOGIC ---

  // 1. Show a loading screen while we wait for auth or the API
  if (!isAuthReady || status === "checking") {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
      </div>
    );
  }

  // 🚨 2. Handle Unverified Users (Bounces to dashboard with a toast trigger)
  if (status === "unverified") {
    return (
      <Navigate 
        to="/dashboard" 
        replace 
        state={{ 
          alert: "verification_required",
          attemptedPath: location.pathname 
        }} 
      />
    );
  }

  // 3. Handle users who already have an establishment (or aren't providers)
  if (status === "has_establishment" || status === "invalid_role") {
    return <Navigate to="/provider" replace />;
  }

  // 4. If they pass all checks, render the Setup form!
  return children;
}