import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { CheckCircle, XCircle, Loader2, Mail } from 'lucide-react';
import api from '../api/axios'; // Adjust path to your base axios instance

export default function VerifyAccount() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [status, setStatus] = useState('loading'); 
  const [errorMessage, setErrorMessage] = useState('');

  // --- NEW: State for the auto-fetched email ---
  const [autoEmail, setAutoEmail] = useState('');
  
  const [isResending, setIsResending] = useState(false);
  const [resendFeedback, setResendFeedback] = useState({ type: '', message: '' });

  useEffect(() => {
    if (!token) {
      setStatus('error');
      setErrorMessage('No verification token provided.');
      return;
    }

    const verifyToken = async () => {
      try {
        await api.get(`/api/auth/verify?token=${token}`);
        setStatus('success');
      } catch (error) {
        setStatus('error');
        setErrorMessage(error.response?.data?.message || 'Verification failed. The link may be invalid or expired.');
        
        // 🚨 Catch the email sent back by your Spring Boot backend!
        if (error.response?.data?.email) {
          setAutoEmail(error.response.data.email);
        }
      }
    };

    verifyToken();
  }, [token]);

  // --- NEW: Resend Function using the auto-fetched email ---
  const handleResend = async () => {
    if (!autoEmail) return;
    
    setIsResending(true);
    setResendFeedback({ type: '', message: '' });

    try {
      // Pass the autoEmail directly to your endpoint
      await api.post(`/api/auth/resend-verification?email=${encodeURIComponent(autoEmail)}`);
      setResendFeedback({ type: 'success', message: 'Verification email sent! Please check your inbox.' });
    } catch (error) {
      // Catches your 429 Too Many Requests and generic errors
      setResendFeedback({ 
        type: 'error', 
        message: error.response?.data?.message || 'Failed to resend. Please try again.' 
      });
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4 font-sans">
      <div className="max-w-md w-full bg-white p-8 rounded-xl shadow-sm border border-gray-100 text-center transition-all">
        
        {/* State 1: Loading */}
        {status === 'loading' && (
          <div className="flex flex-col items-center">
            <Loader2 className="w-12 h-12 text-blue-600 animate-spin mb-4" />
            <h2 className="text-xl font-bold text-gray-900 mb-2">Verifying your account...</h2>
            <p className="text-gray-500">Please wait while we confirm your email address.</p>
          </div>
        )}

        {/* State 2: Success */}
        {status === 'success' && (
          <div className="flex flex-col items-center">
            <CheckCircle className="w-16 h-16 text-green-500 mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Account Verified!</h2>
            <p className="text-gray-500 mb-8">
              Thank you for verifying your email. Your SchedEase account is now fully active.
            </p>
            <Link 
              to="/login" 
              className="w-full bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700 transition font-medium inline-block"
            >
              Continue to Login
            </Link>
          </div>
        )}

        {/* State 3: Error / Expired */}
        {status === 'error' && (
          <div className="flex flex-col items-center">
            <XCircle className="w-16 h-16 text-red-500 mb-4" />
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Verification Failed</h2>
            <p className="text-gray-500 mb-6">
              {errorMessage}
            </p>
            
            <div className="w-full space-y-3">
              <Link 
                to="/login" 
                className="w-full bg-gray-900 text-white py-3 rounded-lg hover:bg-gray-800 transition font-medium inline-block"
              >
                Go to Login
              </Link>
              
              {/* --- NEW: One-Click Resend UI --- */}
              {/* Only show the resend button if we successfully caught the email from the backend */}
              {autoEmail && (
                <div className="mt-2 space-y-3 animate-in fade-in zoom-in duration-200">
                  <button 
                    onClick={handleResend}
                    disabled={isResending}
                    className="w-full flex items-center justify-center gap-2 bg-white text-gray-700 border border-gray-300 py-3 rounded-lg hover:bg-gray-50 transition font-medium disabled:opacity-60"
                  >
                    {isResending ? <Loader2 className="w-4 h-4 animate-spin" /> : <Mail className="w-4 h-4 text-gray-500" />}
                    {isResending ? 'Sending...' : 'Resend Verification Link'}
                  </button>

                  {/* Feedback Message (Success or Error) */}
                  {resendFeedback.message && (
                    <div className={`p-3 rounded-lg text-sm text-left ${resendFeedback.type === 'error' ? 'bg-red-50 text-red-600 border border-red-100' : 'bg-green-50 text-green-700 border border-green-100'}`}>
                      {resendFeedback.message}
                    </div>
                  )}
                </div>
              )}
            </div>

          </div>
        )}

      </div>
    </div>
  );
}