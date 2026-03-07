import api from "../api/axios";
import { useAuth } from "../auth/AuthContext";

export default function Topbar() {
  const { setAccessToken } = useAuth();

  const handleLogout = async () => {
    await api.post("/api/auth/logout");
    setAccessToken(null);
  };

  return (
    <div className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-8">
      <div className="text-lg font-semibold">
        Dashboard
      </div>

      <button
        onClick={handleLogout}
        className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition"
      >
        Logout
      </button>
    </div>
  );
}