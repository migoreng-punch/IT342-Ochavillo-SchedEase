import React from "react";
import { AlertTriangle, Loader2 } from "lucide-react";

export default function DeleteEstablishmentModal({ isOpen, onClose, onConfirm, isDeleting }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      {/* Modal Container */}
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-6">
          
          {/* Icon */}
          <div className="w-12 h-12 rounded-full bg-red-100 flex items-center justify-center mb-4">
            <AlertTriangle className="w-6 h-6 text-red-600" />
          </div>
          
          {/* Text Content */}
          <h2 className="text-xl font-bold text-gray-900 mb-2">
            Delete Establishment?
          </h2>
          <p className="text-gray-500 text-sm mb-6">
            Are you absolutely sure? This will permanently delete your establishment, schedule, and all appointments. <span className="font-semibold text-gray-700">This action cannot be undone.</span>
          </p>
          
          {/* Action Buttons */}
          <div className="flex items-center justify-end gap-3">
            <button
              onClick={onClose}
              disabled={isDeleting}
              className="px-4 py-2 rounded-lg text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              onClick={onConfirm}
              disabled={isDeleting}
              className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium text-white bg-red-600 hover:bg-red-700 transition-colors disabled:opacity-70"
            >
              {isDeleting ? <Loader2 className="w-4 h-4 animate-spin" /> : null}
              {isDeleting ? "Deleting..." : "Delete Establishment"}
            </button>
          </div>

        </div>
      </div>
    </div>
  );
}