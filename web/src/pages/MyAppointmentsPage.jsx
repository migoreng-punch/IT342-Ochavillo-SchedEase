import React, { useEffect, useMemo } from 'react';
import AppointmentCard from '../appointment/components/AppointmentCard';
import { useAppointments } from '../appointment/hooks/useAppointments'; 
// 🚨 1. Import your formatters! (Adjust the path to match your project structure)
import { formatDateForUI, formatTimeForUI } from '../dashboard/util/formatters'; 

export default function MyAppointmentsPage() {
  const { 
    appointments, 
    loading, 
    error, 
    fetchMyAppointments, 
    cancelAppointment 
  } = useAppointments();

  useEffect(() => {
    fetchMyAppointments().then(rawAppointments => {
      // 🚨 ADD THIS LINE!
      console.log("RAW BACKEND DATA:", rawAppointments[0]); 
    });
  }, [fetchMyAppointments]);

  // 🚨 2. Format the raw backend data to match what the AppointmentCard expects
  const formattedAppointments = useMemo(() => {
    if (!appointments) return [];
    
    return appointments.map(apt => ({
      ...apt, // Keep the ID, Status, and other original fields
      // Ensure the name exists, provide a fallback just in case
      establishmentName: apt.establishmentName || "Unknown Establishment", 
      // Format the date into a readable string
      date: formatDateForUI(apt.appointmentDate),
      // Map the backend's 'startTime' to the frontend's 'time' property
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
        
        {loading && (
          <div className="flex justify-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          </div>
        )}
        
        {error && <p className="text-red-500 bg-red-50 p-4 rounded-xl border border-red-100">{error}</p>}
        
        {!loading && !error && (
          <div className="space-y-4">
            {formattedAppointments.length > 0 ? (
              // 🚨 3. Map over the FORMATTED appointments, not the raw ones
              formattedAppointments.map((apt) => (
                <AppointmentCard 
                  key={apt.id} 
                  appointment={apt} 
                  onCancel={handleCancel}
                  onReschedule={(id) => console.log("Reschedule", id)} // Added placeholder so the button works!
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