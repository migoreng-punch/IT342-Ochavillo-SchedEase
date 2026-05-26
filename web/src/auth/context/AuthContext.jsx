/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState, useEffect } from "react";
import api from "../../api/axios";
import { jwtDecode } from "jwt-decode";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(null);
  const [user, setUser] = useState(null);
  const [isAuthReady, setIsAuthReady] = useState(false);

  // Helper function to decode and set everything synchronously
  const processAndSetToken = (token) => {
    try {
      const decoded = jwtDecode(token);
      setUser({
        username: decoded.sub,
        firstName: decoded.firstname || "User",
        role: decoded.role,
        isEmailVerified: decoded.isEmailVerified === true
      });
      setAccessToken(token);
    } catch (error) {
      console.error("Failed to decode token:", error);
      setUser(null);
      setAccessToken(null);
    }
  };

  // 1. INITIAL LOAD
  useEffect(() => {
    const restoreSession = async () => {
      try {
        const response = await api.post("/api/auth/refresh");
        processAndSetToken(response.data.accessToken);
      } catch (error) {
        console.log("No active session found. User needs to log in.");
        setUser(null);
        setAccessToken(null);
      } finally {
        setIsAuthReady(true);
      }
    };

    restoreSession();
  }, []);

  // 2. STANDARD LOGIN (Makes API Call)
  const login = async (data) => {
    const response = await api.post("/api/auth/login", data);
    processAndSetToken(response.data.accessToken);
  };

  // 🚨 3. AUTO LOGIN (No API Call - Just sets state)
  const autoLogin = (token) => {
    processAndSetToken(token);
  };

  // 4. LOGOUT
  const logout = async () => {
    try {
      await api.post("/api/auth/logout");
    } catch (err) {
      console.error("Logout failed", err);
    } finally {
      setAccessToken(null);
      setUser(null);
    }
  };

  // 🚨 Don't forget to export autoLogin!
  return (
    <AuthContext.Provider value={{ accessToken, user, isAuthReady, login, autoLogin, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}