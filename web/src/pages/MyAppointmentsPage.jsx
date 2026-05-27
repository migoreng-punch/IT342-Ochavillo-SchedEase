import React, { useEffect, useMemo, useState } from 'react'; // 🚨 1. Added useState
import { useLocation } from 'react-router-dom'; // 🚨 2. Added useLocation
import { CheckCircle2 } from 'lucide-react'; // 🚨 3. Added CheckCircle2
import AppointmentCard from '../appointment/components/AppointmentCard';
import { useAppointments } from '../appointment/hooks/useAppointments'; 
import { formatDateForUI, formatTimeForUI } from '../dashboard/util/formatters'; 

export default function MyAppointmentsPage() {
  const { 
    appointments, 
    loading, 
    error, 
    fetchMyAppointments, 
    cancelAppointment 
  } = useAppointments();

  // 🚨 4. Logic to capture the message passed from the Booking Page
  const location = useLocation();
  const [successBanner, setSuccessBanner] = useState(location.state?.successMessage || "");

  useEffect(() => {
    // Clear the message after it's read so it doesn't persist on page refresh
    if (location.state?.successMessage) {
        window.history.replaceState({}, document.title);
    }
  }, [location]);

  useEffect(() => {
    fetchMyAppointments();
  }, [fetchMyAppointments]);

  const formattedAppointments = useMemo(() => {
    if (!appointments) return [];
    
    return appointments.map(apt => ({
      ...apt,
      establishmentName: apt.establishmentName || "Unknown Establishment", 
      date: formatDateForUI(apt.appointmentDate),
      time: formatTimeForUI(apt.startTime) 
    }));
  }, [appointments]);

  const handleCancel = async (id) => {
    if (window.confirm("Are you sure you want to cancel?")) {
      try {
        await cancelAppointment(id);
      } catch (err) {
        alert("Could not cancel the appointment.");
      }
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 font-sans">
      <main className="max-w-4xl mx-auto px-4 py-12">
        <h1 className="text-3xl font-extrabold text-gray-900 mb-8">My Appointments</h1>
        
        {/* 🚨 5. Render the banner if it exists */}
        {successBanner && (
          <div className="mb-6 p-4 rounded-xl flex items-start gap-3 text-sm font-medium bg-green-50 text-green-800 border border-green-200 animate-in fade-in slide-in-from-top-2">
            <CheckCircle2 className="w-5 h-5 text-green-600 shrink-0 mt-0.5" />
            <p>{successBanner}</p>
          </div>
        )}
        
        {loading && (
          <div className="flex justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        )}
        
        {error && <p className="text-red-500 bg-red-50 p-4 rounded-xl border border-red-100">{error}</p>}
        
        {!loading && !error && (
          <div className="space-y-4">
            {formattedAppointments.length > 0 ? (
              formattedAppointments.map((apt) => (
                <AppointmentCard 
                  key={apt.id} 
                  appointment={apt} 
                  onCancel={handleCancel}
                  onReschedule={(id) => console.log("Reschedule", id)}
                />
              ))
            ) : (
              <div className="bg-white p-8 text-center rounded-xl border border-gray-200">
                <p className="text-gray-500">No appointments found.</p>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  );
}