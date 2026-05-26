import React from 'react';
import { Check, X, CheckCircle2 } from 'lucide-react';
import StatusBadge from '../components/StatusBadge'; // Your existing component

export default function ProviderAppointmentRow({ appointment, onUpdateStatus }) {
  const statusUpper = appointment.status.toUpperCase();
  const isPending = statusUpper === 'PENDING';
  const isConfirmed = statusUpper === 'CONFIRMED';
  const isFinalState = statusUpper === 'COMPLETED' || statusUpper === 'CANCELLED';

  return (
    <tr className="border-b border-gray-100 hover:bg-gray-50/50 transition-colors">
      {/* Client Column */}
      <td className="px-6 py-4 text-sm font-semibold text-gray-900">
        {appointment.clientName}
      </td>

      {/* Date Column */}
      <td className="px-6 py-4 text-sm text-gray-500 font-medium">
        {appointment.date}
      </td>

      {/* Time Column */}
      <td className="px-6 py-4 text-sm text-gray-500 font-medium">
        {appointment.time}
      </td>

      {/* Status Column */}
      <td className="px-6 py-4 text-sm">
        <StatusBadge status={appointment.status} />
      </td>

      {/* Actions Column */}
      <td className="px-6 py-4 text-sm">
        <div className="flex items-center gap-2">
          
          {/* 🟡 State 1: Pending -> Confirm or Cancel */}
          {isPending && (
            <>
              <button
                onClick={() => onUpdateStatus(appointment.id, 'CONFIRMED')}
                className="flex items-center gap-1 px-3 py-1.5 bg-green-600 text-white text-xs font-semibold rounded-lg hover:bg-green-700 transition-colors"
              >
                <Check className="w-3.5 h-3.5" />
                Confirm
              </button>
              <button
                onClick={() => onUpdateStatus(appointment.id, 'CANCELLED')}
                className="flex items-center gap-1 px-3 py-1.5 border border-red-200 text-red-600 text-xs font-semibold rounded-lg hover:bg-red-50 transition-colors"
              >
                <X className="w-3.5 h-3.5" />
                Cancel
              </button>
            </>
          )}

          {/* 🟢 State 2: Confirmed -> Complete or Cancel */}
          {isConfirmed && (
            <>
              <button
                onClick={() => onUpdateStatus(appointment.id, 'COMPLETED')}
                className="flex items-center gap-1 px-3 py-1.5 bg-slate-600 text-white text-xs font-semibold rounded-lg hover:bg-slate-700 transition-colors"
              >
                <CheckCircle2 className="w-3.5 h-3.5" />
                Complete
              </button>
              <button
                onClick={() => onUpdateStatus(appointment.id, 'CANCELLED')}
                className="flex items-center gap-1 px-3 py-1.5 border border-red-200 text-red-600 text-xs font-semibold rounded-lg hover:bg-red-50 transition-colors"
              >
                <X className="w-3.5 h-3.5" />
                Cancel
              </button>
            </>
          )}

          {/* ⚪ State 3: Completed or Cancelled -> Disabled Text */}
          {isFinalState && (
            <span className="text-xs font-medium text-gray-400 italic">
              No actions available
            </span>
          )}

        </div>
      </td>
    </tr>
  );
}