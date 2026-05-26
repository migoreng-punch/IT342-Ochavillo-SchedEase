import React, { useState, useEffect } from "react";
import { Save, Loader2 } from "lucide-react";
import { useSettings } from "../dashboard/hooks/useSettings";
import EstablishmentInfoCard from "../dashboard/components/EstablishmentInfoCard";
// Note: Assuming you removed AccountSettingsCard previously based on your provided code
import DangerZoneCard from "../dashboard/components/DangerZoneCard";
import DeleteEstablishmentModal from "../dashboard/components/DeleteEstablishmentModal"; // 🚨 1. Import the modal

import { useAuth } from "../auth/context/AuthContext";

export default function ProviderSettingsPage() {
  const {
    formData,
    loading,
    saving,
    error,
    fetchSettings,
    handleChange,
    saveSettings,
    deleteEstablishment,
  } = useSettings();

  const { user } = useAuth();
  const isUnverified = user && !user.isEmailVerified;

  // 🚨 2. Add state for the modal
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    fetchSettings();
  }, [fetchSettings]);

  // 🚨 3. Wrapper function for confirming deletion
  const handleConfirmDelete = async () => {
    await deleteEstablishment();
    setIsModalOpen(false); // Close the modal when done
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );
  }

  return (
    <>
      <div className="max-w-3xl mx-auto pb-12">
        {/* Header Layout */}
        <div className="flex items-center justify-between mb-8 pb-4 border-b border-gray-200">
          <h1 className="text-2xl font-bold text-gray-900">Settings</h1>
          
          <div className="relative group">
            <button
              onClick={saveSettings}
              disabled={saving || isUnverified}
              className={`flex items-center gap-2 px-4 py-2 rounded-lg font-medium transition-colors shadow-sm ${
                isUnverified
                  ? "bg-gray-100 text-gray-400 border border-gray-200 cursor-not-allowed" 
                  : "bg-blue-600 hover:bg-blue-700 text-white disabled:opacity-70" 
              }`}
            >
              {saving ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <Save className="w-4 h-4" />
              )}
              Save Changes
            </button>

            {isUnverified && (
              <div className="absolute top-full right-0 mt-2 w-64 p-2.5 bg-yellow-50 border border-yellow-200 text-yellow-800 text-xs font-medium text-center rounded-lg shadow-md opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none z-50">
                You need to verify your email address to save settings.
              </div>
            )}
          </div>
        </div>

        {error && (
          <div className="bg-red-50 text-red-600 p-4 rounded-xl border border-red-100 mb-6">
            {error}
          </div>
        )}

        {/* Cards Layout */}
        <div className="space-y-6">
          <EstablishmentInfoCard data={formData} onChange={handleChange} />
          
          {/* 🚨 4. Change this to open the modal instead of calling deleteEstablishment directly */}
          <DangerZoneCard onDelete={() => setIsModalOpen(true)} />
        </div>
      </div>

      {/* 🚨 5. Render the modal outside the main layout container */}
      <DeleteEstablishmentModal 
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onConfirm={handleConfirmDelete}
        isDeleting={saving} // Passes the 'saving' state to show the spinner in the modal
      />
    </>
  );
}