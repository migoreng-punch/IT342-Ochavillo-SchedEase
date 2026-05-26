import React from 'react';
import { Trash2, Loader2 } from 'lucide-react';

// 🚨 Add isDeleting to props
export default function DangerZoneCard({ onDeleteAccount, isDeleting }) {
  return (
    <div className="bg-red-50 rounded-xl border border-red-100 overflow-hidden">
      <div className="p-6">
        <h2 className="text-sm font-bold text-red-700">Danger Zone</h2>
        <p className="text-sm text-red-600 mt-1">These actions are permanent and cannot be undone</p>
      </div>

      <div className="bg-white p-6 border-t border-red-100 flex items-center justify-between">
        <div>
          <h3 className="text-sm font-semibold text-gray-900">Delete account</h3>
          <p className="text-xs text-gray-500 mt-1">Permanently delete your account and all associated data</p>
        </div>
        
        {/* 🚨 Update the button to handle the loading state */}
        <button 
          onClick={onDeleteAccount}
          disabled={isDeleting}
          className="flex items-center gap-2 px-4 py-2 border border-red-200 text-red-600 rounded-lg text-sm font-medium hover:bg-red-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isDeleting ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <Trash2 className="w-4 h-4" />
          )}
          {isDeleting ? 'Deleting...' : 'Delete'}
        </button>
      </div>
    </div>
  );
}