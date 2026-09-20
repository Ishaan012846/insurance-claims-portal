import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Navbar } from './components/Navbar';

import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { CustomerDashboard } from './pages/CustomerDashboard';
import { HandlerQueue } from './pages/HandlerQueue';
import { ManagerSlaDashboard } from './pages/ManagerSlaDashboard';
import { ClaimDetail } from './pages/ClaimDetail';

const HomeRedirect = () => {
  const { user, loading } = useAuth();
  if (loading) return null;
  if (!user) return <Navigate to="/login" replace />;
  if (user.role === 'CUSTOMER') return <Navigate to="/customer/claims" replace />;
  if (user.role === 'HANDLER') return <Navigate to="/handler/queue" replace />;
  if (user.role === 'MANAGER') return <Navigate to="/manager/dashboard" replace />;
  return <Navigate to="/login" replace />;
};

export default function App() {
  return (
    <Router>
      <AuthProvider>
        <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
          <Navbar />
          <main className="flex-1">
            <Routes>
              {/* Public Routes */}
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />

              {/* Role-Based Protected Routes */}
              <Route
                path="/customer/claims"
                element={
                  <ProtectedRoute allowedRoles={['CUSTOMER']}>
                    <CustomerDashboard />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/handler/queue"
                element={
                  <ProtectedRoute allowedRoles={['HANDLER', 'MANAGER']}>
                    <HandlerQueue />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/manager/dashboard"
                element={
                  <ProtectedRoute allowedRoles={['MANAGER']}>
                    <ManagerSlaDashboard />
                  </ProtectedRoute>
                }
              />

              <Route
                path="/claims/:id"
                element={
                  <ProtectedRoute allowedRoles={['CUSTOMER', 'HANDLER', 'MANAGER']}>
                    <ClaimDetail />
                  </ProtectedRoute>
                }
              />

              {/* Root & Catch-all Redirect */}
              <Route path="/" element={<HomeRedirect />} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
        </div>
      </AuthProvider>
    </Router>
  );
}
