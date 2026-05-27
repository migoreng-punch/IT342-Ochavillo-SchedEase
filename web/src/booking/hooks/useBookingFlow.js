import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { useAxiosPrivate } from "../../api/interceptor";
import {
  formatTimeForUI,
  formatDateForAPI,
  formatTimeForJava,
} from "../utils/bookingHelpers";

export function useBookingFlow(establishmentId) {
  const navigate = useNavigate();
  const axiosPrivate = useAxiosPrivate();

  // State
  const [establishment, setEstablishment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [availableSlots, setAvailableSlots] = useState([]);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [selectedTime, setSelectedTime] = useState(null);
  const [isBooking, setIsBooking] = useState(false);

  // 🚨 NEW: Let the hook manage its own feedback messages!
  const [feedback, setFeedback] = useState({ type: "", message: "" });

  // Fetch Establishment
  useEffect(() => {
    const fetchEstablishment = async () => {
      try {
        setLoading(true);
        const response = await axios.get(
          `/api/establishments/${establishmentId}`,
        );
        setEstablishment(response.data);
      } catch (error) {
        console.error("Failed to fetch establishment:", error);
      } finally {
        setLoading(false);
      }
    };
    if (establishmentId) fetchEstablishment();
  }, [establishmentId]);

  // Fetch Slots
  useEffect(() => {
    const fetchSlots = async () => {
      try {
        setLoadingSlots(true);
        setSelectedTime(null);
        setFeedback({ type: "", message: "" }); // Clear feedback when switching dates

        const formattedDate = formatDateForAPI(selectedDate);
        const response = await axiosPrivate.get(
          `/api/establishments/${establishmentId}/slots?date=${formattedDate}`,
        );

        const uiSlots = response.data.map((timeStr) => ({
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
    if (establishmentId) fetchSlots();
  }, [selectedDate, establishmentId, axiosPrivate]);

  // Handlers
  const handleBooking = async () => {
    if (!selectedTime) return;

    try {
      setIsBooking(true);
      setFeedback({ type: "", message: "" });

      const payload = {
        establishmentId: parseInt(establishmentId),
        date: formatDateForAPI(selectedDate),
        startTime: formatTimeForJava(selectedTime),
      };

      await axiosPrivate.post("/api/appointments", payload);

      // 🚨 SEND THE MESSAGE TO THE NEXT PAGE
      navigate("/myappointments", {
        state: {
          successMessage: `Success! Your booking for ${selectedTime} is confirmed.`,
        },
      });
    } catch (error) {
      // Errors stay on this page, so we keep using setFeedback
      const errorMessage =
        error.response?.data?.message || "Failed to book appointment.";
      setFeedback({ type: "error", message: errorMessage });
    } finally {
      setIsBooking(false);
    }
  };

  return {
    establishment,
    loading,
    currentMonth,
    setCurrentMonth,
    selectedDate,
    setSelectedDate,
    availableSlots,
    loadingSlots,
    selectedTime,
    setSelectedTime,
    isBooking,
    handleBooking,
    feedback, // 🚨 Export the feedback state so the UI can see it
  };
}
