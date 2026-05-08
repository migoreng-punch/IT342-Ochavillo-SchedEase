export function EstablishmentInfo({ establishment }) {
  if (!establishment) return null;
  
  return (
    <div className="bg-white rounded-2xl border border-gray-200 p-8 shadow-sm">
      <h1 className="text-3xl font-bold text-gray-900 mb-3">{establishment.name}</h1>
      <p className="text-gray-500 mb-6">{establishment.description}</p>
      {/* ... paste address and email JSX here ... */}
    </div>
  );
}