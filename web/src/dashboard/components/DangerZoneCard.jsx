import React from 'react';

export default function DangerZoneCard({ onDelete }) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-red-300 p-6">
      <h2 className="text-base font-bold text-red-600 mb-2">Danger Zone</h2>
      <p className="text-sm text-gray-500 mb-5">
        Once you delete your Establishment, there is no going back. Please be certain.
      </p>
      
      <button 
        onClick={onDelete}
        className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors"
      >
        Delete Establishment
      </button>
    </div>
  );
}