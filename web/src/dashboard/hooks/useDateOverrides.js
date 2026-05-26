import { useState, useCallback } from 'react';
import { useAxiosPrivate } from '../../api/interceptor';

export function useDateOverrides() {
  const axiosPrivate = useAxiosPrivate();
  const [overrides, setOverrides] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

// --- 1. FETCH OVERRIDES ---
  const fetchOverrides = useCallback(async () => {
    try {
      setLoading(true);
      const response = await axiosPrivate.get('/api/availability/overrides');
      
      // 🚨 Safely sort the data, handling both arrays and strings
      const sortedData = response.data.sort((a, b) => {
        const dateA = Array.isArray(a.date) ? new Date(a.date[0], a.date[1] - 1, a.date[2]) : new Date(a.date);
        const dateB = Array.isArray(b.date) ? new Date(b.date[0], b.date[1] - 1, b.date[2]) : new Date(b.date);
        return dateA - dateB;
      });
      
      setOverrides(sortedData);
    } catch (err) {
      console.error("Fetch overrides error:", err);
      setError("Failed to load date overrides.");
    } finally {
      setLoading(false);
    }
  }, [axiosPrivate]);

  // --- 2. ADD OVERRIDE ---
  const addOverride = async (overrideData) => {
    try {
      setError(null);
      
      // 🚨 Map the React data to perfectly match your Java Request Record
      const payload = {
        date: overrideData.date,
        // Match the exact name expected by request.unavailable()
        unavailable: overrideData.isUnavailable, 
        startTime: overrideData.startTime,
        endTime: overrideData.endTime
      };

      // 🚨 Matched to your @PostMapping
      await axiosPrivate.post('/api/availability/overrides', payload);
      
      await fetchOverrides(); // Refresh the list from the database
      return true; // Tell the form to reset itself
    } catch (err) {
      console.error("Add override error:", err);
      setError("Failed to create override. Please try again.");
      return false;
    }
  };

  // --- 3. DELETE OVERRIDE ---
  const deleteOverride = async (id) => {
    try {
      setError(null);
      // Optimistic UI update: instantly hide it from the screen for a snappy feel
      setOverrides(prev => prev.filter(ov => ov.id !== id));
      
      // 🚨 Matched to your @DeleteMapping("/{id}")
      await axiosPrivate.delete(`/api/availability/overrides/${id}`);
    } catch (err) {
      console.error("Delete override error:", err);
      setError("Failed to delete override.");
      // Revert the optimistic update if the server fails
      fetchOverrides(); 
    }
  };

  return { overrides, loading, error, fetchOverrides, addOverride, deleteOverride };
}