import React from 'react';

const steps = [
  {
    number: "1",
    title: "Create Account",
    description: "Sign up as a client to book appointments or as a provider to manage your business."
  },
  {
    number: "2",
    title: "Browse & Book",
    description: "Explore trusted establishments and select available time slots that work for you."
  },
  {
    number: "3",
    title: "Manage Easily",
    description: "Track your appointments and receive notifications all from your personalized dashboard."
  }
];

export default function HowItWorksSection() {
  return (
    <section className="py-20 px-4">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <h2 className="text-3xl font-bold text-gray-900 mb-4">How SchedEase Works</h2>
          <p className="text-gray-500">Get started in three simple steps.</p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-12">
          {steps.map((step, index) => (
            <div key={index} className="flex flex-col items-center text-center">
              <div className="w-16 h-16 bg-blue-600 text-white rounded-full flex items-center justify-center text-2xl font-bold mb-6 shadow-md">
                {step.number}
              </div>
              <h3 className="text-xl font-semibold text-gray-900 mb-3">{step.title}</h3>
              <p className="text-gray-500">{step.description}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}