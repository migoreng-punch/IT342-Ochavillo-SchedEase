import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: true, // VERY IMPORTANT for refresh cookie
});

export default api;