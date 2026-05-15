import React from 'react';
import { Calendar, Clock, Bell, CheckCircle } from 'lucide-react';

const features = [
  {
    icon: <Calendar className="w-6 h-6 text-blue-600" />,
    title: "Easy Booking",
    description: "Browse available time slots and book appointments with just a few clicks."
  },
  {
    icon: <Clock className="w-6 h-6 text-blue-600" />,
    title: "Smart Scheduling",
    description: "Set your weekly hours and date-specific overrides to manage your availability."
  },
  {
    icon: <Bell className="w-6 h-6 text-blue-600" />,
    title: "Notifications",
    description: "Receive instant updates about bookings, cancellations, and schedule changes."
  },
  {
    icon: <CheckCircle className="w-6 h-6 text-blue-600" />,
    title: "Real-time Updates",
    description: "Track appointment status and manage confirmations in real-time from your dashboard."
  }
];

export default function FeaturesSection() {
  return (
    <section className="py-20 px-4 bg-gray-50">
      <div className="max-w-6xl mx-auto">   
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {features.map((feature, index) => (
            <div key={index} className="bg-white p-8 rounded-xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
              <div className="w-12 h-12 bg-blue-50 rounded-lg flex items-center justify-center mb-6">
                {feature.icon}
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-3">{feature.title}</h3>
              <p className="text-gray-500 leading-relaxed text-sm">{feature.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}