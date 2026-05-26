import { useState, useCallback } from 'react';
import { useAxiosPrivate } from '../../api/interceptor';

const weekTemplate = [
  { dayOfWeek: 'MONDAY' },
  { dayOfWeek: 'TUESDAY' },
  { dayOfWeek: 'WEDNESDAY' },
  { dayOfWeek: 'THURSDAY' },
  { dayOfWeek: 'FRIDAY' },
  { dayOfWeek: 'SATURDAY' },
  { dayOfWeek: 'SUNDAY' },
];

export function useWeeklySchedule() {
  const axiosPrivate = useAxiosPrivate();
  
  const [schedule, setSchedule] = useState(
    weekTemplate.map(day => ({ ...day, isWorkingDay: false, startTime: '', endTime: '' }))
  );
  
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  // --- 1. FETCH DATA ---
  const fetchSchedule = useCallback(async () => {
    try {
      setLoading(true);
      const response = await axiosPrivate.get('/api/providers/availability'); 
      const dbData = response.data;

      if (dbData && dbData.length > 0) {
        const mergedSchedule = weekTemplate.map(templateDay => {
          const savedDay = dbData.find(d => d.dayOfWeek === templateDay.dayOfWeek);
          
          if (savedDay) {
            return {
              dayOfWeek: savedDay.dayOfWeek,
              isWorkingDay: !!(savedDay.startTime && savedDay.endTime), 
              // Slice off the seconds so HTML can read it ("09:00")
              startTime: savedDay.startTime ? savedDay.startTime.slice(0, 5) : '',
              endTime: savedDay.endTime ? savedDay.endTime.slice(0, 5) : ''
            };
          }
          
          return { ...templateDay, isWorkingDay: false, startTime: '', endTime: '' };
        });
        
        setSchedule(mergedSchedule);
      } else {
        // 🚨 CULPRIT 1 FIXED: If DB is empty, reset the UI to all OFF!
        setSchedule(
          weekTemplate.map(day => ({ ...day, isWorkingDay: false, startTime: '', endTime: '' }))
        );
      }
    } catch (err) {
      console.error("Failed to load schedule:", err);
      setError("Could not load your schedule.");
    } finally {
      setLoading(false);
    }
  }, [axiosPrivate]);

  // --- 2. HANDLE UI TOGGLES ---
  const updateDay = (dayOfWeek, field, value) => {
    setSchedule(prev => prev.map(day => {
      if (day.dayOfWeek === dayOfWeek) {
        const updatedDay = { ...day, [field]: value };
        
        if (field === 'isWorkingDay' && value === false) {
           updatedDay.startTime = '';
           updatedDay.endTime = '';
        }
        return updatedDay;
      }
      return day;
    }));
  };

  // --- 3. SAVE DATA ---
  const saveSchedule = async () => {
    try {
      setSaving(true);
      setError(null);

      // 🚨 CULPRIT 2 FIXED: Filter and Format the payload for Spring Boot
      const payload = schedule
        // 1. Only grab days where the toggle is ON and times are filled out
        .filter(day => day.isWorkingDay && day.startTime && day.endTime)
        // 2. Format the payload and append ":00" for Java's LocalTime parser
        .map(day => ({
          dayOfWeek: day.dayOfWeek,
          startTime: day.startTime.length === 5 ? `${day.startTime}:00` : day.startTime,
          endTime: day.endTime.length === 5 ? `${day.endTime}:00` : day.endTime
        }));

      // Send the perfectly formatted, clean array to Spring Boot
      await axiosPrivate.put('/api/providers/availability', payload);
      
      alert("Schedule saved successfully!");
      
      // Re-fetch to ensure the UI perfectly matches the database after saving
      fetchSchedule();
      
    } catch (err) {
      console.error("Failed to save schedule:", err);
      setError("Failed to save changes. Please try again.");
    } finally {
      setSaving(false);
    }
  };

  return {
    schedule,
    loading,
    saving,
    error,
    fetchSchedule,
    updateDay,
    saveSchedule
  };
}