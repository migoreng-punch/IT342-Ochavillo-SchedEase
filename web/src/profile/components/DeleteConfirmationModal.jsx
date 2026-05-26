import React from 'react';
import { AlertTriangle, Loader2 } from 'lucide-react';

export default function DeleteConfirmationModal({ isOpen, onClose, onConfirm, isDeleting }) {
  // If the modal is not open, render nothing.
  if (!isOpen) return null;

  return (
    // Backdrop blur and dimming
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      {/* Modal Container with animation */}
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-6">
          
          {/* Warning Icon and Title */}
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center">
              <AlertTriangle className="w-5 h-5 text-red-600" />
            </div>
            <h2 className="text-xl font-bold text-gray-900">
              Delete Your Account?
            </h2>
          </div>
          
          {/* Modal Body Text */}
          <p className="text-gray-500 text-sm mb-6">
            Are you absolutely sure you want to delete your account? <span className="font-semibold text-gray-700">This action is permanent and cannot be undone.</span> All of your profile data, appointments, and settings will be lost forever.
          </p>
          
          {/* Action Buttons */}
          <div className="flex items-center justify-end gap-3 pt-2">
            {/* Cancel Button - Closes the modal */}
            <button
              onClick={onClose}
              disabled={isDeleting}
              className="px-4 py-2 rounded-lg text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            
            {/* Confirm Delete Button - Triggers the deletion logic */}
            <button
              onClick={onConfirm}
              disabled={isDeleting}
              className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-white bg-red-600 hover:bg-red-700 transition-colors disabled:opacity-70"
            >
              {isDeleting && <Loader2 className="w-4 h-4 animate-spin" />}
              {isDeleting ? "Deleting..." : "Permanently Delete"}
            </button>
          </div>

        </div>
      </div>
    </div>
  );
}