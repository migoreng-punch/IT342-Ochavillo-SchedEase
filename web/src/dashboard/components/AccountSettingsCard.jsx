import React from 'react';

export default function AccountSettingsCard({ data, onChange }) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-base font-bold text-gray-900 mb-5">Account Settings</h2>
      
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Username</label>
          <input 
            type="text" 
            value={data.username}
            onChange={(e) => onChange('username', e.target.value)}
            className="w-full p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">First Name</label>
          <input 
            type="text" 
            value={data.firstName}
            onChange={(e) => onChange('firstName', e.target.value)}
            className="w-full p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Last Name</label>
          <input 
            type="text" 
            value={data.lastName}
            onChange={(e) => onChange('lastName', e.target.value)}
            className="w-full p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none"
          />
        </div>

        <div className="pt-2">
          <button 
            type="button"
            className="text-sm font-medium text-blue-600 hover:text-blue-700 transition-colors"
          >
            Change Password
          </button>
        </div>
      </div>
    </div>
  );
}