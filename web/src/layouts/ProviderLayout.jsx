import React from 'react';
import { Outlet } from 'react-router-dom';
import ProviderSidebar from './ProviderSidebar';

export default function ProviderLayout() {
  return (
    <div className="flex min-h-screen bg-gray-50/50 font-sans">
      <ProviderSidebar />
      <main className="flex-1 p-8">
        {/* The specific page (Overview, Appointments, etc.) renders here! */}
        <Outlet />
      </main>
    </div>
  );
}