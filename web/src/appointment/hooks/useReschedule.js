import { useState, useCallback } from 'react';
import { useAxiosPrivate } from '../../api/interceptor';

export function useReschedule() {
  const axiosPrivate = useAxiosPrivate();
  
  const [availableTimes, setAvailableTimes] = useState([]);
  const [isLoadingTimes, setIsLoadingTimes] = useState(false);
  const [rescheduleError, setRescheduleError] = useState(null);

  // 🚨 FIX: Wrapped in useCallback!
  const fetchAvailableTimes = useCallback(async (establishmentId, formattedDate) => {

    if (!establishmentId || establishmentId === 'undefined') {
      console.error("Missing Establishment ID!");
      return; 
    }
    
    try {
      setIsLoadingTimes(true);
      setRescheduleError(null);
      setAvailableTimes([]);

      const response = await axiosPrivate.get(`/api/establishments/${establishmentId}/slots`, {
        params: { date: formattedDate }
      });
      
      setAvailableTimes(response.data);
    } catch (err) {
      console.error("Failed to fetch slots", err);
      setRescheduleError("Failed to load available times.");
    } finally {
      setIsLoadingTimes(false);
    }
  }, [axiosPrivate]); // <-- Only recreate if axiosPrivate changes

  // 🚨 FIX: Wrapped in useCallback just to be safe!
  const submitReschedule = useCallback(async (appointmentId, payload) => {
    try {
      setRescheduleError(null);
      
      await axiosPrivate.put(`/api/appointments/${appointmentId}/reschedule`, {
        date: payload.date,
        startTime: payload.startTime
      });

      return true; // Success!
    } catch (err) {
      const backendMessage = err.response?.data?.message || "Failed to reschedule.";
      setRescheduleError(backendMessage);
      return false; // Failed!
    }
  }, [axiosPrivate]);

  return {
    availableTimes,
    isLoadingTimes,
    rescheduleError,
    setRescheduleError,
    fetchAvailableTimes,
    submitReschedule
  };
}