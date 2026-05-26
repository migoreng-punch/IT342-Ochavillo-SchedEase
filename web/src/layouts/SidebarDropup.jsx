import React, { useState, useRef, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { LogOut, User, Settings, Calendar, LayoutDashboard, Store } from "lucide-react";
import { useAuth } from "../auth/context/AuthContext";
// Adjust this import if your LogoutModal is located elsewhere
import LogoutModal from "../components/LogoutModal"; 

export function SidebarProfileDropup() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [isDropupOpen, setIsDropupOpen] = useState(false);
  const dropupRef = useRef(null);
  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);

  // Close dropup if user clicks outside of it
  useEffect(() => {
    function handleClickOutside(event) {
      if (dropupRef.current && !dropupRef.current.contains(event.target)) {
        setIsDropupOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLogout = async () => {
    await logout();
    setIsLogoutModalOpen(false);
    setIsDropupOpen(false);
    navigate("/login");
  };

  const avatarLetter = user?.username ? user.username.charAt(0).toUpperCase() : "U";

  // If no user is logged in, you probably don't want to render this in the sidebar
  if (!user) return null; 

  return (
    <>
      {/* The parent must be 'relative'. 
        'mt-auto' pushes this entire block to the very bottom of your sidebar. 
      */}
      <div className="relative mt-auto w-full" ref={dropupRef}>
        
        {/* 🚨 THE DROP-UP MENU */}
        {isDropupOpen && (
          // Notice: `bottom-full` and `mb-2` make it pop UP instead of DOWN
          <div className="absolute bottom-full left-0 mb-2 w-full min-w-[14rem] bg-white rounded-xl shadow-lg border border-gray-100 py-1 z-50">
            
            {/* User Info Header */}
            <div className="px-4 py-3 border-b border-gray-100 mb-1 bg-gray-50/50 rounded-t-xl">
              <p className="text-sm font-medium text-gray-900 truncate">
                {user.username}
              </p>
              <p className="text-xs text-gray-500 truncate">
                {user.role}
              </p>
            </div>

            {/* Dynamic Links Based on Role */}
            <Link
              to="/account/settings/profile"
              onClick={() => setIsDropupOpen(false)}
              className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
            >
              <User className="w-4 h-4 text-gray-400" /> Profile
            </Link>

            <Link
              to="/myappointments"
              onClick={() => setIsDropupOpen(false)}
              className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
            >
              <Calendar className="w-4 h-4 text-gray-400" /> My Appointments
            </Link>

            {user.role === "PROVIDER" && (
              <Link
                to="/provider"
                onClick={() => setIsDropupOpen(false)}
                className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
              >
                <Store className="w-4 h-4 text-gray-400" /> Establishment
              </Link>
            )}

            <Link
              to="/account/settings/security"
              onClick={() => setIsDropupOpen(false)}
              className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
            >
              <Settings className="w-4 h-4 text-gray-400" /> Account Settings
            </Link>

            {/* Logout Button */}
            <div className="border-t border-gray-100 mt-1 pt-1">
              <button
                onClick={() => {
                  setIsLogoutModalOpen(true);
                  setIsDropupOpen(false);
                }}
                className="w-full flex items-center gap-3 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 hover:text-red-700 transition-colors text-left"
              >
                <LogOut className="w-4 h-4 text-red-500" /> Sign out
              </button>
            </div>
          </div>
        )}

        {/* 🚨 THE TRIGGER BUTTON */}
        <button
          onClick={() => setIsDropupOpen(!isDropupOpen)}
          className="w-full flex items-center gap-3 p-3 rounded-lg hover:bg-gray-100 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <div className="w-10 h-10 flex-shrink-0 rounded-full bg-blue-600 text-white font-bold text-lg flex items-center justify-center">
            {avatarLetter}
          </div>
          <div className="flex flex-col text-left overflow-hidden">
            <span className="text-sm font-medium text-gray-900 truncate">
              {user.username}
            </span>
            <span className="text-xs text-gray-500 truncate">
              View profile
            </span>
          </div>
        </button>
      </div>

      <LogoutModal
        isOpen={isLogoutModalOpen}
        onClose={() => setIsLogoutModalOpen(false)}
        onConfirm={handleLogout}
      />
    </>
  );
}