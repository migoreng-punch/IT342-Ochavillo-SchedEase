import { useState, useCallback } from 'react';
import { useAxiosPrivate } from '../../api/interceptor';

export function useAppointments() {
  const axiosPrivate = useAxiosPrivate();
  
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // --- 1. Fetch My Appointments ---
  const fetchMyAppointments = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await axiosPrivate.get('/api/appointments/my');
      setAppointments(response.data);
      
      return response.data;
    } catch (err) {
      console.error("Fetch appointments error:", err);
      setError(err.response?.data?.message || "Failed to load appointments.");
      throw err;
    } finally {
      setLoading(false);
    }
  }, [axiosPrivate]);

  // --- 2. Update Appointment Status (Unified Endpoint) ---
  const updateAppointmentStatus = async (appointmentId, newStatus) => {
    try {
      setError(null);
      
      // 🚨 Hits the new unified endpoint with the JSON body
      await axiosPrivate.put(`/api/appointments/${appointmentId}/status`, { 
        status: newStatus 
      });
      
      // Optimistically update the UI so the card instantly changes
      setAppointments((prev) => 
        prev.map(apt => apt.id === appointmentId ? { ...apt, status: newStatus.toUpperCase() } : apt)
      );
      
    } catch (err) {
      console.error("Status update error:", err);
      throw err; 
    }
  };

  return {
    appointments,
    loading,
    error,
    fetchMyAppointments,
    updateAppointmentStatus // 🚨 Export the new function
  };
}