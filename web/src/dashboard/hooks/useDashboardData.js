import { useEffect, useMemo } from 'react';
import { useAppointments } from '../../appointment/hooks/useAppointments'; 
import { formatDateForUI, formatTimeForUI } from '../util/formatters'; 
import { useAuth } from '../../auth/context/AuthContext'; 

export function useDashboardData() {
  const { 
    appointments, 
    loading: aptLoading, 
    error: aptError, 
    fetchMyAppointments,
    updateAppointmentStatus // 🚨 1. PULL IT OUT OF THE APPOINTMENTS HOOK
  } = useAppointments();
  
  const { user } = useAuth(); 

  useEffect(() => {
    fetchMyAppointments();
  }, [fetchMyAppointments]);

  const dashboardData = useMemo(() => {
    if (!appointments || appointments.length === 0) {
      return {
        stats: { upcoming: 0, pending: 0, completed: 0, cancelled: 0 },
        allAppointments: []
      };
    }

    const now = new Date();
    let upcoming = 0;
    let pending = 0;
    let completed = 0;
    let cancelled = 0;

    const formattedApts = appointments.map(apt => {
      const aptDateTime = new Date(`${apt.appointmentDate}T${apt.startTime}`);
      const status = apt.status.toUpperCase();

      // Calculate Stats
      if (status === 'PENDING') pending++;
      else if (status === 'COMPLETED') completed++;
      else if (status === 'CANCELLED') cancelled++;
      else if (status === 'CONFIRMED' && aptDateTime >= now) upcoming++;

      return {
        id: apt.id,
        establishmentId: apt.establishmentId, // You added this perfectly!
        establishmentName: apt.establishmentName || "Unknown Establishment",
        date: formatDateForUI(apt.appointmentDate),
        time: formatTimeForUI(apt.startTime),
        status: status,
        rawDate: aptDateTime
      };
    });

    // Sort chronologically (closest appointments first)
    formattedApts.sort((a, b) => b.rawDate - a.rawDate);

    return {
      stats: { upcoming, pending, completed, cancelled },
      allAppointments: formattedApts
    };
  }, [appointments]);

  return {
    user,
    ...dashboardData,
    aptLoading,
    aptError,
    updateAppointmentStatus // 🚨 2. RETURN IT SO THE DASHBOARD CAN USE IT!
  };
}