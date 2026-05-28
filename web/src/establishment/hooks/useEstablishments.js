import { useState, useEffect } from 'react';
import axios from 'axios';
import { useAxiosPrivate } from '../../api/interceptor'; // 🚨 1. Import your private axios for authenticated requests

export function useEstablishments() {
  const axiosPrivate = useAxiosPrivate(); // 🚨 2. Initialize it
  
  const [establishments, setEstablishments] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [nextCursor, setNextCursor] = useState(null);
  const [hasMore, setHasMore] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  
  // 🚨 3. Add an error state so your component's alert box works
  const [error, setError] = useState(null); 

  // --- Fetching Logic (Unchanged) ---
  useEffect(() => {
    const fetchEstablishments = async () => {
      try {
        setLoading(true);
        const response = await axios.get('/api/establishments', {
          params: { search: searchQuery, limit: 6 }
        });
        
        const fetchedData = response.data.data;
        setEstablishments(Array.isArray(fetchedData) ? fetchedData : []);
        setNextCursor(response.data.nextCursor);
        setHasMore(response.data.hasMore);
      } catch (error) {
        console.error("Failed to fetch establishments:", error);
      } finally {
        setLoading(false);
      }
    };

    const delayDebounceFn = setTimeout(() => fetchEstablishments(), 300);
    return () => clearTimeout(delayDebounceFn);
  }, [searchQuery]);

  const handleLoadMore = async () => {
    if (!hasMore || loadingMore) return;
    try {
      setLoadingMore(true);
      const response = await axios.get('/api/establishments', {
        params: { search: searchQuery, limit: 6, cursor: nextCursor }
      });
      const newData = response.data.data;
      if (Array.isArray(newData)) {
        setEstablishments(prev => [...prev, ...newData]);
      }
      setNextCursor(response.data.nextCursor);
      setHasMore(response.data.hasMore);
    } catch (error) {
      console.error("Failed to load more:", error);
    } finally {
      setLoadingMore(false);
    }
  };

  // 🚨 4. ADD THE MISSING CREATE FUNCTION
  const createEstablishment = async (payload) => {
    try {
      setLoading(true);
      setError(null);
      
      // Use axiosPrivate so the backend knows which Provider is creating this
      await axiosPrivate.post('/api/establishments', payload);
      
      return true; // Success!
    } catch (err) {
      // Catch backend errors (e.g. "Name already taken") and pass them to the UI
      const backendMessage = err.response?.data?.message || "Failed to setup establishment. Please try again.";
      setError(backendMessage);
      return false; // Failed
    } finally {
      setLoading(false);
    }
  };

  return {
    establishments,
    loading,
    loadingMore,
    hasMore,
    searchQuery,
    setSearchQuery,
    handleLoadMore,
    error, // 🚨 5. Export the error
    createEstablishment // 🚨 6. Export the new function
  };
}