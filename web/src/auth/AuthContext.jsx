import { createContext, useContext, useState } from "react";
import api from "../api/axios";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState(null);

  const login = async (data) => {
    const response = await api.post("/api/auth/login", data);
    setAccessToken(response.data.accessToken);
  };

  const logout = async () => {
    await api.post("/api/auth/logout");
    setAccessToken(null);
  };

  return (
    <AuthContext.Provider value={{ accessToken, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}