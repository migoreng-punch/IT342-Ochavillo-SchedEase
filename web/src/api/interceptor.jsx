import { useEffect } from "react";
import { useAuth } from "../auth/context/AuthContext"; // Adjust path
import api from "./axios"; // Your base axios instance

export function useAxiosPrivate() {
  const { accessToken, setAccessToken } = useAuth();

  useEffect(() => {
    // 1. REQUEST INTERCEPTOR: Attach token to every outbound request
    const requestIntercept = api.interceptors.request.use(
      (config) => {
        // If the header doesn't exist yet, add the token from memory
        if (!config.headers["Authorization"] && accessToken) {
          config.headers["Authorization"] = `Bearer ${accessToken}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // 2. RESPONSE INTERCEPTOR: Your exact retry logic
    const responseIntercept = api.interceptors.response.use(
      (response) => response,
      async (error) => {
        const prevRequest = error?.config;
        
        if (error?.response?.status === 401 && !prevRequest?.sent) {
          prevRequest.sent = true; // Use .sent instead of ._retry (standard convention)
          console.log("🚨 401 Caught! Token expired. Attempting refresh...");

          try {
            // The browser automatically sends the HttpOnly refresh cookie here
            const response = await api.post("/api/auth/refresh");
            console.log("✅ Refresh successful! New token received.");
            // Save the new token to memory
            setAccessToken(response.data.accessToken);
            
            // Update the failed request with the new token and retry
            prevRequest.headers["Authorization"] = `Bearer ${response.data.accessToken}`;

            console.log("🔄 Retrying original request...");
            return api(prevRequest);
          } catch (refreshError) {
            // If the refresh fails (e.g., refresh token expired), they need to log in again
            console.log("❌ Refresh failed. Logging user out."); // ADD THIS
            setAccessToken(null);
            return Promise.reject(refreshError);
          }
        }
        return Promise.reject(error);
      }
    );

    // Cleanup function: remove interceptors when component unmounts 
    // to prevent memory leaks or duplicate interceptors
    return () => {
      api.interceptors.request.eject(requestIntercept);
      api.interceptors.response.eject(responseIntercept);
    };
  }, [accessToken, setAccessToken]); // Re-run if the token changes

  return api;
}