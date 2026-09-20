import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    // Redirect user to their role-default dashboard if unauthorized
    if (user.role === 'CUSTOMER') return <Navigate to="/customer/claims" replace />;
    if (user.role === 'HANDLER') return <Navigate to="/handler/queue" replace />;
    if (user.role === 'MANAGER') return <Navigate to="/manager/dashboard" replace />;
  }

  return children;
};
