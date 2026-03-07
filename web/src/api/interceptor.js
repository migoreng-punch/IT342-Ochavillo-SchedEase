import api from "./axios";
import { useAuth } from "../context/AuthContext";

export function setupInterceptors(setAccessToken) {
  api.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true;

        try {
          const res = await api.post("/api/auth/refresh");
          setAccessToken(res.data.accessToken);

          originalRequest.headers[
            "Authorization"
          ] = `Bearer ${res.data.accessToken}`;

          return api(originalRequest);
        } catch (refreshError) {
          return Promise.reject(refreshError);
        }
      }

      return Promise.reject(error);
    }
  );
}