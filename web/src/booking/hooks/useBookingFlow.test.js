import { renderHook, act, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useBookingFlow } from './useBookingFlow';
import axios from 'axios';
import { useAxiosPrivate } from '../../api/interceptor';

// 1. Mock the external libraries
vi.mock('react-router-dom', () => ({
  useNavigate: () => vi.fn()
}));
vi.mock('axios');
vi.mock('../../api/interceptor', () => ({
  useAxiosPrivate: vi.fn()
}));

// Mock your helper functions so they don't break the test
vi.mock('../utils/bookingHelpers', () => ({
  formatTimeForUI: (time) => time,
  formatDateForAPI: (date) => '2026-05-10',
  formatTimeForJava: (time) => time
}));

describe('useBookingFlow Hook', () => {
  const mockAxiosPrivate = {
    get: vi.fn().mockResolvedValue({ data: ['09:00 AM'] }),
    post: vi.fn().mockResolvedValue({})
  };

  beforeEach(() => {
    vi.clearAllMocks();
    useAxiosPrivate.mockReturnValue(mockAxiosPrivate);
    // Fake the establishment fetch response
    axios.get.mockResolvedValue({ data: { id: 1, name: 'Test Shop' } });
  });

  it('initializes with default states', async () => {
    const { result } = renderHook(() => useBookingFlow(1));
    
    // Check initial state before API calls finish
    expect(result.current.loading).toBe(true);
    expect(result.current.selectedTime).toBeNull();
    expect(result.current.selectedDate).toBeInstanceOf(Date);

    // Wait for the simulated API calls to finish
    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });
  });

  it('updates the selected date', () => {
    const { result } = renderHook(() => useBookingFlow(1));
    const testDate = new Date('2026-05-12T00:00:00Z');

    act(() => {
      // Your component uses setSelectedDate, not handleDateSelect!
      result.current.setSelectedDate(testDate);
    });

    expect(result.current.selectedDate).toBe(testDate);
  });
});