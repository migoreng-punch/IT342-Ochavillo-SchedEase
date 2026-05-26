import { BrowserRouter, Routes, Route, Outlet } from "react-router-dom";
import { AuthProvider } from "./auth/context/AuthContext";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ProtectedRoute from "./routes/ProtectedRoute";
import AppLayout from "./layouts/AppLayout";
import ProviderLayout from "./layouts/ProviderLayout";
import HomePage from "./pages/HomePage";
import BookingPage from "./pages/BookingPage";
import LandingPage from "./pages/LandingPage";
import ClientDashboard from "./pages/ClientDashboard";
import VerifyAccount from "./pages/VerifyAccount";
import MyAppointmentsPage from "./pages/MyAppointmentsPage";
import ProviderOverview from "./pages/ProviderOverview";
import ProviderAppointmentsPage from "./pages/ProviderAppointmentPage";
import ProviderSchedulePage from "./pages/ProviderSchedulePage";
import DateOverridesPage from "./pages/DateOverridesPage";
import ProviderSettingsPage from "./pages/ProviderSettingsPage";

import ProviderRoute from "./routes/ProviderRoute";
import AccountSettings from "./pages/AccountSettings";
import SetupEstablishment from "./pages/SetupEstablishment";

// 🚨 Import your new Bouncer wrappers (Adjust paths if you saved them elsewhere!)
import { RequireNoEstablishment } from "./routes/RequireNoEstablishment";
import SecuritySettingsPage from "./pages/SecuritySettingsPage";

// ClientLayout works perfectly because ProtectedRoute uses {children}
const ClientLayout = () => {
  return (
    <ProtectedRoute>
      <AppLayout>
        <Outlet />
      </AppLayout>
    </ProtectedRoute>
  );
};

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* --- PUBLIC ROUTES --- */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/verify" element={<VerifyAccount />} />

          {/* --- PROTECTED CLIENT ROUTES --- */}
          <Route element={<ClientLayout />}>
            <Route path="/dashboard" element={<ClientDashboard />} />
            <Route path="/homepage" element={<HomePage />} />
            <Route path="/establishment/:id" element={<BookingPage />} />
            <Route path="/myappointments" element={<MyAppointmentsPage />} />
            <Route path="/account/settings/profile" element={<AccountSettings />} />
            <Route path="/account/settings/security" element={<SecuritySettingsPage />} />

            {/* 🚨 UPDATED: The heavily guarded Setup route */}
            <Route
              path="/setup-establishment"
              element={
                <RequireNoEstablishment>
                  <SetupEstablishment />
                </RequireNoEstablishment>
              }
            />
          </Route>

          {/* --- PROTECTED PROVIDER ROUTES --- */}
          {/* 1. The Security Guard (Uses Outlet) */}
          <Route element={<ProviderRoute />}>
            {/* 2. The UI Layout (Uses Outlet) */}
            <Route element={<ProviderLayout />}>
              {/* 3. The Specific Pages */}
              <Route path="/provider" element={<ProviderOverview />} />
              <Route
                path="/provider/appointments"
                element={<ProviderAppointmentsPage />}
              />
              <Route
                path="/provider/schedule"
                element={<ProviderSchedulePage />}
              />
              <Route
                path="/provider/overrides"
                element={<DateOverridesPage />}
              />
              <Route
                path="/provider/settings"
                element={<ProviderSettingsPage />}
              />
            </Route>
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
