import React from 'react';
import StatusBadge from '../../appointment/components/StatusBadge';

export default function MiniAppointmentCard({ appointment }) {
  return (
    <div className="flex items-center justify-between p-5 bg-white border border-gray-200 rounded-xl hover:shadow-sm transition-shadow mb-3">
      <div>
        <h4 className="font-semibold text-gray-900 mb-1">{appointment.establishmentName}</h4>
        <div className="flex items-center gap-3 text-sm text-gray-500">
          <span>{appointment.shortDate}</span>
          <span className="text-gray-300">|</span>
          <span>{appointment.time}</span>
        </div>
      </div>
      <StatusBadge status={appointment.status} />
    </div>
  );
}