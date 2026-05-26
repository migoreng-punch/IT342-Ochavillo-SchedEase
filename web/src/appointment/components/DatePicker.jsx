import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export default function DatePicker({ 
  calendarData, 
  onNextMonth, 
  onPrevMonth, 
  onSelectDate 
}) {
  return (
    <div className="border border-gray-200 rounded-xl overflow-hidden bg-white">
      {/* Navigation */}
      <div className="flex items-center justify-between p-4 border-b border-gray-100 bg-gray-50/50">
        <button onClick={onPrevMonth} className="p-1 text-gray-400 hover:text-gray-900 transition-colors">
          <ChevronLeft className="w-5 h-5" />
        </button>
        <h3 className="font-semibold text-gray-900 text-sm">{calendarData.monthName}</h3>
        <button onClick={onNextMonth} className="p-1 text-gray-400 hover:text-gray-900 transition-colors">
          <ChevronRight className="w-5 h-5" />
        </button>
      </div>

      {/* Grid */}
      <div className="p-5">
        <div className="grid grid-cols-7 mb-4">
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
            <div key={day} className="text-center text-xs font-medium text-gray-400">
              {day}
            </div>
          ))}
        </div>

        <div className="grid grid-cols-7 gap-y-2 text-sm">
          {calendarData.blanks.map((_, i) => (
            <div key={`blank-${i}`} className="h-10"></div>
          ))}

          {calendarData.days.map((dayObj) => (
            <div key={dayObj.dayNum} className="flex justify-center items-center h-10">
              <button
                disabled={dayObj.isPast}
                onClick={() => onSelectDate(dayObj.dateObject)}
                className={`
                  w-9 h-9 flex items-center justify-center rounded-lg transition-all font-medium
                  ${dayObj.isPast 
                    ? 'text-gray-300 cursor-not-allowed' 
                    : dayObj.isSelected 
                      ? 'bg-white border-2 border-blue-500 text-blue-600 shadow-sm'
                      : 'text-gray-700 hover:bg-gray-100'
                  }
                `}
              >
                {dayObj.dayNum}
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}