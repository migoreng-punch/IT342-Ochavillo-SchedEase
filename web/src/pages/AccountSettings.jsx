import React, { useEffect, useMemo, useState } from 'react';
import { Loader2, AlertCircle, CheckCircle2 } from 'lucide-react';
import SettingsSidebar from '../profile/components/SettingsSidebar'; 
import SettingsForm from '../profile/components/SettingsForm';       
import { useProfile } from '../profile/hooks/useProfile'; 
import { useAppointments } from '../appointment/hooks/useAppointments'; // 🚨 1. Import appointments hook

export default function AccountSettings() {
  
  const { user, setUser, loading: profileLoading, saving, error, updateProfile } = useProfile();
  
  // 🚨 2. Destructure what we need for the stats
  const { 
    appointments, 
    loading: aptLoading, 
    fetchMyAppointments 
  } = useAppointments();

  const [successMsg, setSuccessMsg] = useState('');

  // 🚨 3. Fetch appointments when the page loads
  useEffect(() => {
    fetchMyAppointments();
  }, [fetchMyAppointments]);

  // 🚨 4. Calculate the real stats dynamically
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

  const handleFieldChange = (field, value) => {
    setUser(prev => ({ ...prev, [field]: value }));
    setSuccessMsg('');
  };

  const handleSave = async () => {
    setSuccessMsg('');
    const success = await updateProfile(user);
    
    if (success) {
      setSuccessMsg("Profile updated successfully!");
      setTimeout(() => setSuccessMsg(''), 3000); 
    }
  };

  if (profileLoading) {
    return (
      <div className="min-h-screen bg-[#F8FAFC] flex justify-center items-center">
        <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#F8FAFC] font-sans pb-16">
      <main className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        
        <div className="mb-8">
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-1">
            Account Settings
          </h1>
          <p className="text-gray-500 text-base">Manage your profile, security, and preferences</p>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 border border-red-100 rounded-xl flex items-center gap-3 text-red-700 font-medium">
            <AlertCircle className="w-5 h-5 shrink-0" /> {error}
          </div>
        )}

        {successMsg && (
          <div className="mb-6 p-4 bg-green-50 border border-green-100 rounded-xl flex items-center gap-3 text-green-700 font-medium">
            <CheckCircle2 className="w-5 h-5 shrink-0" /> {successMsg}
          </div>
        )}

        <div className="flex flex-col lg:flex-row gap-8">
          
          {/* 🚨 5. Pass the calculated stats and loading state to the sidebar! */}
          <SettingsSidebar 
            user={user} 
            stats={activityStats} 
            isLoading={aptLoading} 
          />
          
          <SettingsForm 
            user={user} 
            onUserChange={handleFieldChange} 
            onSave={handleSave}
            isSaving={saving} 
          />

        </div>
      </main>
    </div>
  );
}