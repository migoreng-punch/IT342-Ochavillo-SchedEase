import React, { useEffect } from 'react';
import { useDateOverrides } from '../dashboard/hooks/useDateOverrides';
import AddOverrideForm from '../dashboard/components/AddOverrideForm';
import OverrideItem from '../dashboard/components/OverrideItem';
import { Loader2 } from 'lucide-react';

export default function DateOverridesPage() {
  const { overrides, loading, error, fetchOverrides, addOverride, deleteOverride } = useDateOverrides();

  useEffect(() => {
    fetchOverrides();
  }, [fetchOverrides]);

  return (
    <div className="max-w-3xl mx-auto">
      
      {/* Page Header */}
      <div className="mb-8 border-b border-gray-200 pb-4">
        <h1 className="text-2xl font-bold text-gray-900">Date Overrides</h1>
      </div>

      {error && (
        <div className="bg-red-50 text-red-600 p-4 rounded-xl border border-red-100 mb-6">
          {error}
        </div>
      )}

      <div className="space-y-6">
        
        {/* Top: Add Form */}
        <AddOverrideForm onAdd={addOverride} />

        {/* Bottom: Active Overrides List */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-100 bg-gray-50/50">
            <h2 className="text-base font-bold text-gray-900">Active Overrides</h2>
          </div>

          <div className="divide-y divide-gray-100">
            {loading ? (
              <div className="flex justify-center p-8">
                <Loader2 className="w-6 h-6 text-blue-600 animate-spin" />
              </div>
            ) : overrides.length > 0 ? (
              overrides.map(override => (
                <OverrideItem 
                  key={override.id} 
                  override={override} 
                  onDelete={deleteOverride} 
                />
              ))
            ) : (
              <div className="p-8 text-center text-gray-500 text-sm">
                No active date overrides.
              </div>
            )}
          </div>
        </div>

      </div>
    </div>
  );
}