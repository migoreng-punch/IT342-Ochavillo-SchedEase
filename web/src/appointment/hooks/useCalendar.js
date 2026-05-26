import { useState, useMemo } from 'react';

export function useCalendar() {
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(null);

  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const calendarData = useMemo(() => {
    const year = currentMonth.getFullYear();
    const month = currentMonth.getMonth();
    
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();

    const blanks = Array.from({ length: firstDay }, () => null);
    const days = Array.from({ length: daysInMonth }, (_, i) => {
      const date = new Date(year, month, i + 1);
      date.setHours(0, 0, 0, 0);
      return {
        dayNum: i + 1,
        dateObject: date,
        isPast: date < today,
        isSelected: selectedDate?.getTime() === date.getTime()
      };
    });

    return { 
      blanks, 
      days, 
      monthName: currentMonth.toLocaleString('default', { month: 'long', year: 'numeric' }) 
    };
  }, [currentMonth, selectedDate]); // eslint-disable-line react-hooks/exhaustive-deps

  const nextMonth = () => setCurrentMonth(new Date(currentMonth.setMonth(currentMonth.getMonth() + 1)));
  const prevMonth = () => setCurrentMonth(new Date(currentMonth.setMonth(currentMonth.getMonth() - 1)));

  return {
    selectedDate,
    setSelectedDate,
    calendarData,
    nextMonth,
    prevMonth
  };
}