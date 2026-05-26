import React from 'react';
import { Mail, Phone, MapPin, Globe } from 'lucide-react';

export function PersonalInfo({ user, onChange }) {
  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm">
      <div className="mb-6">
        <h2 className="text-lg font-bold text-gray-900">Personal Information</h2>
        <p className="text-sm text-gray-500">Update your name and contact details</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 mb-5">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">First name</label>
          <input 
            type="text" 
            value={user.firstName}
            onChange={(e) => onChange('firstName', e.target.value)}
            className="w-full px-4 py-2.5 bg-white border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">Last name</label>
          <input 
            type="text" 
            value={user.lastName}
            onChange={(e) => onChange('lastName', e.target.value)}
            className="w-full px-4 py-2.5 bg-white border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
          />
        </div>
      </div>

      <div className="mb-5">
        <label className="block text-sm font-medium text-gray-700 mb-1.5">Username</label>
        <div className="relative">
          <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-gray-400 text-sm">@</div>
          <input 
            type="text" 
            value={user.username}
            onChange={(e) => onChange('username', e.target.value)}
            className="w-full pl-9 pr-4 py-2.5 bg-white border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
          />
        </div>
        <p className="text-xs text-gray-500 mt-1.5">Your unique handle on SchedEase</p>
      </div>
    </div>
  );
}

export function ContactInfo({ user, onChange }) {
  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm">
      <div className="mb-6">
        <h2 className="text-lg font-bold text-gray-900">Contact Details</h2>
        <p className="text-sm text-gray-500">How we and establishments can reach you</p>
      </div>

      <div className="space-y-5">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">Email address</label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
              <Mail className="w-4 h-4 text-gray-400" />
            </div>
            <input 
              type="email" 
              value={user.email}
              onChange={(e) => onChange('email', e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 bg-white border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1.5">Phone number</label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
              <Phone className="w-4 h-4 text-gray-400" />
            </div>
            <input 
              type="tel" 
              value={user.phone}
              onChange={(e) => onChange('phone', e.target.value)}
              className="w-full pl-10 pr-4 py-2.5 bg-white border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
            />
          </div>
        </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Location</label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none">
                <MapPin className="w-4 h-4 text-gray-400" />
              </div>
              <input 
                type="text" 
                value={user.location}
                onChange={(e) => onChange('location', e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 bg-white border border-gray-300 rounded-xl text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
              />
            </div>
          </div>
      </div>
    </div>
  );
}

export function AccountInfo({ user }) {
  return (
    <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm">
      <div className="mb-6">
        <h2 className="text-lg font-bold text-gray-900">Account</h2>
        <p className="text-sm text-gray-500">Your role and membership details</p>
      </div>

      <div className="space-y-4 divide-y divide-gray-100">
        <div className="flex justify-between items-center pb-4">
          <div>
            <p className="text-sm font-medium text-gray-900">Account type</p>
            <p className="text-xs text-gray-500 mt-0.5">Your current membership plan</p>
          </div>
          <span className="px-3 py-1 bg-gray-100 text-gray-700 text-xs font-semibold rounded-lg">
            {user.role}
          </span>
        </div>
        <div className="flex justify-between items-center pt-4">
          <div>
            <p className="text-sm font-medium text-gray-900">Member since</p>
            <p className="text-xs text-gray-500 mt-0.5">Account creation date</p>
          </div>
          <span className="text-sm font-medium text-gray-600">
            {user.memberSince}
          </span>
        </div>
      </div>
    </div>
  );
}