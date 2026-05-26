import React, { useEffect, useState, useMemo } from 'react';
import { useProviderAppointments } from '../appointment/hooks/useProviderAppointments'; // Reuse your hook!
import ProviderAppointmentRow from '../appointment/components/ProviderAppointmentRow';
import { Loader2 } from 'lucide-react';

export default function ProviderAppointmentsPage() {
  const { appointments, loading, error, fetchAppointments, updateStatus } = useProviderAppointments();
  const [filter, setFilter] = useState('ALL');

  useEffect(() => {
    fetchAppointments();
  }, [fetchAppointments]);

  const filteredAppointments = useMemo(() => {
    if (filter === 'ALL') return appointments;
    return appointments.filter(apt => apt.status.toUpperCase() === filter);
  }, [appointments, filter]);

  const tabs = ['ALL', 'PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'];

  return (
    <div className="max-w-5xl mx-auto p-2">
      
      {/* Header and Filter Navigation */}
      <div className="mb-6 border-b border-gray-200">
        <h1 className="text-2xl font-bold text-gray-900 mb-6">Appointments</h1>
        <nav className="flex space-x-8">
          {tabs.map((tab) => (
            <button
              key={tab}
              onClick={() => setFilter(tab)}
              className={`py-4 px-1 border-b-2 font-semibold text-sm transition-colors ${
                filter === tab
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              {tab.charAt(0) + tab.slice(1).toLowerCase()}
            </button>
          ))}
        </nav>
      </div>

      {/* Error State Banner */}
      {error && (
        <div className="bg-red-50 text-red-600 p-4 rounded-xl border border-red-100 mb-6">
          {error}
        </div>
      )}

      {/* Core Async UI State Handling */}
      {loading ? (
        <div className="flex justify-center items-center h-64">
          <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              
              {/* Table Header Section */}
              <thead>
                <tr className="bg-gray-50/70 border-b border-gray-200">
                  <th className="px-6 py-4.5 text-xs font-bold uppercase tracking-wider text-gray-600">Client</th>
                  <th className="px-6 py-4.5 text-xs font-bold uppercase tracking-wider text-gray-600">Date</th>
                  <th className="px-6 py-4.5 text-xs font-bold uppercase tracking-wider text-gray-600">Time</th>
                  <th className="px-6 py-4.5 text-xs font-bold uppercase tracking-wider text-gray-600">Status</th>
                  <th className="px-6 py-4.5 text-xs font-bold uppercase tracking-wider text-gray-600">Actions</th>
                </tr>
              </thead>

              {/* Table Content Body */}
              <tbody className="divide-y divide-gray-100">
                {filteredAppointments.length > 0 ? (
                  filteredAppointments.map((apt) => (
                    <ProviderAppointmentRow 
                      key={apt.id} 
                      appointment={apt} 
                      onUpdateStatus={updateStatus} 
                    />
                  ))
                ) : (
                  <tr>
                    <td colSpan="5" className="px-6 py-12 text-center text-gray-500 font-medium">
                      No {filter !== 'ALL' ? filter.toLowerCase() : ''} appointments found.
                    </td>
                  </tr>
                )}
              </tbody>

            </table>
          </div>
        </div>
      )}
    </div>
  );
}