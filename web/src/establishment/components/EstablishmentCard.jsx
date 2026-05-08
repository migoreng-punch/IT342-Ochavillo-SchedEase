import { Link } from 'react-router-dom';

export function EstablishmentCard({ establishment }) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-6 flex flex-col hover:shadow-md transition-shadow">
      <h3 className="text-lg font-bold text-gray-900 mb-2">{establishment.name}</h3>
      <p className="text-sm text-gray-500 mb-6 line-clamp-2 flex-grow">
        {establishment.description}
      </p>
      
      {/* ... (Keep your contact/address HTML here) ... */}

      <Link 
        to={`/establishment/${establishment.id}`}
        className="w-full inline-flex justify-center items-center gap-2 bg-blue-600 text-white px-4 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
      >
        View Availability
      </Link>
    </div>
  );
}