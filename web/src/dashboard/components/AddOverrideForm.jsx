import React, { useState } from 'react';
import { Plus, Loader2 } from 'lucide-react';
// 🚨 1. Import your AuthContext
import { useAuth } from '../../auth/context/AuthContext'; // Adjust path if necessary depending on your folder structure

export default function AddOverrideForm({ onAdd }) {
  const [date, setDate] = useState('');
  const [isUnavailable, setIsUnavailable] = useState(true);
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 🚨 2. Pull the user from context and check verification
  const { user } = useAuth();
  const isUnverified = user && !user.isEmailVerified;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!date || isUnverified) return; // Extra safety check

    setIsSubmitting(true);
    
    // Format times for Spring Boot (append :00 if times exist)
    const formattedStart = startTime && !isUnavailable ? `${startTime}:00` : null;
    const formattedEnd = endTime && !isUnavailable ? `${endTime}:00` : null;

    const success = await onAdd({
      date,
      isUnavailable,
      startTime: formattedStart,
      endTime: formattedEnd
    });

    if (success) {
      // Reset form on success
      setDate('');
      setIsUnavailable(true);
      setStartTime('');
      setEndTime('');
    }
    setIsSubmitting(false);
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-lg font-bold text-gray-900 mb-5">Add New Override</h2>
      
      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Date Input */}
        <div>
          <label className={`block text-sm font-medium mb-1 ${isUnverified ? 'text-gray-400' : 'text-gray-700'}`}>
            Date
          </label>
          <input 
            type="date" 
            required
            disabled={isUnverified} // 🚨 Disabled if unverified
            value={date}
            onChange={(e) => setDate(e.target.value)}
            className={`w-full p-2.5 border rounded-lg text-sm outline-none transition-colors ${
              isUnverified 
                ? 'bg-gray-50 border-gray-200 text-gray-400 cursor-not-allowed' 
                : 'border-gray-300 focus:ring-blue-500 focus:border-blue-500 text-gray-900'
            }`}
          />
        </div>

        {/* 🚨 Checkbox (Disabled & Styled for unverified state) */}
        <div className="flex items-center gap-2">
          <input 
            type="checkbox" 
            id="unavailable"
            checked={isUnavailable}
            disabled={isUnverified}
            onChange={(e) => setIsUnavailable(e.target.checked)}
            className={`w-4 h-4 rounded border-gray-300 focus:ring-blue-500 ${
              isUnverified ? 'text-gray-300 bg-gray-100 cursor-not-allowed' : 'text-blue-600 cursor-pointer'
            }`}
          />
          <label 
            htmlFor="unavailable" 
            className={`text-sm font-medium ${isUnverified ? 'text-gray-400 cursor-not-allowed' : 'text-gray-700 cursor-pointer'}`}
          >
            Mark as fully unavailable
          </label>
        </div>

        {/* Conditional Time Inputs */}
        {!isUnavailable && (
          <div className="flex gap-4 p-4 bg-gray-50 rounded-lg border border-gray-100">
            <div className="flex-1">
              <label className="block text-xs font-medium text-gray-500 mb-1">Start Time</label>
              <input 
                type="time" 
                required
                disabled={isUnverified} // 🚨 Disabled
                value={startTime}
                onChange={(e) => setStartTime(e.target.value)}
                className={`w-full p-2 border rounded-md text-sm outline-none ${
                  isUnverified ? 'bg-gray-100 border-gray-200 text-gray-400 cursor-not-allowed' : 'border-gray-300'
                }`}
              />
            </div>
            <div className="flex-1">
              <label className="block text-xs font-medium text-gray-500 mb-1">End Time</label>
              <input 
                type="time" 
                required
                disabled={isUnverified} // 🚨 Disabled
                value={endTime}
                onChange={(e) => setEndTime(e.target.value)}
                className={`w-full p-2 border rounded-md text-sm outline-none ${
                  isUnverified ? 'bg-gray-100 border-gray-200 text-gray-400 cursor-not-allowed' : 'border-gray-300'
                }`}
              />
            </div>
          </div>
        )}

        {/* 🚨 Submit Button Container with Tooltip */}
        <div className="relative group">
          <button 
            type="submit" 
            disabled={isSubmitting || !date || isUnverified}
            className={`w-full flex items-center justify-center gap-2 py-2.5 rounded-lg font-medium transition-colors ${
              isUnverified
                ? 'bg-gray-100 text-gray-400 border border-gray-200 cursor-not-allowed'
                : 'bg-blue-600 hover:bg-blue-700 text-white disabled:opacity-70'
            }`}
          >
            {isSubmitting ? <Loader2 className="w-4 h-4 animate-spin" /> : <Plus className="w-4 h-4" />}
            Add Override
          </button>

          {/* 🚨 The Yellow Tooltip */}
          {isUnverified && (
            <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-64 p-2.5 bg-yellow-50 border border-yellow-200 text-yellow-800 text-xs font-medium text-center rounded-lg shadow-md opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none z-50">
              You need to verify your email address to add schedule overrides.
            </div>
          )}
        </div>
      </form>
    </div>
  );
}