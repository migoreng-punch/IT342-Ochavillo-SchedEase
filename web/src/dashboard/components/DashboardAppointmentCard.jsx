import React from 'react';
import { Calendar, Clock, ChevronRight, RotateCcw } from 'lucide-react';
import StatusBadge from '../../appointment/components/StatusBadge';
import { Link } from 'react-router-dom';

export default function DashboardAppointmentCard({ appointment, onViewDetails }) {
  const isCompleted = appointment.status === 'COMPLETED';

  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm hover:shadow-md transition-shadow flex flex-col">
      
      {/* Header: Badge & Details Button */}
      <div className="flex justify-between items-start mb-4">
        <StatusBadge status={appointment.status} />
        
        {/* 🚨 CHANGED: Converted from <Link> to <button> to trigger the Modal! */}
        <button 
          onClick={onViewDetails}
          className="text-sm font-medium text-gray-500 hover:text-gray-900 flex items-center gap-1 transition-colors"
        >
          Details <ChevronRight className="w-4 h-4" />
        </button>
      </div>

      {/* Body: Establishment & Time */}
      <div className="mb-6 flex-grow">
        <h3 className="text-lg font-bold text-gray-900 mb-3">{appointment.establishmentName}</h3>
        <div className="flex gap-4 text-sm text-gray-500 font-medium">
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

      {/* Footer: Action (Only visible if completed) */}
      {isCompleted && (
        <div className="pt-4 border-t border-gray-100">
          <Link 
            to="/book" 
            className="text-sm font-medium text-blue-600 hover:text-blue-700 flex items-center gap-1.5 transition-colors"
          >
            <RotateCcw className="w-4 h-4" /> Book again
          </Link>
        </div>
      )}
    </div>
  );
}