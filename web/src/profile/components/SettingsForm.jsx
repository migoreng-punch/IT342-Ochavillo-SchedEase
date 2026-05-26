import React, { useState } from 'react';
import { AlertCircle, Loader2 } from 'lucide-react';
import { PersonalInfo, ContactInfo, AccountInfo } from './FormSections';
import api from '../../api/axios'; // Adjust this path to your actual axios instance!

export default function SettingsForm({ user, onUserChange, onSave }) {
  // --- NEW: States for the Resend feature ---
  const [isResending, setIsResending] = useState(false);
  const [resendFeedback, setResendFeedback] = useState({ type: '', message: '' });

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave();
  };

  // --- NEW: Resend Logic ---
  const handleResend = async () => {
    if (!user?.email) return;

    setIsResending(true);
    setResendFeedback({ type: '', message: '' });

    try {
      // Pass the email as a query parameter to match your @RequestParam
      await api.post(`/api/auth/resend-verification?email=${encodeURIComponent(user.email)}`);
      
      setResendFeedback({ 
        type: 'success', 
        message: 'Verification email sent! Please check your inbox.' 
      });
    } catch (error) {
      // Handle the 429 Rate Limit from your backend
      if (error.response?.status === 429) {
        setResendFeedback({ 
          type: 'error', 
          message: 'Too many attempts. Please wait 1 minute.' 
        });
      } else {
        setResendFeedback({ 
          type: 'error', 
          message: error.response?.data?.message || 'Failed to send verification email.' 
        });
      }
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="flex-1 space-y-6">
      
      {/* 🚨 UPDATED: Verification Banner */}
      {!user.isVerified && (
        <div className="bg-yellow-50 border border-yellow-200 rounded-2xl p-4 flex items-start gap-3">
          <AlertCircle className="w-5 h-5 text-yellow-600 shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="text-sm text-yellow-800 font-medium">
              Your email isn't verified yet. Verify to unlock all booking features.
            </p>
            
            <button 
              type="button" // Important so it doesn't submit the main form!
              onClick={handleResend}
              disabled={isResending}
              className="flex items-center gap-1.5 text-sm text-yellow-700 font-bold hover:text-yellow-800 underline mt-1.5 disabled:opacity-60 disabled:no-underline transition-all"
            >
              {isResending && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              {isResending ? 'Sending...' : 'Resend verification email'}
            </button>

            {/* Feedback Message (Success or Error) */}
            {resendFeedback.message && (
              <p className={`text-xs mt-2 font-medium animate-in fade-in ${resendFeedback.type === 'error' ? 'text-red-600' : 'text-green-700'}`}>
                {resendFeedback.message}
              </p>
            )}
          </div>
        </div>
      )}

      {/* Main Form */}
      <form onSubmit={handleSubmit} className="space-y-6">
        
        <PersonalInfo user={user} onChange={onUserChange} />
        
        <ContactInfo user={user} onChange={onUserChange} />
        
        {/* Read-only section doesn't need an onChange */}
        <AccountInfo user={user} />

        <div className="flex justify-end pt-2">
          <button 
            type="submit"
            className="px-6 py-2.5 bg-blue-600 text-white text-sm font-semibold rounded-xl hover:bg-blue-700 shadow-sm transition-colors"
          >
            Save changes
          </button>
        </div>
      </form>
    </div>
  );
}