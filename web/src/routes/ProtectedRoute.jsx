import { Navigate } from "react-router-dom";
import { useAuth } from "../auth/context/AuthContext";

export default function ProtectedRoute({ children }) {
  const { accessToken } = useAuth();

  if (!accessToken) {
    return <Navigate to="/login" />;
  }

  return children;
}