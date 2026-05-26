import { useState, useEffect, useCallback } from 'react';
import { useAxiosPrivate } from '../../api/interceptor'; // Adjust path as needed

export function useProfile() {
  const axiosPrivate = useAxiosPrivate();
  
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  
  // 🚨 NEW: Dedicated loading state just for password changes
  const [changingPassword, setChangingPassword] = useState(false); 

  // --- 1. Fetch Profile on Load ---
  const fetchProfile = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await axiosPrivate.get('/api/users/me');
      
      setUser({
        firstName: response.data.firstName || '',
        lastName: response.data.lastName || '',
        username: response.data.username || '',
        role: response.data.role || 'CLIENT',
        email: response.data.email || '', 
        phone: response.data.phoneNumber || '', 
        location: response.data.address || '',
        isVerified: response.data.enabled || '', 
        memberSince: response.data.createdAt 
          ? new Date(response.data.createdAt).toLocaleDateString('en-US', { 
              month: 'long', 
              year: 'numeric' 
            })
          : 'Unknown'
      });
      
    } catch (err) {
      console.error("Failed to load profile:", err);
      setError("Could not load profile data.");
    } finally {
      setLoading(false);
    }
  }, [axiosPrivate]);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  // --- 2. Update Profile ---
  const updateProfile = async (updatedData) => {
    try {
      setSaving(true);
      setError(null);

      const response = await axiosPrivate.put('/api/users/me', {
        firstName: updatedData.firstName,
        lastName: updatedData.lastName,
        username: updatedData.username
      });

      setUser(prev => ({
        ...prev,
        ...response.data
      }));

      return true;
    } catch (err) {
      const backendMessage = err.response?.data?.message || "Failed to update profile.";
      setError(backendMessage);
      return false;
    } finally {
      setSaving(false);
    }
  };

  // --- 3. 🚨 NEW: Change Password ---
  const changePassword = async (passwordData) => {
    try {
      setChangingPassword(true);
      
      // Hit the new Spring Boot endpoint
      await axiosPrivate.put('/api/users/me/password', {
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
        confirmPassword: passwordData.confirmPassword
      });

      // Return a success object so the UI can clear the form and show a success message
      return { success: true, message: "Password updated successfully!" };
      
    } catch (err) {
      // Capture the exact error from Spring Boot (e.g., "Current password is incorrect")
      const backendMessage = err.response?.data?.message || "Failed to update password.";
      return { success: false, message: backendMessage };
      
    } finally {
      setChangingPassword(false);
    }
  };

  const deleteAccount = async () => {
    try {
      // Adjust the URL if your controller has a different base path!
      await axiosPrivate.delete('/api/users/me'); 
      return { success: true };
    } catch (err) {
      const backendMessage = err.response?.data || "Failed to delete account.";
      return { success: false, message: backendMessage };
    }
  };

  return {
    user,
    setUser,
    loading,
    saving,
    error,
    changingPassword, // 🚨 Expose the new state
    updateProfile,
    changePassword,
    deleteAccount    // 🚨 Expose the new function
  };
}