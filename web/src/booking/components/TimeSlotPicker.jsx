import React from "react";
// 🚨 1. Import your AuthContext to get the user status
import { useAuth } from "../../auth/context/AuthContext"; // Adjust path if necessary

const selectedDateFormat = new Intl.DateTimeFormat("en-US", { 
  weekday: "long", 
  month: "long", 
  day: "numeric", 
  year: "numeric" 
});

export function TimeSlotPicker({ 
  selectedDate, 
  availableSlots, 
  loadingSlots, 
  selectedTime, 
  setSelectedTime, 
  isBooking, 
  handleBooking 
}) {
  // 🚨 2. Pull the user from context
  const { user } = useAuth();

  // 🚨 3. Check if they are unverified
  const isUnverified = user && !user.isEmailVerified;

  return (
    <div className="w-full lg:w-1/3 bg-white rounded-2xl border border-gray-200 p-8 shadow-sm lg:sticky lg:top-24">
      <h2 className="text-xl font-bold text-gray-900 mb-2">
        Available Times
      </h2>
      <p className="text-sm text-gray-500 mb-6">
        {selectedDateFormat.format(selectedDate)}
      </p>

      {/* --- THE GRID LOGIC --- */}
      {loadingSlots ? (
        <div className="flex justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-3 mb-8">
          {availableSlots && availableSlots.length > 0 ? (
            availableSlots.map((slot, index) => (
              <button
                key={index}
                onClick={() => !slot.booked && setSelectedTime(slot.time)}
                disabled={slot.booked}
                className={`py-2.5 rounded-lg text-sm font-medium border transition-all
                  ${
                    slot.booked
                      ? "border-gray-100 bg-gray-50 text-gray-300 cursor-not-allowed"
                      : selectedTime === slot.time
                        ? "border-blue-600 bg-blue-600 text-white shadow-md"
                        : "border-gray-200 bg-white text-gray-700 hover:border-blue-600 hover:text-blue-600"
                  }
                `}
              >
                {slot.time}
              </button>
            ))
          ) : (
            <div className="col-span-2 text-center py-8 text-gray-500 text-sm">
              No available slots for this date.
            </div>
          )}
        </div>
      )}
      {/* ------------------------------ */}

      {/* 🚨 4. The Soft-Blocked Button Container */}
      <div className="relative group mt-8">
        <button
          onClick={handleBooking}
          // Button is disabled if no time selected, is currently booking, OR is unverified
          disabled={!selectedTime || isBooking || isUnverified}
          className={`w-full flex justify-center items-center gap-2 py-3.5 rounded-xl font-semibold shadow-sm transition-colors ${
            isUnverified
              ? "bg-gray-100 text-gray-400 border border-gray-200 cursor-not-allowed" // Grayed out state
              : "bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed" // Normal state
          }`}
        >
          {isBooking ? "Confirming..." : "Confirm Booking"}
        </button>

        {/* 🚨 5. The Yellow Tooltip (Only renders if unverified) */}
        {isUnverified && (
          <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-64 p-2.5 bg-yellow-50 border border-yellow-200 text-yellow-800 text-xs font-medium text-center rounded-lg shadow-md opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none z-50">
            You need to verify your email address to book appointments.
          </div>
        )}
      </div>
      
    </div>
  );
}