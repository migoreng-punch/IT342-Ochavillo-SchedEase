import React from 'react';

export default function EstablishmentInfoCard({ data, onChange }) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <h2 className="text-base font-bold text-gray-900 mb-5">Establishment Information</h2>
      
      <div className="space-y-5">
        
        {/* Basic Info */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Establishment Name</label>
          <input 
            type="text" 
            value={data.establishmentName}
            onChange={(e) => onChange('establishmentName', e.target.value)}
            className="w-full p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea 
            rows="3"
            value={data.description}
            onChange={(e) => onChange('description', e.target.value)}
            className="w-full p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none resize-none"
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Address</label>
          <input 
            type="text" 
            value={data.address}
            onChange={(e) => onChange('address', e.target.value)}
            className="w-full p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none"
          />
        </div>

        {/* Contact Info (2 columns) */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input 
              type="email" 
              value={data.contactEmail}
              onChange={(e) => onChange('contactEmail', e.target.value)}
              className="w-full p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
            <input 
              type="tel" 
              value={data.contactPhone}
              onChange={(e) => onChange('contactPhone', e.target.value)}
              className="w-full p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none"
            />
          </div>
        </div>

        {/* Divider */}
        <hr className="border-gray-100" />

        {/* Scheduling Settings (3 columns) */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1" title="How long each appointment lasts">
              Slot Duration (mins)
            </label>
            <input 
              type="number" 
              min="1"
              value={data.slotDurationMinutes}
              // We use Number() to ensure it passes an integer to your state instead of a string
              onChange={(e) => onChange('slotDurationMinutes', Number(e.target.value))}
              className="w-full p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1" title="How much rest time between appointments">
              Buffer (mins)
            </label>
            <input 
              type="number" 
              min="0"
              value={data.bufferMinutes}
              onChange={(e) => onChange('bufferMinutes', Number(e.target.value))}
              className="w-full p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1" title="Minimum hours required before a user can book">
              Booking Cut-off (hours)
            </label>
            <input 
              type="number" 
              min="0"
              value={data.bookingCutoffHours}
              onChange={(e) => onChange('bookingCutoffHours', Number(e.target.value))}
              className="w-full p-2 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none"
            />
          </div>
        </div>

      </div>
    </div>
  );
}