import React from "react";
import { NavLink } from "react-router-dom";
import {
  User,
  Shield,
  Bell,
  LogOut,
  Calendar,
  CheckCircle,
  Clock,
  AlertCircle,
  Camera,
  ChevronRight,
  Loader2,
} from "lucide-react";
import { Link } from "react-router-dom";

// 🚨 Add stats and isLoading to props
export default function SettingsSidebar({ user, stats, isLoading }) {
  return (
    <div className="w-full lg:w-72 shrink-0 space-y-6">
      {/* User Profile Card (Unchanged) */}
      <div className="bg-white border border-gray-200 rounded-2xl p-6 text-center shadow-sm">
        <div className="relative inline-block mb-4">
          <div className="w-20 h-20 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center text-2xl font-bold mx-auto border-4 border-white shadow-sm">
            {user.firstName?.[0] || ""}
            {user.lastName?.[0] || ""}
          </div>
        </div>
        <h2 className="text-lg font-bold text-gray-900">
          {user.firstName} {user.lastName}
        </h2>
        <p className="text-sm text-gray-500 mb-3">@{user.username}</p>

        {!user.isVerified && (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md text-xs font-medium bg-yellow-50 text-yellow-700 border border-yellow-200">
            <AlertCircle className="w-3.5 h-3.5" /> Unverified
          </span>
        )}
      </div>

      {/* Activity Stats */}
      <div className="bg-white border border-gray-200 rounded-2xl p-5 shadow-sm">
        <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-4">
          Activity
        </h3>

        {/* 🚨 Show a loader if fetching, otherwise show the real stats */}
        {isLoading ? (
          <div className="flex justify-center py-4">
            <Loader2 className="w-5 h-5 text-gray-400 animate-spin" />
          </div>
        ) : (
          <div className="space-y-3 mb-4">
            <div className="flex justify-between items-center text-sm">
              <span className="flex items-center gap-2 text-gray-600">
                <Calendar className="w-4 h-4 text-gray-400" /> Total bookings
              </span>
              <span className="font-semibold text-gray-900">
                {stats?.total || 0}
              </span>
            </div>
            <div className="flex justify-between items-center text-sm">
              <span className="flex items-center gap-2 text-gray-600">
                <CheckCircle className="w-4 h-4 text-gray-400" /> Completed
              </span>
              <span className="font-semibold text-gray-900">
                {stats?.completed || 0}
              </span>
            </div>
            <div className="flex justify-between items-center text-sm">
              <span className="flex items-center gap-2 text-gray-600">
                <Clock className="w-4 h-4 text-gray-400" /> Upcoming
              </span>
              <span className="font-semibold text-gray-900">
                {stats?.upcoming || 0}
              </span>
            </div>
          </div>
        )}

        <Link
          to="/myappointments"
          className="text-sm font-medium text-blue-600 hover:text-blue-700 flex items-center justify-between border-t border-gray-100 pt-3"
        >
          View all appointments <ChevronRight className="w-4 h-4" />
        </Link>
      </div>

      {/* Navigation Menu (Unchanged) */}
      <nav className="bg-white border border-gray-200 rounded-2xl p-2 shadow-sm flex flex-col gap-1">
        <NavLink
          to="/account/settings/profile"
          className={({ isActive }) =>
            `flex items-center gap-3 px-4 py-2.5 rounded-xl font-medium text-sm transition-colors text-left ${
              isActive
                ? "bg-blue-50 text-blue-700" // Active state
                : "text-gray-600 hover:bg-gray-50 hover:text-gray-900" // Inactive state
            }`
          }
        >
          <User className="w-4 h-4" /> Profile
        </NavLink>

        <NavLink
          to="/account/settings/security"
          className={({ isActive }) =>
            `flex items-center gap-3 px-4 py-2.5 rounded-xl font-medium text-sm transition-colors text-left ${
              isActive
                ? "bg-blue-50 text-blue-700" // Active state
                : "text-gray-600 hover:bg-gray-50 hover:text-gray-900" // Inactive state
            }`
          }
        >
          <Shield className="w-4 h-4" /> Security
        </NavLink>
      </nav>
    </div>
  );
}
