import React, { useState } from 'react';
import { Eye, EyeOff, Loader2 } from 'lucide-react';

// 🚨 Properly accepting onChangePassword and isChanging as props
export default function ChangePasswordCard({ onChangePassword, isChanging }) {
  
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  
  const [showCurrent, setShowCurrent] = useState(false);
  const [showNew, setShowNew] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const handleSubmit = async () => {
    // Basic frontend validation
    if (!currentPassword || !newPassword || !confirmPassword) {
      setFeedback({ type: "error", message: "All fields are required." });
      return;
    }

    // Use the function passed from the parent
    const response = await onChangePassword({
      currentPassword,
      newPassword,
      confirmPassword
    });

    if (response.success) {
      setFeedback({ type: "success", message: response.message });
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } else {
      setFeedback({ type: "error", message: response.message });
    }
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
      <div className="space-y-5">
        
        {/* Success/Error messages */}
        {feedback.message && (
          <div className={`p-3 rounded-lg text-sm ${feedback.type === "error" ? "bg-red-50 text-red-600" : "bg-green-50 text-green-600"}`}>
            {feedback.message}
          </div>
        )}

        {/* Current Password */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Current password</label>
          <div className="relative">
            <input 
              type={showCurrent ? "text" : "password"} 
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              placeholder="........"
              className="w-full p-2.5 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none pr-10"
            />
            <button 
              type="button"
              onClick={() => setShowCurrent(!showCurrent)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              {showCurrent ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
            </button>
          </div>
        </div>

        {/* New Password */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">New password</label>
          <div className="relative">
            <input 
              type={showNew ? "text" : "password"} 
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="........"
              className="w-full p-2.5 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none pr-10"
            />
            <button 
              type="button"
              onClick={() => setShowNew(!showNew)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              {showNew ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
            </button>
          </div>
        </div>

        {/* Confirm New Password */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Confirm new password</label>
          <div className="relative">
            <input 
              type={showConfirm ? "text" : "password"} 
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="........"
              className="w-full p-2.5 border border-gray-300 rounded-lg text-sm focus:ring-blue-500 focus:border-blue-500 outline-none pr-10"
            />
            <button 
              type="button"
              onClick={() => setShowConfirm(!showConfirm)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              {showConfirm ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
            </button>
          </div>
        </div>

        {/* Action Button */}
        <div className="flex justify-end pt-2">
          <button 
            onClick={handleSubmit}
            disabled={isChanging} // 🚨 FIXED: using isChanging
            className="flex items-center gap-2 bg-blue-600 text-white px-5 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors disabled:opacity-70"
          >
            {isChanging && <Loader2 className="w-4 h-4 animate-spin" />} {/* 🚨 FIXED */}
            {isChanging ? "Updating..." : "Update password"} {/* 🚨 FIXED */}
          </button>
        </div>
      </div>
    </div>
  );
}