import { useState, useCallback } from 'react';
import { useAxiosPrivate } from '../../api/interceptor';
import { formatDateForUI, formatTimeForUI } from '../../dashboard/util/formatters';

export function useProviderAppointments() {
  const axiosPrivate = useAxiosPrivate();
  
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Fetch all appointments for this provider's establishment
  const fetchAppointments = useCallback(async () => {
    try {
      setLoading(true);
      const response = await axiosPrivate.get('/api/appointments/provider');
      
      // Format the data immediately so the UI components are perfectly clean
      const formatted = response.data.map(apt => ({
        ...apt,
        clientName: apt.clientName || "Unknown Client",
        date: formatDateForUI(apt.appointmentDate),
        time: formatTimeForUI(apt.startTime),
        rawDate: new Date(`${apt.appointmentDate}T${apt.startTime}`) // Used for sorting
      }));

      // Sort chronological by default
      formatted.sort((a, b) => a.rawDate - b.rawDate);
      setAppointments(formatted);

    } catch (err) {
      console.error("Fetch error:", err);
      setError("Failed to load appointments.");
    } finally {
      setLoading(false);
    }
  }, [axiosPrivate]);

  // Update status (Confirm, Cancel, Complete)
  const updateStatus = async (id, newStatus) => {
    try {
      // Optimistic UI Update: Change it on the screen instantly for a snappy feel
      setAppointments(prev => 
        prev.map(apt => apt.id === id ? { ...apt, status: newStatus } : apt)
      );

      // Fire the API call in the background
      // Adjust this endpoint to match your actual Spring Boot controller!
      await axiosPrivate.put(`/api/appointments/${id}/status`, { status: newStatus });
      
    } catch (err) {
      console.error("Status update error:", err);
      alert("Failed to update status. Reverting change.");
      fetchAppointments(); // Re-fetch to fix the UI if the server failed
    }
  };

  return {
    appointments,
    loading,
    error,
    fetchAppointments,
    updateStatus
  };
}