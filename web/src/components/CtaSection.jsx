import React from 'react';
import { Link } from 'react-router-dom';

export default function CtaSection() {
  return (
    <section className="py-20 px-4 bg-gray-50 flex flex-col items-center text-center border-t border-gray-100">
      <h2 className="text-3xl font-bold text-gray-900 mb-4">Ready to Get Started?</h2>
      <p className="text-gray-500 mb-10 max-w-2xl mx-auto">
        Join thousands of clients and businesses using SchedEase to simplify appointment management.
      </p>
      <div className="flex items-center">
        <Link to="/register" className="px-6 py-3 text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors font-medium">
          Create Free Account
        </Link>
      </div>
    </section>
  );
}