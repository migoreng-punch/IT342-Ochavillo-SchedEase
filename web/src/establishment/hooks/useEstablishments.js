import { useState, useEffect } from 'react';
import axios from 'axios';

export function useEstablishments() {
  const [establishments, setEstablishments] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [nextCursor, setNextCursor] = useState(null);
  const [hasMore, setHasMore] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);

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

  return {
    establishments,
    loading,
    loadingMore,
    hasMore,
    searchQuery,
    setSearchQuery,
    handleLoadMore
  };
}