import { useState, useEffect, useMemo, useCallback } from 'react';
import { useAxiosPrivate } from '../../api/interceptor'; // Adjust path
import { formatDateForUI, formatTimeForUI } from '../util/formatters'; // Adjust path

export function useProviderDashboardData() {
  const axiosPrivate = useAxiosPrivate();
  
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchProviderAppointments = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 🚨 Adjust this URL if your Controller has a different base path!
      const response = await axiosPrivate.get('/api/appointments/provider');
      setAppointments(response.data);
      
    } catch (err) {
      console.error("Failed to load provider data:", err);
      setError(err.response?.data?.message || "Failed to load dashboard data.");
    } finally {
      setLoading(false);
    }
  }, [axiosPrivate]);

  useEffect(() => {
    fetchProviderAppointments();
  }, [fetchProviderAppointments]);

  // Process the raw backend data into stats and recent activity
  // Process the raw backend data into stats and recent activity
  const dashboardData = useMemo(() => {
    if (!appointments || appointments.length === 0) {
      return {
        stats: { total: 0, pending: 0, confirmed: 0, clients: 0 },
        recentActivity: []
      };
    }

    let pendingCount = 0;
    let confirmedCount = 0;
    const uniqueClients = new Set(); 

    appointments.forEach(apt => {
      const status = apt.status.toUpperCase();
      if (status === 'PENDING') pendingCount++;
      if (status === 'CONFIRMED') confirmedCount++;
      
      if (apt.clientName) {
        uniqueClients.add(apt.clientName); 
      }
    });

    // 🚨 FIX 1: Change apt.date to apt.appointmentDate so the sorting doesn't break!
    const sortedApts = [...appointments].sort((a, b) => {
      const dateA = new Date(`${a.appointmentDate}T${a.startTime}`);
      const dateB = new Date(`${b.appointmentDate}T${b.startTime}`);
      return dateA - dateB; 
    });

    // 🚨 FIX 2: Change apt.date to apt.appointmentDate so the UI can format it!
    const formattedActivity = sortedApts.slice(0, 5).map(apt => ({
      id: apt.id,
      name: apt.clientName || "Unknown Client", 
      date: `${formatDateForUI(apt.appointmentDate)} at ${formatTimeForUI(apt.startTime)}`,
      status: apt.status
    }));

    return {
      stats: {
        total: appointments.length,
        pending: pendingCount,
        confirmed: confirmedCount,
        clients: uniqueClients.size
      },
      recentActivity: formattedActivity
    };
  }, [appointments]);

  return {
    ...dashboardData,
    loading,
    error,
    refetch: fetchProviderAppointments
  };
}