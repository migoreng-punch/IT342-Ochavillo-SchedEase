import React from 'react';
import BaseModal from '../../components/BaseModal';
import StatusBadge from './StatusBadge';
import { Calendar, Clock, MapPin, Building2, FileText } from 'lucide-react';

export default function BookingDetailsModal({ 
  isOpen, 
  onClose, 
  appointment, 
  onReschedule, 
  onCancel 
}) {
  if (!appointment) return null;

  const isActive = appointment.status === 'CONFIRMED' || appointment.status === 'PENDING';

  return (
    <BaseModal isOpen={isOpen} onClose={onClose} title="Booking Details">
      
      <div className="p-6">
        {/* Status & Establishment Header */}
        <div className="flex justify-between items-start mb-6">
          <div>
            <h3 className="text-xl font-bold text-gray-900 mb-1">
              {appointment.establishmentName}
            </h3>
            <p className="text-sm text-gray-500 flex items-center gap-1.5">
              <Building2 className="w-4 h-4" /> Service Provider
            </p>
          </div>
          <StatusBadge status={appointment.status} />
        </div>

        {/* Details Grid */}
        <div className="bg-gray-50 border border-gray-100 rounded-xl p-5 mb-6 space-y-4">
          <div className="flex items-center gap-3 text-gray-700">
            <div className="p-2 bg-white rounded-lg shadow-sm">
              <Calendar className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-0.5">Date</p>
              <p className="font-semibold">{appointment.date}</p>
            </div>
          </div>

          <div className="flex items-center gap-3 text-gray-700">
            <div className="p-2 bg-white rounded-lg shadow-sm">
              <Clock className="w-5 h-5 text-blue-600" />
            </div>
            <div>
              <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-0.5">Time</p>
              <p className="font-semibold">{appointment.time}</p>
            </div>
          </div>

          {/* Optional: Add Address if you have it in your DTO! */}
          {appointment.address && (
            <div className="flex items-center gap-3 text-gray-700">
              <div className="p-2 bg-white rounded-lg shadow-sm">
                <MapPin className="w-5 h-5 text-blue-600" />
              </div>
              <div>
                <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-0.5">Location</p>
                <p className="font-semibold">{appointment.address}</p>
              </div>
            </div>
          )}
        </div>

        {/* Appointment ID / Notes */}
        <div className="flex items-start gap-2 text-sm text-gray-500 mb-2">
          <FileText className="w-4 h-4 mt-0.5 shrink-0" />
          <p>
            Booking ID: <span className="font-mono text-gray-700">{appointment.id.split('-')[0].toUpperCase()}</span><br/>
            Please arrive 5 minutes before your scheduled time.
          </p>
        </div>
      </div>

      {/* Action Footer (Only show if the appointment hasn't happened yet) */}
      {isActive && (
        <div className="p-5 border-t border-gray-100 bg-gray-50/50 flex gap-3 justify-end">
          <button 
            onClick={() => {
              onClose();
              onCancel(appointment);
            }}
            className="px-5 py-2.5 rounded-xl text-sm font-semibold text-red-600 bg-red-50 hover:bg-red-100 transition-colors"
          >
            Cancel Booking
          </button>
          <button 
            onClick={() => {
              onClose();
              onReschedule(appointment);
            }}
            className="px-5 py-2.5 rounded-xl text-sm font-semibold text-white bg-blue-600 hover:bg-blue-700 shadow-sm transition-colors"
          >
            Reschedule
          </button>
        </div>
      )}
    </BaseModal>
  );
}