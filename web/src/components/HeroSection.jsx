import React from 'react';
import { Link } from 'react-router-dom'

export default function HeroSection() {
  return (
    <section className="flex flex-col items-center text-center pt-24 pb-16 px-4 max-w-4xl mx-auto">
      <h1 className="text-5xl font-extrabold text-gray-900 leading-tight mb-6">
        Simplify Your Appointment<br />Booking
      </h1>
      <p className="text-lg text-gray-500 mb-10 max-w-2xl">
        SchedEase connects clients with trusted establishments. Book appointments instantly, 
        manage your schedule, and never miss a reservation again.
      </p>
    </section>
  );
}