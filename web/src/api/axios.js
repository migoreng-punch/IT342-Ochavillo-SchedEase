import axios from "axios";

const api = axios.create({
  baseURL: "",
  withCredentials: true, // VERY IMPORTANT for refresh cookie
});

export default api;