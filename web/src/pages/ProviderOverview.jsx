import React from 'react';
import StatusBadge from '../appointment/components/StatusBadge'; 
import { Calendar, Clock, CheckCircle, Users, Loader2 } from 'lucide-react';
import { useProviderDashboardData } from '../dashboard/hooks/useProviderDashboardData'; // 🚨 Import hook

const StatCard = ({ icon, value, label, iconBg, iconColor }) => (
  <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm flex flex-col">
    <div className={`w-10 h-10 rounded-lg flex items-center justify-center mb-4 ${iconBg} ${iconColor}`}>
      {icon}
    </div>
    <h3 className="text-2xl font-bold text-gray-900 mb-1">{value}</h3>
    <p className="text-sm text-gray-500 font-medium">{label}</p>
  </div>
);

export default function ProviderOverview() {
  // 🚨 1. Pull in the real data!
  const { stats, recentActivity, loading, error } = useProviderDashboardData();

  return (
    <div className="flex min-h-screen bg-gray-50/50 font-sans">

      <main className="flex-1 p-8">
        <div className="max-w-5xl">
          <h1 className="text-2xl font-bold text-gray-900 mb-8">Overview</h1>

          {/* Error State */}
          {error && (
            <div className="bg-red-50 text-red-600 p-4 rounded-xl border border-red-100 mb-8">
              {error}
            </div>
          )}

          {/* Loading State */}
          {loading ? (
             <div className="flex justify-center items-center h-64">
               <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
             </div>
          ) : (
            <>
              {/* Stat Cards Grid */}
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                <StatCard 
                  icon={<Calendar className="w-5 h-5" />} 
                  value={stats.total} 
                  label="Total Appointments" 
                  iconBg="bg-blue-50" iconColor="text-blue-600" 
                />
                <StatCard 
                  icon={<Clock className="w-5 h-5" />} 
                  value={stats.pending} 
                  label="Pending" 
                  iconBg="bg-yellow-50" iconColor="text-yellow-600" 
                />
                <StatCard 
                  icon={<CheckCircle className="w-5 h-5" />} 
                  value={stats.confirmed} 
                  label="Confirmed" 
                  iconBg="bg-green-50" iconColor="text-green-600" 
                />
                <StatCard 
                  icon={<Users className="w-5 h-5" />} 
                  value={stats.clients} 
                  label="Total Clients" 
                  iconBg="bg-purple-50" iconColor="text-purple-600" 
                />
              </div>

              {/* Recent Activity List */}
              <div className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden">
                <div className="p-6 border-b border-gray-100 flex justify-between items-center">
                  <h2 className="text-lg font-bold text-gray-900">Recent Activity</h2>
                </div>
                
                <div className="divide-y divide-gray-100 p-2">
                  {recentActivity.length > 0 ? (
                    recentActivity.map((activity) => (
                      <div key={activity.id} className="flex items-center justify-between p-4 hover:bg-gray-50 rounded-lg transition-colors">
                        <div>
                          <p className="font-semibold text-gray-900 mb-0.5">{activity.name}</p>
                          <p className="text-sm text-gray-500">{activity.date}</p>
                        </div>
                        <StatusBadge status={activity.status} />
                      </div>
                    ))
                  ) : (
                    <div className="p-8 text-center text-gray-500">
                      No recent activity found.
                    </div>
                  )}
                </div>
              </div>
            </>
          )}
          
        </div>
      </main>
    </div>
  );
}