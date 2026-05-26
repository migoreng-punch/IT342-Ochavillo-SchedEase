import React from "react";
import { Link, useLocation } from "react-router-dom";
import {
  LayoutGrid,
  CalendarDays,
  Clock,
  CalendarOff,
  Settings,
} from "lucide-react";
import { useAuth } from "../auth/context/AuthContext"; // Adjust path
import { SidebarProfileDropup } from "./SidebarDropup";

export default function ProviderSidebar() {
  const { user } = useAuth();
  const location = useLocation();

  const navItems = [
    { name: "Overview", path: "/provider", icon: LayoutGrid },
    {
      name: "Appointments",
      path: "/provider/appointments",
      icon: CalendarDays,
    },
    { name: "Weekly Schedule", path: "/provider/schedule", icon: Clock },
    { name: "Date Overrides", path: "/provider/overrides", icon: CalendarOff },
    { name: "Settings", path: "/provider/settings", icon: Settings },
  ];

  return (
    <aside className="w-64 bg-white border-r border-gray-200 flex flex-col h-screen sticky top-0">
      {/* Brand Header */}
      <Link
        to="/homepage" // Adjust this to your actual homepage route!
        className="h-16 flex items-center px-6 border-b border-gray-100 mb-4 hover:bg-gray-50 transition-colors cursor-pointer"
      >
        <CalendarDays className="w-6 h-6 text-blue-600 mr-2" />
        <span className="text-lg font-bold text-gray-900">SchedEase</span>
      </Link>

      {/* Navigation Links */}
      <nav className="flex-1 px-4 space-y-1">
        {navItems.map((item) => {
          const isActive = location.pathname === item.path;
          return (
            <Link
              key={item.name}
              to={item.path}
              className={`flex items-center px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                  ? "bg-blue-50 text-blue-700"
                  : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
              }`}
            >
              <item.icon
                className={`w-5 h-5 mr-3 ${isActive ? "text-blue-700" : "text-gray-400"}`}
              />
              {item.name}
            </Link>
          );
        })}
      </nav>

      {/* User Profile */}
      <div className="p-2 border-t border-gray-200">
        <SidebarProfileDropup/>
      </div>
    </aside>
  );
}
