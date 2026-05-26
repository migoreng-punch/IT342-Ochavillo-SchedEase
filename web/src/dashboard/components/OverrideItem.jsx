import React from 'react';
import { Trash2 } from 'lucide-react';

export default function OverrideItem({ override, onDelete }) {
  // 🚨 DIAGNOSTIC RADAR: Open your browser console (F12) to see this!
  console.log("🔍 RAW OVERRIDE DATA from Backend:", override);

  // 1. Ultra-Safe Date Parser
  let parsedDate = new Date(); // Default fallback to prevent crashes
  
  try {
    // We will check 'date', but also guess common Spring Boot entity names
    const rawDate = override.date || override.overrideDate || override.targetDate;

    if (!rawDate) {
      console.warn("⚠️ No recognizable date field found in this object:", override);
    } else if (Array.isArray(rawDate)) {
      // Safely handle arrays [2026, 3, 20] using standard UTC conversion
      // Note: JavaScript months are 0-indexed, so we subtract 1 from the month!
      parsedDate = new Date(Date.UTC(rawDate[0], rawDate[1] - 1, rawDate[2]));
    } else {
      parsedDate = new Date(rawDate);
    }

    // If it's STILL an invalid date for some reason, catch it
    if (isNaN(parsedDate.getTime())) {
      parsedDate = new Date(); 
    }
  } catch (e) {
    console.error("Date parsing failed completely:", e);
  }

  // 2. Safe Formatting
  const formattedDate = new Intl.DateTimeFormat('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    timeZone: 'UTC' 
  }).format(parsedDate);

  // 3. Ultra-Safe Time Parser
  const safeFormatTime = (timeData) => {
    if (!timeData) return "??:??"; // Fallback if missing
    if (Array.isArray(timeData)) {
      // Handles [14, 30] -> "14:30"
      return timeData.map(num => String(num).padStart(2, '0')).join(':').slice(0, 5);
    }
    // Handles "14:30:00" -> "14:30"
    return String(timeData).slice(0, 5);
  };

  return (
    <div className="flex items-center justify-between p-5 border-b border-gray-100 last:border-0 hover:bg-gray-50 transition-colors">
      
      <div>
        <h3 className="font-semibold text-gray-900 text-sm mb-1">{formattedDate}</h3>
        
        {override.unavailable ? (
          <p className="text-xs font-medium text-red-500">Fully Unavailable</p>
        ) : (
          <p className="text-xs font-medium text-gray-500">
            Available: {safeFormatTime(override.startTime)} - {safeFormatTime(override.endTime)}
          </p>
        )}
      </div>

      <button 
        onClick={() => onDelete(override.id)}
        className="p-2 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors tooltip"
        title="Delete Override"
      >
        <Trash2 className="w-4 h-4" />
      </button>

    </div>
  );
}