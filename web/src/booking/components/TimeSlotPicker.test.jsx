import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { TimeSlotPicker } from './TimeSlotPicker'; // Named import matches "export function"

describe('TimeSlotPicker Component', () => {
  // 1. Arrange: Match the exact data structure your component expects
  const mockDate = new Date('2026-05-10T12:00:00Z');
  const mockAvailableSlots = [
    { time: '09:00 AM', booked: false },
    { time: '10:00 AM', booked: true },  // This one should be disabled
    { time: '11:00 AM', booked: false },
  ];

  it('renders all provided time slots', () => {
    render(
      <TimeSlotPicker 
        selectedDate={mockDate}
        availableSlots={mockAvailableSlots} 
        setSelectedTime={vi.fn()} 
      />
    );
    
    expect(screen.getByText('09:00 AM')).toBeTruthy();
    expect(screen.getByText('10:00 AM')).toBeTruthy();
    expect(screen.getByText('11:00 AM')).toBeTruthy();
  });

  it('disables buttons for booked slots', () => {
    render(
      <TimeSlotPicker 
        selectedDate={mockDate}
        availableSlots={mockAvailableSlots} 
        setSelectedTime={vi.fn()} 
      />
    );
    
    const unavailableSlot = screen.getByRole('button', { name: /10:00 AM/i });
    expect(unavailableSlot).toBeDisabled();
  });

  it('calls setSelectedTime with the correct string when clicked', () => {
    const handleSelect = vi.fn();
    render(
      <TimeSlotPicker 
        selectedDate={mockDate}
        availableSlots={mockAvailableSlots} 
        setSelectedTime={handleSelect} 
      />
    );
    
    const availableSlot = screen.getByRole('button', { name: /09:00 AM/i });
    fireEvent.click(availableSlot);

    // It passes `slot.time` (a string), not the whole object!
    expect(handleSelect).toHaveBeenCalledWith('09:00 AM');
  });
});