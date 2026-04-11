import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';

// 🚨 You were missing this exact line!
export default function HomePage() {
  
  const [establishments, setEstablishments] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  
  // 1. New Pagination State
  const [nextCursor, setNextCursor] = useState(null);
  const [hasMore, setHasMore] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);

  // 2. Initial Fetch Data
  useEffect(() => {
    const fetchEstablishments = async () => {
      try {
        setLoading(true);
        const response = await axios.get('/api/establishments', {
          params: { search: searchQuery, limit: 6 } // No cursor on initial load
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

    const delayDebounceFn = setTimeout(() => {
      fetchEstablishments();
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [searchQuery]);

  // 3. Load More function
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

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col font-sans text-gray-900">
      {/* HEADER */}
      <header className="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2">
            <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
            <span className="text-xl font-bold tracking-tight">SchedEase</span>
          </Link>
          <nav className="hidden md:flex items-center gap-8">
            <Link to="/browse" className="text-blue-600 font-medium hover:text-blue-700">Browse</Link>
            <Link to="/appointments" className="text-gray-500 font-medium hover:text-gray-900">My Appointments</Link>
            <Link to="/login" className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium hover:bg-gray-50 transition-colors">
              Login
            </Link>
          </nav>
        </div>
      </header>

      {/* HERO SECTION */}
      <main className="flex-grow">
        <section className="bg-white py-16 sm:py-24 text-center px-4">
          <div className="max-w-3xl mx-auto">
            <h1 className="text-4xl sm:text-5xl font-extrabold tracking-tight text-gray-900 mb-4">
              Book Appointments with Trusted Establishments
            </h1>
            <p className="text-lg text-gray-500 mb-8">
              Fast, simple scheduling for businesses and clients.
            </p>
            
            <div className="max-w-xl mx-auto flex gap-2">
              <div className="relative flex-grow">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                  </svg>
                </div>
                <input
                  type="text"
                  className="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 transition-shadow"
                  placeholder="Search establishments..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
              <button className="px-6 py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 transition-colors shadow-sm whitespace-nowrap">
                Search
              </button>
            </div>
          </div>
        </section>

        {/* ESTABLISHMENTS GRID */}
        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <h2 className="text-2xl font-bold text-gray-900 mb-8">Available Establishments</h2>
          
          {loading ? (
            <div className="flex justify-center py-12">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            </div>
          ) : (
            <>
              {/* The Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {establishments.map((est) => (
                  <div key={est.id} className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col hover:shadow-md transition-shadow">
                    <h3 className="text-lg font-bold text-gray-900 mb-2">{est.name}</h3>
                    <p className="text-sm text-gray-500 mb-6 line-clamp-2 flex-grow">
                      {est.description}
                    </p>
                    
                    <div className="space-y-2 mb-6">
                      <div className="flex items-start gap-2 text-sm text-gray-500">
                        <svg className="w-4 h-4 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                        </svg>
                        <span className="truncate">{est.address}</span>
                      </div>
                      <div className="flex items-center gap-2 text-sm text-gray-500">
                        <svg className="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                        </svg>
                        <span className="truncate">{est.contactEmail}</span>
                      </div>
                    </div>

                    <Link 
                      to={`/establishment/${est.id}`}
                      className="w-full inline-flex justify-center items-center gap-2 bg-blue-600 text-white px-4 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
                    >
                      View Availability
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
                      </svg>
                    </Link>
                  </div>
                ))}
              </div>

              {/* Load More Button */}
              {hasMore && (
                <div className="mt-12 flex justify-center">
                  <button
                    onClick={handleLoadMore}
                    disabled={loadingMore}
                    className="px-6 py-3 bg-white border border-gray-300 text-gray-700 font-medium rounded-lg hover:bg-gray-50 transition-colors shadow-sm disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                  >
                    {loadingMore ? (
                      <>
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-700"></div>
                        Loading...
                      </>
                    ) : (
                      'Load More Establishments'
                    )}
                  </button>
                </div>
              )}
            </>
          )}
        </section>
      </main>

      {/* FOOTER */}
      <footer className="bg-white border-t border-gray-200 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 mb-8">
            <div className="col-span-2 md:col-span-1">
              <span className="text-lg font-bold text-gray-900 mb-4 block">SchedEase</span>
              <p className="text-sm text-gray-500">Fast, simple scheduling for businesses and clients.</p>
            </div>
            <div>
              <h4 className="font-bold text-gray-900 mb-4 text-sm">Product</h4>
              <ul className="space-y-3 text-sm text-gray-500">
                <li><Link to="#" className="hover:text-blue-600">Features</Link></li>
                <li><Link to="#" className="hover:text-blue-600">Pricing</Link></li>
                <li><Link to="#" className="hover:text-blue-600">Security</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="font-bold text-gray-900 mb-4 text-sm">Company</h4>
              <ul className="space-y-3 text-sm text-gray-500">
                <li><Link to="#" className="hover:text-blue-600">About</Link></li>
                <li><Link to="#" className="hover:text-blue-600">Blog</Link></li>
                <li><Link to="#" className="hover:text-blue-600">Careers</Link></li>
              </ul>
            </div>
            <div>
              <h4 className="font-bold text-gray-900 mb-4 text-sm">Support</h4>
              <ul className="space-y-3 text-sm text-gray-500">
                <li><Link to="#" className="hover:text-blue-600">Help Center</Link></li>
                <li><Link to="#" className="hover:text-blue-600">Contact</Link></li>
                <li><Link to="#" className="hover:text-blue-600">Privacy</Link></li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-200 pt-8 text-center">
            <p className="text-sm text-gray-400">&copy; {new Date().getFullYear()} SchedEase. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}

// Fallback data matching the exact DTO from your backend!
const mockData = [
  {
    id: 1,
    name: "Downtown Dental Clinic",
    description: "Professional dental care in the heart of the city. We offer comprehensive dental services.",
    address: "123 Main Street, Downtown",
    contactEmail: "contact@downtowndental.com"
  },
  {
    id: 2,
    name: "Wellness Spa & Massage",
    description: "Relax and rejuvenate with our premium spa treatments and therapeutic massage.",
    address: "456 Oak Avenue, Westside",
    contactEmail: "hello@wellnessspa.com"
  },
  {
    id: 3,
    name: "TechFix Computer Repair",
    description: "Expert computer and device repair services. Fast turnaround, quality guaranteed.",
    address: "789 Tech Boulevard, Silicon Valley",
    contactEmail: "support@techfix.com"
  },
  {
    id: 4,
    name: "Harbor Medical Center",
    description: "Comprehensive healthcare services with experienced medical professionals.",
    address: "321 Harbor Drive, Bayfront",
    contactEmail: "info@harbormedical.com"
  },
  {
    id: 5,
    name: "Precision Auto Service",
    description: "Full-service automotive repair and maintenance for all vehicle makes and models.",
    address: "654 Industrial Road, East End",
    contactEmail: "service@precisionauto.com"
  },
  {
    id: 6,
    name: "StyleCut Hair Salon",
    description: "Modern hair salon offering cuts, color, and styling by professional stylists.",
    address: "987 Fashion Lane, Uptown",
    contactEmail: "book@stylecut.com"
  }
];