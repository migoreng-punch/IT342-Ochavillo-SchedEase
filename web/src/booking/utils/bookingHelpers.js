export const getDaysInMonth = (year, month) => new Date(year, month + 1, 0).getDate();

export const getFirstDayOfMonth = (year, month) => new Date(year, month, 1).getDay();

export const formatTimeForJava = (time12h) => {
  if (!time12h) return "00:00:00";
  const [time, modifier] = time12h.split(" ");
  let [hours, minutes] = time.split(":");

  if (hours === "12") hours = "00";
  if (modifier === "PM") hours = parseInt(hours, 10) + 12;

  return `${hours.toString().padStart(2, "0")}:${minutes}:00`;
};

export const formatTimeForUI = (time24h) => {
  const [hourString, minute] = time24h.split(":");
  let hour = parseInt(hourString, 10);
  const modifier = hour >= 12 ? "PM" : "AM";
  if (hour === 0) hour = 12;
  if (hour > 12) hour -= 12;
  return `${hour}:${minute} ${modifier}`;
};

export const formatDateForAPI = (dateObj) => {
  const year = dateObj.getFullYear();
  const month = String(dateObj.getMonth() + 1).padStart(2, "0");
  const day = String(dateObj.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};