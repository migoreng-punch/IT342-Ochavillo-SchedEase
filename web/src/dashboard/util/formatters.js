export const formatTimeForUI = (time24h) => {
  if (!time24h) return "";
  const [hourString, minute] = time24h.split(':');
  let hour = parseInt(hourString, 10);
  const modifier = hour >= 12 ? 'PM' : 'AM';
  if (hour === 0) hour = 12;
  if (hour > 12) hour -= 12;
  return `${hour}:${minute} ${modifier}`;
};

export const formatDateForUI = (dateString, isShort = false) => {
  if (!dateString) return "";
  const [year, month, day] = dateString.split('-');
  const date = new Date(year, month - 1, day);
  
  if (isShort) {
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }
  return date.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });
};