import React from 'react';
import { Calendar, Clock, ArrowRight } from 'lucide-react';
import StatusBadge from '../../appointment/components/StatusBadge';

export default function NextAppointmentCard({ appointment, onManage }) {
  if (!appointment) return null;

  return (
    <div className="bg-white border border-gray-200 rounded-xl p-6 shadow-sm mb-8">
      <div className="flex justify-between items-start mb-6">
        <div>
          <p className="text-sm font-medium text-gray-500 mb-1">Next Appointment</p>
          <h3 className="text-2xl font-bold text-gray-900">{appointment.establishmentName}</h3>
        </div>
        <StatusBadge status={appointment.status} />
      </div>

      <div className="flex gap-6 text-gray-600 mb-6">
        <div className="flex items-center gap-2">
          <Calendar className="w-5 h-5 text-gray-400" />
          <span className="font-medium">{appointment.date}</span>
        </div>
        <div className="flex items-center gap-2">
          <Clock className="w-5 h-5 text-gray-400" />
          <span className="font-medium">{appointment.time}</span>
        </div>
      </div>

      <button 
        onClick={() => onManage(appointment.id)}
        className="inline-flex items-center gap-2 bg-blue-600 text-white px-5 py-2.5 rounded-lg font-medium hover:bg-blue-700 transition-colors"
      >
        Manage Appointment
        <ArrowRight className="w-4 h-4" />
      </button>
    </div>
  );
}