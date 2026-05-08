import { getDaysInMonth, getFirstDayOfMonth } from "../utils/bookingHelpers";

const monthYearFormat = new Intl.DateTimeFormat("en-US", { month: "long", year: "numeric" });

export function BookingCalendar({ currentMonth, setCurrentMonth, selectedDate, setSelectedDate }) {
  
  const handlePrevMonth = () => setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1));
  const handleNextMonth = () => setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1));

  const renderCalendarDays = () => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    const daysInMonth = getDaysInMonth(year, month);
    const firstDay = getFirstDayOfMonth(year, month);
    const days = [];

    for (let i = 0; i < firstDay; i++) {
      days.push(<div key={`empty-${i}`} className="h-10 w-10"></div>);
    }

    for (let day = 1; day <= daysInMonth; day++) {
      const date = new Date(year, month, day);
      const isSelected = selectedDate.toDateString() === date.toDateString();
      const isPast = date < new Date(new Date().setHours(0, 0, 0, 0));

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
        </button>
      );
    }
    return days;
  };

  return (
    <div className="bg-white rounded-2xl border border-gray-200 p-8 shadow-sm">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-bold text-gray-900">Select Date</h2>
        <div className="flex items-center gap-4">
          <button onClick={handlePrevMonth}> {/* ... SVG Icon ... */} </button>
          <span className="font-semibold text-gray-900 min-w-[120px] text-center">
            {monthYearFormat.format(currentMonth)}
          </span>
          <button onClick={handleNextMonth}> {/* ... SVG Icon ... */} </button>
        </div>
      </div>
      <div className="grid grid-cols-7 gap-y-4 gap-x-2 text-center justify-items-center">
        {["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map((day) => (
          <div key={day} className="text-xs font-semibold text-gray-400 w-10">{day}</div>
        ))}
        {renderCalendarDays()}
      </div>
    </div>
  );
}