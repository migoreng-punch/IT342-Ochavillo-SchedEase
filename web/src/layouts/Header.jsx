import React, { useState, useRef, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  LogOut,
  User,
  Settings,
  Calendar,
  LayoutDashboard,
  Cog,
  Plus,
  Store,
  ClipboardClock,
} from "lucide-react";
import { useAuth } from "../auth/context/AuthContext";
import LogoutModal from "../components/LogoutModal";
import { useAxiosPrivate } from "../api/interceptor";

export function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const axiosPrivate = useAxiosPrivate();

  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);
  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);
  const [hasEstablishment, setHasEstablishment] = useState(null);

  // Close dropdown if user clicks outside of it
  useEffect(() => {
    function handleClickOutside(event) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // 🚨 UPDATED EFFECT: Check for establishment existence for ALL providers
  useEffect(() => {
    let isMounted = true;

    const checkEstablishmentStatus = async () => {
      // 🚨 Removed the isEmailVerified check here so it runs for unverified users too
      if (user?.role === "PROVIDER") {
        try {
          await axiosPrivate.get("/api/establishments/me");
          if (isMounted) setHasEstablishment(true);
        } catch (error) {
          if (isMounted) setHasEstablishment(false);
        }
      }
    };

    checkEstablishmentStatus();

    return () => {
      isMounted = false;
    };
  }, [user, axiosPrivate]);

  const handleLogout = async () => {
    await logout();
    setIsLogoutModalOpen(false);
    setIsDropdownOpen(false);
    navigate("/login");
  };

  const avatarLetter = user?.username
    ? user.username.charAt(0).toUpperCase()
    : "U";

  let logoLink = "/";
  if (user?.username) {
    logoLink = "/homepage";
  }

  return (
    <>
      <header className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          {/* Logo */}
          <Link
            to={logoLink}
            className="flex items-center gap-2 hover:opacity-80 transition-opacity"
          >
            <svg
              className="w-6 h-6 text-blue-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            <span className="text-xl font-bold tracking-tight">SchedEase</span>
          </Link>

          {/* Navigation / Auth */}
          <nav className="flex items-center gap-6">
            {user?.username ? (
              <>
                {/* 🚨 UPDATED: Create Establishment Soft-Block Button */}
                {user.role === "PROVIDER" && hasEstablishment === false && (
                  <div className="relative group hidden sm:block">
                    {user.isEmailVerified ? (
                      // ACTIVE STATE: Verified
                      <Link
                        to="/setup-establishment"
                        className="flex items-center gap-2 px-4 py-2 text-sm font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-all active:scale-95 shadow-sm"
                      >
                        <Plus className="w-4 h-4" />
                        Create Establishment
                      </Link>
                    ) : (
                      // DISABLED STATE: Unverified
                      <>
                        <button
                          disabled
                          className="flex items-center gap-2 px-4 py-2 text-sm font-semibold text-gray-400 bg-gray-100 border border-gray-200 rounded-lg cursor-not-allowed"
                        >
                          <Plus className="w-4 h-4" />
                          Create Establishment
                        </button>

                        {/* The Yellow Tooltip */}
                        <div className="absolute top-full right-0 mt-2 w-64 p-2.5 bg-yellow-50 border border-yellow-200 text-yellow-800 text-xs font-medium text-center rounded-lg shadow-md opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none z-50">
                          You need to verify your email address to access this
                          function.
                        </div>
                      </>
                    )}
                  </div>
                )}

                {/* LOGGED IN STATE: Show Avatar & Dropdown */}
                <div className="relative" ref={dropdownRef}>
                  <button
                    onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                    className="w-10 h-10 rounded-full bg-blue-600 text-white font-bold text-lg flex items-center justify-center hover:bg-blue-700 transition-colors focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                  >
                    {avatarLetter}
                  </button>

                  {/* Dropdown Menu */}
                  {isDropdownOpen && (
                    <div className="absolute right-0 mt-2 w-56 bg-white rounded-xl shadow-lg border border-gray-100 py-1 z-50">
                      {/* User Info Header */}
                      <div className="px-4 py-3 border-b border-gray-100 mb-1 bg-gray-50/50 rounded-t-xl">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {user.username}
                        </p>
                        <p className="text-xs text-gray-500 truncate">
                          {user.role}
                        </p>
                      </div>

                      {/* PROVIDER LINKS */}
                      {user?.role === "PROVIDER" && (
                        <>
                          <Link
                            to="/account/settings/profile"
                            onClick={() => setIsDropdownOpen(false)}
                            className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                          >
                            <User className="w-4 h-4 text-gray-400" /> {" "}
                            Profile
                          </Link>
                          <Link
                            to="/myappointments"
                            onClick={() => setIsDropdownOpen(false)}
                            className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                          >
                            <Calendar className="w-4 h-4 text-gray-400" /> {" "}
                            My Appointments
                          </Link>
                          <Link
                            to="/dashboard"
                            onClick={() => setIsDropdownOpen(false)}
                            className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                          >
                            <LayoutDashboard className="w-4 h-4 text-gray-400" />{" "}
                            Dashboard
                          </Link>
                          <Link
                            to="/provider"
                            onClick={() => setIsDropdownOpen(false)}
                            className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                          >
                            <Store className="w-4 h-4 text-gray-400" />{" "}
                            Establishment Dashboard
                          </Link>
                          <Link
                            to="/account/settings/Security"
                            onClick={() => setIsDropdownOpen(false)}
                            className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                          >
                            <Settings className="w-4 h-4 text-gray-400" />{" "}
                            Account Settings
                          </Link>
                        </>
                      )}

                      {/* CLIENT LINKS */}
                      {user?.role === "CLIENT" && (
                        <>
                          <Link
                            to="/account/settings/profile"
                            onClick={() => setIsDropdownOpen(false)}
                            className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                          >
                            <User className="w-4 h-4 text-gray-400" /> Profile
                          </Link>
                          <Link
                            to="/myappointments"
                            onClick={() => setIsDropdownOpen(false)}
                            className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                          >
                            <Calendar className="w-4 h-4 text-gray-400" />  {" "}
                            My Appointments
                          </Link>
                          <Link
                            to="/dashboard"
                            onClick={() => setIsDropdownOpen(false)}
                            className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                          >
                            <User className="w-4 h-4 text-gray-400" /> Dashboard
                          </Link>
                          <Link
                            to="/account/settings/security"
                            onClick={() => setIsDropdownOpen(false)}
                            className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                          >
                            <Settings className="w-4 h-4 text-gray-400" />{" "}
                            Account Settings
                          </Link>
                        </>
                      )}

                      {/* Logout Button */}
                      <div className="border-t border-gray-100 mt-1 pt-1">
                        <button
                          onClick={() => {
                            setIsLogoutModalOpen(true);
                            setIsDropdownOpen(false);
                          }}
                          className="w-full flex items-center gap-3 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 hover:text-red-700 transition-colors text-left"
                        >
                          <LogOut className="w-4 h-4 text-red-500" />
                          Sign out
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </>
            ) : (
              // LOGGED OUT STATE: Show Login Button
              <Link
                to="/login"
                className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium hover:bg-gray-50 transition-colors"
              >
                Login
              </Link>
            )}
          </nav>
        </div>
      </header>

      <LogoutModal
        isOpen={isLogoutModalOpen}
        onClose={() => setIsLogoutModalOpen(false)}
        onConfirm={handleLogout}
      />
    </>
  );
}
