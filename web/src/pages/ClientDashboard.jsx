import React, { useState } from 'react';
import { Search, Filter, ArrowRight, Loader2 } from 'lucide-react';
import { Link } from 'react-router-dom';

// Hooks
import { useDashboardData } from '../dashboard/hooks/useDashboardData'; 
import { useReschedule } from '../appointment/hooks/useReschedule'; // 🚨 NEW

// Components
import DashboardAppointmentCard from '../dashboard/components/DashboardAppointmentCard';
import BookingDetailsModal from '../appointment/components/BookingDetailsModal'; 
import RescheduleModal from '../appointment/components/RescheduleModal'; // 🚨 NEW

export default function ClientDashboard() {
  // Make sure updateAppointmentStatus is destructured here!
  const { user, stats, allAppointments, aptLoading, aptError, updateAppointmentStatus } = useDashboardData();
  
  // Bring in the reschedule hook
  const { availableTimes, isLoadingTimes, rescheduleError, fetchAvailableTimes, submitReschedule, setRescheduleError } = useReschedule();
  
  // UI State for filtering
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState('All');
  
  // Modal States
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [appointmentToReschedule, setAppointmentToReschedule] = useState(null); // 🚨 NEW

  // Apply Search and Filters
  const filteredAppointments = allAppointments?.filter(apt => {
    const matchesSearch = apt.establishmentName.toLowerCase().includes(searchQuery.toLowerCase());
    let matchesTab = true;
    if (activeFilter === 'Upcoming') {
      matchesTab = apt.status === 'CONFIRMED' || apt.status === 'PENDING';
    } else if (activeFilter === 'Completed') {
      matchesTab = apt.status === 'COMPLETED';
    } else if (activeFilter === 'Cancelled') {
      matchesTab = apt.status === 'CANCELLED';
    }
    return matchesSearch && matchesTab;
  }) || [];

  // --- Handlers ---
  const handleCancelAppointment = async (appointment) => {
    const isConfirmed = window.confirm("Are you sure you want to cancel this appointment?");
    if (!isConfirmed) return;

    try {
      await updateAppointmentStatus(appointment.id, 'CANCELLED');
      alert("Appointment successfully cancelled.");
    } catch (err) {
      alert("Failed to cancel appointment. " + (err.response?.data?.message || ""));
    }
  };

  const handleRescheduleSubmit = async (payload) => {
    const success = await submitReschedule(appointmentToReschedule.id, payload);
    if (success) {
      alert("Appointment rescheduled successfully!");
      setAppointmentToReschedule(null); // Close modal
      window.location.reload(); // Refresh page to recalculate stats and fetch fresh data
    }
  };

  return (
    <div className="min-h-screen bg-[#F8FAFC] font-sans pb-16">
      <main className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        
        {/* --- 1. Header Section --- */}
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8">
          <div>
            <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight mb-1">
              My Appointments
            </h1>
            <p className="text-gray-500 text-base">Manage and track all your bookings</p>
          </div>
          <Link 
            to="/homepage" 
            className="inline-flex items-center gap-2 bg-blue-600 text-white px-5 py-2.5 rounded-xl font-medium hover:bg-blue-700 transition-colors shadow-sm"
          >
            Book New <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        {/* --- 2. Stats Grid --- */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
          {[
            { label: 'Upcoming', value: stats?.upcoming || 0, color: 'text-blue-600' },
            { label: 'Pending', value: stats?.pending || 0, color: 'text-yellow-600' },
            { label: 'Completed', value: stats?.completed || 0, color: 'text-purple-600' },
            { label: 'Cancelled', value: stats?.cancelled || 0, color: 'text-red-600' }
          ].map(stat => (
            <div key={stat.label} className="bg-white rounded-2xl border border-gray-200 p-5 shadow-sm">
              <p className="text-sm font-medium text-gray-500 mb-2">{stat.label}</p>
              <p className={`text-3xl font-bold ${stat.color}`}>{stat.value}</p>
            </div>
          ))}
        </div>

        {/* --- 3. Controls (Search & Filters) --- */}
        <div className="flex flex-col md:flex-row gap-4 mb-8">
          <div className="relative flex-grow">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <Search className="h-4 w-4 text-gray-400" />
            </div>
            <input
              type="text"
              placeholder="Search establishments..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-3 bg-white border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-shadow"
            />
          </div>

          <div className="flex items-center gap-1 bg-white border border-gray-200 rounded-xl p-1.5 shadow-sm overflow-x-auto shrink-0">
            <div className="pl-3 pr-2 border-r border-gray-100">
              <Filter className="w-4 h-4 text-gray-400" />
            </div>
            {['All', 'Upcoming', 'Completed', 'Cancelled'].map(filter => (
              <button
                key={filter}
                onClick={() => setActiveFilter(filter)}
                className={`
                  px-4 py-1.5 rounded-lg text-sm font-medium transition-all whitespace-nowrap
                  ${activeFilter === filter 
                    ? 'bg-blue-600 text-white shadow-sm' 
                    : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  }
                `}
              >
                {filter}
              </button>
            ))}
          </div>
        </div>

        {/* --- 4. Content Area --- */}
        {aptLoading ? (
          <div className="flex justify-center items-center py-20">
            <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
          </div>
        ) : aptError ? (
          <div className="bg-red-50 text-red-600 p-4 rounded-xl border border-red-100">
            {aptError}
          </div>
        ) : filteredAppointments.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {filteredAppointments.map(apt => (
              <DashboardAppointmentCard 
                key={apt.id} 
                appointment={apt} 
                onViewDetails={() => setSelectedAppointment(apt)} 
              />
            ))}
          </div>
        ) : (
          <div className="bg-white border border-gray-200 rounded-2xl p-12 text-center shadow-sm">
            <p className="text-gray-500 text-lg font-medium mb-2">No appointments found</p>
            <p className="text-gray-400 text-sm">
              {searchQuery ? "Try adjusting your search terms." : "You don't have any bookings in this category."}
            </p>
          </div>
        )}
      </main>

      {/* --- 5. MODALS --- */}
      
      {/* Details Modal */}
      <BookingDetailsModal 
        isOpen={!!selectedAppointment} 
        onClose={() => setSelectedAppointment(null)}
        appointment={selectedAppointment}
        
        // When user clicks Reschedule inside details
        onReschedule={(apt) => {
          setAppointmentToReschedule(apt);
          setRescheduleError(null); // Clear old errors
        }}
        
        // When user clicks Cancel inside details
        onCancel={(apt) => handleCancelAppointment(apt)}
      />

      {/* Reschedule Modal */}
      {appointmentToReschedule && (
        <RescheduleModal 
          isOpen={!!appointmentToReschedule}
          onClose={() => setAppointmentToReschedule(null)}
          currentAppointment={{ 
            date: appointmentToReschedule.date, 
            time: appointmentToReschedule.time 
          }}
          onDateSelect={(dateStr) => fetchAvailableTimes(appointmentToReschedule.establishmentId, dateStr)}
          availableTimes={availableTimes}
          isLoadingTimes={isLoadingTimes}
          error={rescheduleError}
          onConfirm={handleRescheduleSubmit}
        />
      )}

    </div>
  );
}