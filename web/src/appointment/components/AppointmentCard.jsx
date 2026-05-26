import React from 'react';
import { Calendar, Clock, RefreshCw, X } from 'lucide-react';
import StatusBadge from './StatusBadge';

export default function AppointmentCard({ appointment, onReschedule, onCancel }) {
  // Only show action buttons for active appointments
  const isActive = appointment.status === 'Confirmed' || appointment.status === 'Pending';

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:shadow-md transition-shadow">
      
      {/* Left Side: Info */}
      <div>
        <h3 className="text-lg font-bold text-gray-900 mb-2">{appointment.establishmentName}</h3>
        <div className="flex flex-wrap items-center gap-4 text-sm text-gray-500 font-medium">
          <div className="flex items-center gap-1.5">
            <Calendar className="w-4 h-4 text-gray-400" />
            <span>{appointment.date}</span>
          </div>
          <div className="flex items-center gap-1.5">
            <Clock className="w-4 h-4 text-gray-400" />
            <span>{appointment.time}</span>
          </div>
        </div>
      </div>

      {/* Right Side: Status & Actions */}
      <div className="flex items-center gap-6">
        <StatusBadge status={appointment.status} />
        
        {isActive && (
          <div className="flex items-center gap-2 border-l border-gray-200 pl-4">
            <button 
              onClick={() => onReschedule(appointment.id)}
              className="p-2 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors"
              title="Reschedule"
            >
              <RefreshCw className="w-5 h-5" />
            </button>
            <button 
              onClick={() => onCancel(appointment.id)}
              className="p-2 text-red-500 hover:bg-red-50 rounded-lg transition-colors"
              title="Cancel"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        )}
      </div>
      
    </div>
  );
}