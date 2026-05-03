import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import axios from "axios";
import { useAxiosPrivate } from "../api/interceptor";

export default function BookingPage() {
  const { id } = useParams(); // Gets the ID from the URL (e.g., /establishment/1)
  const navigate = useNavigate();

  const axiosPrivate = useAxiosPrivate();
  // --- State Management ---
  const [establishment, setEstablishment] = useState(null);
  const [loading, setLoading] = useState(true);

  // Calendar State
  const [currentMonth, setCurrentMonth] = useState(new Date()); // Controls which month is visible
  const [selectedDate, setSelectedDate] = useState(new Date()); // The actual day clicked

  // Slots State
  const [availableSlots, setAvailableSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [selectedTime, setSelectedTime] = useState(null);

  const [isBooking, setIsBooking] = useState(false);

  // --- 1. Fetch Establishment Details ---
  useEffect(() => {
    const fetchEstablishment = async () => {
      try {
        setLoading(true);
        // Hitting the endpoint we built: GET /api/establishments/{id}
        const response = await axios.get(`/api/establishments/${id}`);
        setEstablishment(response.data);
      } catch (error) {
        console.error("Failed to fetch establishment:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchEstablishment();
  }, [id]);

  // --- 2. Fetch Available Slots when Date Changes ---
  useEffect(() => {
    const fetchSlots = async () => {
      try {
        setLoadingSlots(true);
        setSelectedTime(null);

        // 🚨 THE FIX: Manually build the YYYY-MM-DD string using LOCAL time
        const year = selectedDate.getFullYear();
        const month = String(selectedDate.getMonth() + 1).padStart(2, "0");
        const day = String(selectedDate.getDate()).padStart(2, "0");
        const formattedDate = `${year}-${month}-${day}`;

        // ... everything else stays exactly the same!
        const response = await axiosPrivate.get(
          `/api/establishments/${id}/slots?date=${formattedDate}`,
        );

        const formatTimeForUI = (time24h) => {
          const [hourString, minute] = time24h.split(":");
          let hour = parseInt(hourString, 10);
          const modifier = hour >= 12 ? "PM" : "AM";
          if (hour === 0) hour = 12;
          if (hour > 12) hour -= 12;
          return `${hour}:${minute} ${modifier}`;
        };

        const rawSlots = response.data;
        const uiSlots = rawSlots.map((timeStr) => ({
          time: formatTimeForUI(timeStr),
          booked: false,
        }));

        setAvailableSlots(uiSlots);
      } catch (error) {
        console.error("Failed to fetch slots:", error);
        setAvailableSlots([]);
      } finally {
        setLoadingSlots(false);
      }
    };

    fetchSlots();
  }, [selectedDate, id, axiosPrivate]);

  // --- Calendar Logic ---
  const getDaysInMonth = (year, month) =>
    new Date(year, month + 1, 0).getDate();
  const getFirstDayOfMonth = (year, month) => new Date(year, month, 1).getDay();

  const handlePrevMonth = () => {
    setCurrentMonth(
      new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1),
    );
  };

  const handleNextMonth = () => {
    setCurrentMonth(
      new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1),
    );
  };

  const renderCalendarDays = () => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const daysInMonth = getDaysInMonth(year, month);
    const firstDay = getFirstDayOfMonth(year, month);

    const days = [];

    // Empty padding days for alignment
    for (let i = 0; i < firstDay; i++) {
      days.push(<div key={`empty-${i}`} className="h-10 w-10"></div>);
    }

    // Actual days
    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(year, month, day);
      const isSelected = selectedDate.toDateString() === date.toDateString();
      const isPast = date < new Date(new Date().setHours(0, 0, 0, 0)); // Don't allow past dates

      days.push(
        <button
          key={day}
          onClick={() => !isPast && setSelectedDate(date)}
          disabled={isPast}
          className={`h-10 w-10 flex items-center justify-center rounded-lg text-sm font-medium transition-colors
            ${isSelected ? "bg-blue-600 text-white shadow-md" : ""}
            ${!isSelected && !isPast ? "text-gray-700 hover:bg-gray-100" : ""}
            ${isPast ? "text-gray-300 cursor-not-allowed" : ""}
          `}
        >
          {day}
        </button>,
      );
    }
    return days;
  };

  // --- Formatters ---
  const monthYearFormat = new Intl.DateTimeFormat("en-US", {
    month: "long",
    year: "numeric",
  });
  const selectedDateFormat = new Intl.DateTimeFormat("en-US", {
    weekday: "long",
    month: "long",
    day: "numeric",
    year: "numeric",
  });

  // --- Handlers ---
  const handleBooking = async () => {
    if (!selectedTime) return;

    try {
      setIsBooking(true);

      // 🚨 THE FIX: Use local time here too, no more toISOString()!
      const year = selectedDate.getFullYear();
      const month = String(selectedDate.getMonth() + 1).padStart(2, "0");
      const day = String(selectedDate.getDate()).padStart(2, "0");
      const formattedDate = `${year}-${month}-${day}`;

      const formattedTime = formatTimeForJava(selectedTime);

      const payload = {
        establishmentId: parseInt(id),
        date: formattedDate,
        startTime: formattedTime,
      };

      await axiosPrivate.post("/api/appointments", payload);

      alert(`Success! Your booking for ${selectedTime} is confirmed.`);
      navigate("/appointments");
    } catch (error) {
      console.error("Booking failed:", error);
      const errorMessage =
        error.response?.data?.message || "Failed to book appointment.";
      alert(`Error: ${errorMessage}`);
    } finally {
      setIsBooking(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col font-sans text-gray-900">
      {/* HEADER (Reused) */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2">
            <svg
              className="w-6 h-6 text-blue-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
              />
            </svg>
            <span className="text-xl font-bold tracking-tight">SchedEase</span>
          </Link>
          <nav className="hidden md:flex items-center gap-8">
            <Link
              to="/browse"
              className="text-gray-500 font-medium hover:text-gray-900"
            >
              Browse
            </Link>
            <Link
              to="/appointments"
              className="text-gray-500 font-medium hover:text-gray-900"
            >
              My Appointments
            </Link>
          </nav>
        </div>
      </header>

      <main className="flex-grow max-w-6xl mx-auto w-full px-4 sm:px-6 lg:px-8 py-8">
        {/* Back Button */}
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-sm text-gray-500 hover:text-blue-600 mb-6 transition-colors"
        >
          <svg
            className="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M10 19l-7-7m0 0l7-7m-7 7h18"
            />
          </svg>
          Back to browse
        </button>

        <div className="flex flex-col lg:flex-row gap-6 items-start">
          {/* LEFT COLUMN: Info & Calendar */}
          <div className="w-full lg:w-2/3 space-y-6">
            {/* Establishment Info Card */}
            {establishment && (
              <div className="bg-white rounded-2xl border border-gray-200 p-8 shadow-sm">
                <h1 className="text-3xl font-bold text-gray-900 mb-3">
                  {establishment.name}
                </h1>
                <p className="text-gray-500 mb-6">
                  {establishment.description}
                </p>
                <div className="space-y-3">
                  <div className="flex items-center gap-3 text-gray-600">
                    <svg
                      className="w-5 h-5"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                      />
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                      />
                    </svg>
                    <span>{establishment.address}</span>
                  </div>
                  <div className="flex items-center gap-3 text-gray-600">
                    <svg
                      className="w-5 h-5"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                      />
                    </svg>
                    <span>{establishment.contactEmail}</span>
                  </div>
                </div>
              </div>
            )}

            {/* Calendar Card */}
            <div className="bg-white rounded-2xl border border-gray-200 p-8 shadow-sm">
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-xl font-bold text-gray-900">Select Date</h2>
                <div className="flex items-center gap-4">
                  <button
                    onClick={handlePrevMonth}
                    className="p-1.5 rounded-md border border-gray-200 hover:bg-gray-50 text-gray-600"
                  >
                    <svg
                      className="w-4 h-4"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M15 19l-7-7 7-7"
                      />
                    </svg>
                  </button>
                  <span className="font-semibold text-gray-900 min-w-[120px] text-center">
                    {monthYearFormat.format(currentMonth)}
                  </span>
                  <button
                    onClick={handleNextMonth}
                    className="p-1.5 rounded-md border border-gray-200 hover:bg-gray-50 text-gray-600"
                  >
                    <svg
                      className="w-4 h-4"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 5l7 7-7 7"
                      />
                    </svg>
                  </button>
                </div>
              </div>

              {/* Calendar Grid */}
              <div className="grid grid-cols-7 gap-y-4 gap-x-2 text-center justify-items-center">
                {["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map(
                  (day) => (
                    <div
                      key={day}
                      className="text-xs font-semibold text-gray-400 w-10"
                    >
                      {day}
                    </div>
                  ),
                )}
                {renderCalendarDays()}
              </div>
            </div>
          </div>

          {/* RIGHT COLUMN: Available Times */}
          <div className="w-full lg:w-1/3 bg-white rounded-2xl border border-gray-200 p-8 shadow-sm lg:sticky lg:top-24">
            <h2 className="text-xl font-bold text-gray-900 mb-2">
              Available Times
            </h2>
            <p className="text-sm text-gray-500 mb-6">
              {selectedDateFormat.format(selectedDate)}
            </p>

            {loadingSlots ? (
              <div className="flex justify-center py-12">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              </div>
            ) : (
              <div className="grid grid-cols-2 gap-3 mb-8">
                {availableSlots.length > 0 ? (
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

            <button
              onClick={handleBooking}
              disabled={!selectedTime || isBooking}
              className="w-full flex justify-center items-center gap-2 bg-blue-600 text-white py-3.5 rounded-xl font-semibold shadow-sm hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isBooking ? (
                <>
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                  Confirming...
                </>
              ) : (
                <>
                  <svg
                    className="w-5 h-5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                  Confirm Booking
                </>
              )}
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

// --- Helper Functions ---
const formatTimeForJava = (time12h) => {
  if (!time12h) return "00:00:00";
  const [time, modifier] = time12h.split(" ");
  let [hours, minutes] = time.split(":");

  if (hours === "12") hours = "00";
  if (modifier === "PM") hours = parseInt(hours, 10) + 12;

  return `${hours.toString().padStart(2, "0")}:${minutes}:00`;
};
