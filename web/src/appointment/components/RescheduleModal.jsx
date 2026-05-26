import React, { useState, useEffect } from 'react';
import { Calendar, ChevronLeft, ChevronRight, Loader2, AlertCircle } from 'lucide-react';
import { useCalendar } from '../hooks/useCalendar'; // From previous step
import DatePicker from '../components/DatePicker'; // From previous step

export default function RescheduleModal({ 
  isOpen, 
  onClose, 
  onConfirm,
  onDateSelect, // Callback to fetch available times
  availableTimes = [], // Array of time strings e.g., ["09:00:00", "09:30:00"]
  isLoadingTimes = false,
  error = null,
  currentAppointment 
}) {
  const { selectedDate, setSelectedDate, calendarData, nextMonth, prevMonth } = useCalendar();
  const [selectedTime, setSelectedTime] = useState(null);

  // When a date is clicked, clear the time and tell the parent to fetch new times
  useEffect(() => {
    if (selectedDate) {
      setSelectedTime(null);
      
      // Format date to YYYY-MM-DD for your Spring Boot backend
      const formattedDate = selectedDate.toLocaleDateString('en-CA'); 
      onDateSelect(formattedDate);
    }
  }, [selectedDate]);

  if (!isOpen) return null;

  const handleConfirm = () => {
    if (selectedDate && selectedTime) {
      const formattedDate = selectedDate.toLocaleDateString('en-CA');
      onConfirm({ date: formattedDate, startTime: selectedTime });
    }
  };

  // Helper to format "09:00:00" to "9:00 AM" for the UI
  const formatTime = (timeString) => {
    const [hour, minute] = timeString.split(':');
    const d = new Date();
    d.setHours(parseInt(hour, 10), parseInt(minute, 10), 0);
    return d.toLocaleTimeString('en-US', { hour: 'numeric', minute: '2-digit' });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden flex flex-col max-h-[90vh]">
        
        <div className="p-6 overflow-y-auto">
          {/* Appointment Banner */}
          <div className="flex items-center gap-2 bg-gray-50 border border-gray-100 rounded-lg p-4 mb-6">
            <Calendar className="w-5 h-5 text-gray-500" />
            <p className="text-sm text-gray-600">
              Currently booked: <span className="font-semibold text-gray-900">{currentAppointment.date} at {currentAppointment.time}</span>
            </p>
          </div>

          {/* Backend Error Display (Catches Cooldowns, Rate Limits, etc.) */}
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-100 rounded-lg flex gap-2 text-red-600 text-sm">
              <AlertCircle className="w-5 h-5 shrink-0" />
              <p>{error}</p>
            </div>
          )}

          <h2 className="text-base font-bold text-gray-900 mb-3">1. Select a new date</h2>
          <DatePicker 
            calendarData={calendarData}
            onNextMonth={nextMonth}
            onPrevMonth={prevMonth}
            onSelectDate={setSelectedDate}
          />

          {/* 🚨 NEW: Time Selection Section */}
          {selectedDate && (
            <div className="mt-6">
              <h2 className="text-base font-bold text-gray-900 mb-3">2. Select a time</h2>
              
              {isLoadingTimes ? (
                <div className="flex justify-center items-center py-8">
                  <Loader2 className="w-6 h-6 text-blue-500 animate-spin" />
                </div>
              ) : availableTimes.length > 0 ? (
                <div className="grid grid-cols-3 gap-2">
                  {availableTimes.map((time) => (
                    <button
                      key={time}
                      onClick={() => setSelectedTime(time)}
                      className={`
                        py-2 px-3 rounded-lg text-sm font-medium transition-all border
                        ${selectedTime === time 
                          ? 'bg-blue-500 text-white border-blue-500 shadow-sm' 
                          : 'bg-white text-gray-700 border-gray-200 hover:border-blue-500 hover:bg-blue-50'
                        }
                      `}
                    >
                      {formatTime(time)}
                    </button>
                  ))}
                </div>
              ) : (
                <p className="text-sm text-gray-500 text-center py-4 bg-gray-50 rounded-lg border border-gray-100">
                  No available times for this date.
                </p>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-5 border-t border-gray-100 bg-white flex items-center justify-end gap-3 shrink-0">
          <button 
            onClick={onClose}
            className="px-5 py-2.5 rounded-lg text-sm font-semibold text-gray-700 border border-gray-300 hover:bg-gray-50 transition-colors"
          >
            Cancel
          </button>
          <button 
            disabled={!selectedDate || !selectedTime}
            onClick={handleConfirm}
            className="px-5 py-2.5 rounded-lg text-sm font-semibold text-white bg-blue-500 disabled:opacity-50 disabled:cursor-not-allowed hover:bg-blue-600 transition-colors"
          >
            Confirm Reschedule
          </button>
        </div>

      </div>
    </div>
  );
}