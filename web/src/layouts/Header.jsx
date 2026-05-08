import { Link } from 'react-router-dom';

export function Header() {
  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2">
          <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <span className="text-xl font-bold tracking-tight">SchedEase</span>
        </Link>
        <nav className="hidden md:flex items-center gap-8">
          <Link to="/browse" className="text-blue-600 font-medium hover:text-blue-700">Browse</Link>
          <Link to="/appointments" className="text-gray-500 font-medium hover:text-gray-900">My Appointments</Link>
          <Link to="/login" className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium hover:bg-gray-50 transition-colors">
            Login
          </Link>
        </nav>
      </div>
    </header>
  );
}