import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { useAxiosPrivate } from "../../api/interceptor"; // Adjust path
import { formatTimeForUI, formatDateForAPI, formatTimeForJava } from "../utils/bookingHelpers";

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

  // Fetch Establishment
  useEffect(() => {
    const fetchEstablishment = async () => {
      try {
        setLoading(true);
        const response = await axios.get(`/api/establishments/${establishmentId}`);
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
        const formattedDate = formatDateForAPI(selectedDate);
        const response = await axiosPrivate.get(
          `/api/establishments/${establishmentId}/slots?date=${formattedDate}`
        );

        const uiSlots = response.data.map((timeStr) => ({
          time: formatTimeForUI(timeStr),
          booked: false, // Update if backend returns booked status
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
      const payload = {
        establishmentId: parseInt(establishmentId),
        date: formatDateForAPI(selectedDate),
        startTime: formatTimeForJava(selectedTime),
      };
      await axiosPrivate.post("/api/appointments", payload);
      alert(`Success! Your booking for ${selectedTime} is confirmed.`);
      navigate("/appointments");
    } catch (error) {
      const errorMessage = error.response?.data?.message || "Failed to book appointment.";
      alert(`Error: ${errorMessage}`);
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
    handleBooking
  };
}