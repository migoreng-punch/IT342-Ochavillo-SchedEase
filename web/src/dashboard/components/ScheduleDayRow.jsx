import React from 'react';

export default function ScheduleDayRow({ dayData, onChange }) {
  const { dayOfWeek, isWorkingDay, startTime, endTime } = dayData;

  // Capitalize first letter, lowercase the rest (e.g., MONDAY -> Monday)
  const formattedDayName = dayOfWeek.charAt(0) + dayOfWeek.slice(1).toLowerCase();

  return (
    <div className="bg-white p-5 rounded-xl border border-gray-200 flex items-center justify-between gap-6 shadow-sm transition-opacity duration-200">
      
      {/* Left: Toggle & Day Name */}
      <div className="flex items-center gap-4 w-40">
        <label className="relative inline-flex items-center cursor-pointer">
          <input 
            type="checkbox" 
            className="sr-only peer" 
            checked={isWorkingDay}
            onChange={(e) => onChange(dayOfWeek, 'isWorkingDay', e.target.checked)}
          />
          <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
        </label>
        <span className={`font-semibold ${isWorkingDay ? 'text-gray-900' : 'text-gray-400'}`}>
          {formattedDayName}
        </span>
      </div>

      {/* Right: Time Inputs */}
      <div className={`flex flex-1 items-center gap-6 ${isWorkingDay ? 'opacity-100' : 'opacity-40 pointer-events-none'}`}>
        
        {/* Start Time */}
        <div className="flex-1">
          <label className="block text-xs font-medium text-gray-500 mb-1">Start Time</label>
          <input 
            type="time" 
            value={startTime || ''}
            onChange={(e) => onChange(dayOfWeek, 'startTime', e.target.value)}
            disabled={!isWorkingDay}
            className="w-full p-2.5 border border-gray-300 rounded-lg text-sm text-gray-900 focus:ring-blue-500 focus:border-blue-500 bg-gray-50 outline-none transition-colors"
          />
        </div>

        {/* End Time */}
        <div className="flex-1">
          <label className="block text-xs font-medium text-gray-500 mb-1">End Time</label>
          <input 
            type="time" 
            value={endTime || ''}
            onChange={(e) => onChange(dayOfWeek, 'endTime', e.target.value)}
            disabled={!isWorkingDay}
            className="w-full p-2.5 border border-gray-300 rounded-lg text-sm text-gray-900 focus:ring-blue-500 focus:border-blue-500 bg-gray-50 outline-none transition-colors"
          />
        </div>

      </div>
    </div>
  );
}