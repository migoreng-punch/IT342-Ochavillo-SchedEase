import React, { useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { Loader2 } from "lucide-react"; // 🚨 Added Loader2 to match the profile page loading state
import { useAuth } from "../auth/context/AuthContext"; // Adjust path as needed
import { useProfile } from "../profile/hooks/useProfile"; // Adjust path as needed
import { useAppointments } from "../appointment/hooks/useAppointments"; // Adjust path as needed

import ChangePasswordCard from "../profile/components/ChangePasswordCard";
import DangerZoneCard from "../profile/components/DangerZoneCard";
import SettingsSidebar from "../profile/components/SettingsSidebar"; 
import DeleteConfirmationModal from "../profile/components/DeleteConfirmationModal"; // 🚨 1. Import your new Modal component

export default function SecuritySettingsPage() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  
  // Profile Hook
  const { user, loading, changePassword, changingPassword, deleteAccount } = useProfile();
  
  // Appointments Hook
  const { appointments, loading: aptLoading, fetchMyAppointments } = useAppointments();

  // state to manage the loading state during deletion
  const [isDeleting, setIsDeleting] = useState(false);

  // 🚨 2. Add state to control modal visibility (starts as closed)
  const [isModalOpen, setIsModalOpen] = useState(false);

  // 1. Fetch appointments on mount
  useEffect(() => {
    fetchMyAppointments();
  }, [fetchMyAppointments]);

  // 2. Calculate stats dynamically
  const activityStats = useMemo(() => {
    if (!appointments) return { total: 0, completed: 0, upcoming: 0 };
    
    let completed = 0;
    let upcoming = 0;
    const now = new Date();

    appointments.forEach(apt => {
      const status = apt.status?.toUpperCase();
      if (status === 'COMPLETED') {
        completed++;
      } else if (status === 'CONFIRMED' || status === 'PENDING') {
        const aptDateTime = new Date(`${apt.appointmentDate}T${apt.startTime}`);
        if (aptDateTime >= now) {
          upcoming++;
        }
      }
    });

    return {
      total: appointments.length,
      completed,
      upcoming
    };
  }, [appointments]);

  // 🚨 3. Modified logic for opening the modal
  const handleOpenModal = () => {
    setIsModalOpen(true);
  }

  // 🚨 4. The actual account deletion function called from the modal
  const handleConfirmDelete = async () => {
    // We don't need window.confirm here anymore.
    
    setIsDeleting(true);
    
    // The deletion logic continues as before...
    const response = await deleteAccount();
    
    if (response.success) {
      // Clear local state, close modal, and redirect to login
      setIsModalOpen(false);
      await logout();
      navigate("/login");
    } else {
      alert(response.message);
      setIsDeleting(false);
      // Optional: keep the modal open or close it on failure
    }
  };

  // 🚨 MATCHED: Loading state styling
  if (loading) {
    return (
      <div className="min-h-screen bg-[#F8FAFC] flex justify-center items-center">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );
  }

  // 🚨 MATCHED: Wrapper styling, container width, and typography
  return (
    <>
      {/* 🚨 Render the Modal component and pass down its state and functions */}
      <DeleteConfirmationModal 
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)} // Function to close
        onConfirm={handleConfirmDelete}      // Function to trigger deletion
        isDeleting={isDeleting}             // Passes loading state to modal button
      />

      <div className="min-h-screen bg-[#F8FAFC] font-sans pb-16">
        <main className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
          
          <div className="mb-8">
            <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-1">
              Security Settings
            </h1>
            <p className="text-gray-500 text-base">
              Manage your password and account security
            </p>
          </div>

          <div className="flex flex-col lg:flex-row gap-8">
            
            {/* Sidebar */}
            <div className="w-full lg:w-1/3">
              <SettingsSidebar 
                user={user} 
                stats={activityStats} 
                isLoading={aptLoading} 
              />
            </div>

            {/* Cards */}
            <div className="w-full lg:w-2/3 space-y-6">
              <ChangePasswordCard 
                onChangePassword={changePassword} 
                isChanging={changingPassword} 
              />
              <DangerZoneCard 
                // 🚨 Passes the function that now opens the modal
                onDeleteAccount={handleOpenModal} 
                isDeleting={isDeleting} 
              />
            </div>
            
          </div>
        </main>
      </div>
    </>
  );
}