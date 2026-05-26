import React, { useEffect } from 'react';
import { Save, Loader2 } from 'lucide-react';
import { useWeeklySchedule } from '../dashboard/hooks/useWeeklySchedule';
import ScheduleDayRow from '../dashboard/components/ScheduleDayRow';
// 🚨 1. Import your AuthContext
import { useAuth } from '../auth/context/AuthContext'; // Adjust path if necessary

export default function ProviderSchedulePage() {
  const { schedule, loading, saving, error, fetchSchedule, updateDay, saveSchedule } = useWeeklySchedule();
  
  // 🚨 2. Pull the user from context
  const { user } = useAuth();
  
  // 🚨 3. Check verification status
  const isUnverified = user && !user.isEmailVerified;

  useEffect(() => {
    fetchSchedule();
  }, [fetchSchedule]);

  return (
    <div className="max-w-4xl mx-auto">
      
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-3xl font-extrabold text-gray-900">Weekly Schedule</h1>
        
        {/* 🚨 4. The Soft-Blocked Button Container */}
        <div className="relative group">
          <button 
            onClick={saveSchedule}
            // Add isUnverified to the disabled conditions
            disabled={saving || loading || isUnverified}
            className={`flex items-center gap-2 px-5 py-2.5 rounded-lg font-medium transition-colors shadow-sm ${
              isUnverified
                ? "bg-gray-100 text-gray-400 border border-gray-200 cursor-not-allowed" // Grayed out
                : "bg-blue-600 hover:bg-blue-700 text-white disabled:opacity-70" // Active
            }`}
          >
            {saving ? <Loader2 className="w-5 h-5 animate-spin" /> : <Save className="w-5 h-5" />}
            {saving ? 'Saving...' : 'Save Changes'}
          </button>

          {/* 🚨 5. The Yellow Tooltip */}
          {isUnverified && (
            <div className="absolute top-full right-0 mt-2 w-64 p-2.5 bg-yellow-50 border border-yellow-200 text-yellow-800 text-xs font-medium text-center rounded-lg shadow-md opacity-0 group-hover:opacity-100 transition-opacity duration-200 pointer-events-none z-50">
              You need to verify your email address to save changes to your schedule.
            </div>
          )}
        </div>
      </div>

      {error && (
        <div className="bg-red-50 text-red-600 p-4 rounded-xl border border-red-100 mb-6">
          {error}
        </div>
      )}

      {/* Main Content */}
      {loading ? (
        <div className="flex justify-center items-center h-64">
          <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
        </div>
      ) : (
        <div className="space-y-4">
          
          {/* Day Rows */}
          {schedule.map((dayData) => (
            <ScheduleDayRow 
              key={dayData.dayOfWeek} 
              dayData={dayData} 
              onChange={updateDay} 
            />
          ))}

          {/* Footer Info Note */}
          <div className="mt-8 bg-blue-50 border border-blue-100 p-4 rounded-xl flex items-start gap-2">
            <span className="font-bold text-blue-800 text-sm">Note:</span>
            <p className="text-blue-800 text-sm">
              This schedule defines your regular weekly availability. Use Date Overrides to make exceptions for specific dates.
            </p>
          </div>

        </div>
      )}
    </div>
  );
}