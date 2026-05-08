export function HeroSearch({ searchQuery, onSearchChange }) {
  return (
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
            {/* Search Icon */}
            <input
              type="text"
              className="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
              placeholder="Search establishments..."
              value={searchQuery}
              onChange={(e) => onSearchChange(e.target.value)}
            />
          </div>
          <button className="px-6 py-3 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700">
            Search
          </button>
        </div>
      </div>
    </section>
  );
}