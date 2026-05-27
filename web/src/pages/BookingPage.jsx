import React from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Header } from "../layouts/Header"; // Global Layout
import { useBookingFlow } from "../booking/hooks/useBookingFlow";
import { EstablishmentInfo } from "../booking/components/EstablishmentInfo";
import { BookingCalendar } from "../booking/components/BookingCalendar";
import { TimeSlotPicker } from "../booking/components/TimeSlotPicker";
import { CheckCircle2, AlertCircle } from "lucide-react";

export default function BookingPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const {
    establishment,
    loading,
    currentMonth, setCurrentMonth,
    selectedDate, setSelectedDate,
    availableSlots, loadingSlots,
    selectedTime, setSelectedTime,
    isBooking, 
    handleBooking,
    feedback // 🚨 1. Pull the feedback state directly from your updated hook!
  } = useBookingFlow(id);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col font-sans text-gray-900">
      <main className="flex-grow max-w-6xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-8">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-sm text-gray-500 hover:text-blue-600 mb-6 transition-colors"
        >
          Back to browse
        </button>

        {/* 🚨 2. The Banner (Now reacts instantly to the hook's state) */}
        {feedback?.message && (
          <div className={`mb-6 p-4 rounded-xl flex items-start gap-3 text-sm font-medium animate-in fade-in slide-in-from-top-2 ${
            feedback.type === "success" 
              ? "bg-green-50 text-green-800 border border-green-200" 
              : "bg-red-50 text-red-800 border border-red-200"
          }`}>
            {feedback.type === "success" ? (
              <CheckCircle2 className="w-5 h-5 text-green-600 shrink-0 mt-0.5" />
            ) : (
              <AlertCircle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
            )}
            <p>{feedback.message}</p>
          </div>
        )}

        <div className="flex flex-col lg:flex-row gap-6 items-start">
          <div className="w-full lg:w-2/3 space-y-6">
            <EstablishmentInfo establishment={establishment} />
            
            <BookingCalendar 
              currentMonth={currentMonth}
              setCurrentMonth={setCurrentMonth}
              selectedDate={selectedDate}
              setSelectedDate={setSelectedDate}
            />
          </div>

          <TimeSlotPicker 
            selectedDate={selectedDate}
            availableSlots={availableSlots}
            loadingSlots={loadingSlots}
            selectedTime={selectedTime}
            setSelectedTime={setSelectedTime}
            isBooking={isBooking}
            handleBooking={handleBooking} // 🚨 3. Pass the raw hook function directly
          />
        </div>
      </main>
    </div>
  );
}