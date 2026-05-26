import React from 'react';
import { useEstablishments } from '../establishment/hooks/useEstablishments';
import { HeroSearch } from '../establishment/components/HeroSearch';
import { EstablishmentCard } from '../establishment/components/EstablishmentCard';

export default function HomePage() {
  const {
    establishments,
    loading,
    loadingMore,
    hasMore,
    searchQuery,
    setSearchQuery,
    handleLoadMore
  } = useEstablishments();

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col font-sans text-gray-900">
      <main className="flex-grow">
        <HeroSearch 
          searchQuery={searchQuery} 
          onSearchChange={setSearchQuery} 
        />

        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <h2 className="text-2xl font-bold text-gray-900 mb-8">Available Establishments</h2>
          
          {loading ? (
            <div className="flex justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {establishments.map((est) => (
                  <EstablishmentCard key={est.id} establishment={est} />
                ))}
              </div>

              {hasMore && (
                <div className="mt-12 flex justify-center">
                  <button
                    onClick={handleLoadMore}
                    disabled={loadingMore}
                    className="px-6 py-3 bg-white border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 disabled:opacity-50 flex items-center gap-2"
                  >
                    {loadingMore ? 'Loading...' : 'Load More Establishments'}
                  </button>
                </div>
              )}
            </>
          )}
        </section>
      </main>
    </div>
  );
}