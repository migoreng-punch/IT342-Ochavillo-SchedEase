import { Link } from 'react-router-dom';

export function Footer() {
  return (
    <footer className="bg-white border-t border-gray-200 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* ... (Keep your exact footer grid HTML here) ... */}
        <div className="border-t border-gray-200 pt-8 text-center">
          <p className="text-sm text-gray-400">&copy; {new Date().getFullYear()} SchedEase. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}