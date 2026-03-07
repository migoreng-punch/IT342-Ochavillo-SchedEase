import { NavLink } from "react-router-dom";

export default function Sidebar() {
  return (
    <aside className="w-64 bg-white border-r border-gray-200 p-6">
      <h1 className="text-2xl font-bold text-primary mb-10">
        SchedEase
      </h1>

      <nav className="space-y-3">
        <NavLink
          to="/dashboard"
          className="block px-4 py-2 rounded-lg hover:bg-gray-100"
        >
          Dashboard
        </NavLink>

        <NavLink
          to="/appointments"
          className="block px-4 py-2 rounded-lg hover:bg-gray-100"
        >
          Appointments
        </NavLink>

        <NavLink
          to="/profile"
          className="block px-4 py-2 rounded-lg hover:bg-gray-100"
        >
          Profile
        </NavLink>
      </nav>
    </aside>
  );
}