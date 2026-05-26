import { useState, useCallback } from 'react';
import { useAxiosPrivate } from '../../api/interceptor';

export function useSettings() {
  const axiosPrivate = useAxiosPrivate();
  
  const [formData, setFormData] = useState({
    // Establishment Data
    establishmentName: '',
    description: '',
    address: '',
    contactEmail: '',
    contactPhone: '',
    
    // 🚨 New Scheduling Fields
    slotDurationMinutes: 0, // Sensible default
    bufferMinutes: 0,        
    bookingCutoffHours: 0,  

    // User Data
    username: '',
    firstName: '',
    lastName: ''
  });

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  // --- 1. FETCH DATA ---
  const fetchSettings = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      
      const [estabResponse, userResponse] = await Promise.all([
        axiosPrivate.get('/api/establishments/me'),
        axiosPrivate.get('/api/users/me')
      ]);
      
      const estabData = estabResponse.data;
      const userData = userResponse.data;
      
      setFormData({
        establishmentName: estabData.name || '',
        description: estabData.description || '',
        address: estabData.address || '',
        contactEmail: estabData.contactEmail || '',
        contactPhone: estabData.phone || '',
        
        // 🚨 Map the new fields from the backend
        slotDurationMinutes: estabData.slotDurationMinutes || 0,
        bufferMinutes: estabData.bufferMinutes || 0,
        bookingCutoffHours: estabData.bookingCutoffHours || 0,

        username: userData.username || '',
        firstName: userData.firstName || '',
        lastName: userData.lastName || ''
      });

      console.log(formData);
    } catch (err) {
      console.error("Failed to load settings:", err);
      setError("Could not load settings data.");
    } finally {
      setLoading(false);
    }
  }, [axiosPrivate]);

  // --- 2. HANDLE INPUT CHANGES ---
  const handleChange = (field, value) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  // --- 3. SAVE DATA ---
  const saveSettings = async () => {
    try {
      setSaving(true);
      setError(null);

      const establishmentPayload = {
        name: formData.establishmentName,
        description: formData.description,
        address: formData.address,
        // 🚨 CRITICAL FIX: The key must be 'contactEmail' to match your Java DTO!
        contactEmail: formData.contactEmail, 
        // Note: If your Java DTO expects 'phone', keep this. If it expects 'contactPhone', change this too!
        phone: formData.contactPhone, 
        
        slotDurationMinutes: formData.slotDurationMinutes,
        bufferMinutes: formData.bufferMinutes,
        bookingCutoffHours: formData.bookingCutoffHours
      };

      const userPayload = {
        username: formData.username,
        firstName: formData.firstName,
        lastName: formData.lastName
      };

      // Fire both PUT requests simultaneously
      await Promise.all([
        axiosPrivate.put('/api/establishments/me', establishmentPayload),
        axiosPrivate.put('/api/users/me', userPayload)
      ]);

      alert("Settings updated successfully!");
    } catch (err) {
      console.error("Failed to save settings:", err);
      
      // 🚨 CRITICAL FIX: Robust error extractor to prevent the React white-screen crash
      const errorData = err.response?.data;
      let errorMessage = "Failed to save changes. Please try again.";

      if (errorData) {
        if (typeof errorData === 'string') {
          errorMessage = errorData;
        } else if (errorData.message) {
          errorMessage = errorData.message;
        } else if (errorData.error) {
          errorMessage = typeof errorData.error === 'string' ? errorData.error : errorData.error.message;
        }
      }

      setError(errorMessage);
    } finally {
      setSaving(false);
    }
  };

  // --- 4. DELETE ESTABLISHMENT ---
const deleteEstablishment = async () => {
    // 1. Updated confirmation text to be specific to the establishment
    const isConfirmed = window.confirm("Are you absolutely sure? This will permanently delete your establishment, schedule, and all appointments.");
    if (!isConfirmed) return;

    try {
      setSaving(true);
      
      // 🚨 2. The new Spring Boot endpoint
      await axiosPrivate.delete('/api/establishments/me');
      
      // 3. Redirect back to the provider dashboard (their account is still active!)
      window.location.href = '/provider'; 
      
    } catch (err) {
      console.error("Failed to delete establishment:", err);
      setError("Failed to delete establishment. Please try again.");
      setSaving(false);
    }
  };

  return { formData, loading, saving, error, fetchSettings, handleChange, saveSettings, deleteEstablishment };
}