import { renderHook, act } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axios from 'axios';
import { useEstablishments } from './useEstablishments'; 

// 1. Mock Axios
vi.mock('axios');

describe('useEstablishments Hook', () => {
  const mockInitialData = {
    data: {
      data: [{ id: 1, name: 'Shop A' }, { id: 2, name: 'Shop B' }],
      nextCursor: 'cursor_123',
      hasMore: true
    }
  };

  const mockLoadMoreData = {
    data: {
      data: [{ id: 3, name: 'Shop C' }],
      nextCursor: null,
      hasMore: false
    }
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers(); 
  });

  afterEach(() => {
    vi.runOnlyPendingTimers(); // Clears out any stuck timers
    vi.useRealTimers(); 
  });

  it('fetches initial establishments on mount', async () => {
    axios.get.mockResolvedValue(mockInitialData);

    const { result } = renderHook(() => useEstablishments());

    // State before the 300ms timer fires
    expect(result.current.loading).toBe(true);
    expect(result.current.establishments).toEqual([]);

    // FIX: Fast-forward time AND wait for the Axios promise to finish simultaneously
    await act(async () => {
      await vi.advanceTimersByTimeAsync(300);
    });

    // No waitFor needed! State is already perfectly updated.
    expect(result.current.loading).toBe(false);
    expect(axios.get).toHaveBeenCalledWith('/api/establishments', {
      params: { search: '', limit: 6 }
    });
    expect(result.current.establishments).toHaveLength(2);
    expect(result.current.hasMore).toBe(true);
  });

  it('debounces the search query', async () => {
    axios.get.mockResolvedValue(mockInitialData);
    const { result } = renderHook(() => useEstablishments());

    // Clear initial mount fetch
    await act(async () => { 
      await vi.advanceTimersByTimeAsync(300); 
    });
    axios.get.mockClear();

    // Act: Change the search query
    act(() => {
      result.current.setSearchQuery('Coffee');
    });

    // Assert: Axios should NOT be called immediately
    expect(axios.get).not.toHaveBeenCalled();

    // Act: Fast-forward time to trigger the debounce
    await act(async () => {
      await vi.advanceTimersByTimeAsync(300);
    });

    // Assert: Axios is called after 300ms with the new search term
    expect(axios.get).toHaveBeenCalledWith('/api/establishments', {
      params: { search: 'Coffee', limit: 6 }
    });
  });

  it('handles load more correctly', async () => {
    // 1. Setup initial fetch
    axios.get.mockResolvedValueOnce(mockInitialData);
    const { result } = renderHook(() => useEstablishments());
    
    await act(async () => { 
      await vi.advanceTimersByTimeAsync(300); 
    });

    // 2. Setup mock for the "Load More" call
    axios.get.mockResolvedValueOnce(mockLoadMoreData);

    // 3. Act: Trigger load more and wait for the promise to resolve
    await act(async () => {
      await result.current.handleLoadMore();
    });

    // 4. Assert: Everything is updated instantly
    expect(result.current.loadingMore).toBe(false);
    expect(axios.get).toHaveBeenCalledWith('/api/establishments', {
      params: { search: '', limit: 6, cursor: 'cursor_123' }
    });
    
    // Should now have 3 establishments (2 initial + 1 new)
    expect(result.current.establishments).toHaveLength(3);
    expect(result.current.hasMore).toBe(false);
  });

  it('handles API errors gracefully', async () => {
    // Force the API to fail
    axios.get.mockRejectedValue(new Error('Network Error'));
    
    // Spy on console.error so our test output stays clean
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    const { result } = renderHook(() => useEstablishments());

    await act(async () => {
      await vi.advanceTimersByTimeAsync(300);
    });

    // Verify it handled the crash safely
    expect(result.current.loading).toBe(false);
    expect(result.current.establishments).toEqual([]);
    expect(consoleSpy).toHaveBeenCalledWith("Failed to fetch establishments:", expect.any(Error));

    consoleSpy.mockRestore();
  });
});