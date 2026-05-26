import React from 'react';

export default function QuickActionCard({ icon, title, subtitle, onClick }) {
  return (
    <button 
      onClick={onClick} 
      className="flex items-center gap-4 p-5 bg-white border border-gray-200 rounded-xl hover:border-blue-500 hover:shadow-sm transition-all text-left w-full group"
    >
      <div className="p-3 bg-blue-50 text-blue-600 rounded-xl shrink-0 group-hover:bg-blue-600 group-hover:text-white transition-colors">
        {icon}
      </div>
      <div>
        <h4 className="font-semibold text-gray-900 mb-0.5">{title}</h4>
        <p className="text-sm text-gray-500">{subtitle}</p>
      </div>
    </button>
  );
}