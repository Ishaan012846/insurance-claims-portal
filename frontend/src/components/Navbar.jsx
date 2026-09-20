import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ShieldAlert, LogOut, User, FileText, LayoutDashboard, CheckSquare } from 'lucide-react';

export const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return null;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getRoleBadgeColor = (role) => {
    switch (role) {
      case 'MANAGER':
        return 'bg-purple-100 text-purple-800 border-purple-300';
      case 'HANDLER':
        return 'bg-blue-100 text-blue-800 border-blue-300';
      default:
        return 'bg-emerald-100 text-emerald-800 border-emerald-300';
    }
  };

  return (
    <nav className="bg-white border-b border-slate-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center space-x-8">
            <Link to="/" className="flex items-center space-x-3 text-blue-600 hover:text-blue-700">
              <ShieldAlert className="h-8 w-8 text-blue-600" />
              <span className="font-bold text-xl tracking-tight text-slate-900">
                Claims<span className="text-blue-600">Portal</span>
              </span>
            </Link>

            <div className="hidden md:flex space-x-4">
              {user.role === 'CUSTOMER' && (
                <Link
                  to="/customer/claims"
                  className="flex items-center space-x-2 text-slate-600 hover:text-blue-600 px-3 py-2 text-sm font-medium"
                >
                  <FileText className="h-4 w-4" />
                  <span>My Claims</span>
                </Link>
              )}

              {(user.role === 'HANDLER' || user.role === 'MANAGER') && (
                <Link
                  to="/handler/queue"
                  className="flex items-center space-x-2 text-slate-600 hover:text-blue-600 px-3 py-2 text-sm font-medium"
                >
                  <CheckSquare className="h-4 w-4" />
                  <span>Review Queue</span>
                </Link>
              )}

              {user.role === 'MANAGER' && (
                <Link
                  to="/manager/dashboard"
                  className="flex items-center space-x-2 text-slate-600 hover:text-blue-600 px-3 py-2 text-sm font-medium"
                >
                  <LayoutDashboard className="h-4 w-4" />
                  <span>SLA Dashboard</span>
                </Link>
              )}
            </div>
          </div>

          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-3 bg-slate-50 px-3 py-1.5 rounded-full border border-slate-200">
              <User className="h-4 w-4 text-slate-500" />
              <span className="text-sm font-medium text-slate-700">{user.email}</span>
              <span
                className={`text-xs px-2.5 py-0.5 rounded-full font-semibold border ${getRoleBadgeColor(
                  user.role
                )}`}
              >
                {user.role}
              </span>
            </div>

            <button
              onClick={handleLogout}
              className="flex items-center space-x-1.5 text-slate-500 hover:text-red-600 px-3 py-2 rounded-lg text-sm font-medium transition-colors"
              title="Sign Out"
            >
              <LogOut className="h-4 w-4" />
              <span className="hidden sm:inline">Logout</span>
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};
