import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ShieldAlert, LogIn, UserCheck } from 'lucide-react';

export const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      const user = await login(email, password);
      if (user.role === 'CUSTOMER') navigate('/customer/claims');
      else if (user.role === 'HANDLER') navigate('/handler/queue');
      else if (user.role === 'MANAGER') navigate('/manager/dashboard');
      else navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password credentials.');
    } finally {
      setSubmitting(false);
    }
  };

  const fillDemoUser = (userEmail, userPass) => {
    setEmail(userEmail);
    setPassword(userPass);
  };

  return (
    <div className="min-h-screen bg-slate-100 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center text-blue-600">
          <ShieldAlert className="h-12 w-12" />
        </div>
        <h2 className="mt-3 text-center text-2xl font-extrabold text-slate-900">
          Insurance Claims Portal
        </h2>
        <p className="mt-1 text-center text-xs text-slate-500">
          Sign in to access your claims account
        </p>
      </div>

      <div className="mt-6 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-white py-8 px-6 shadow-xl rounded-xl border border-slate-200 sm:px-10">
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg">
              {error}
            </div>
          )}

          <form className="space-y-5" onSubmit={handleSubmit}>
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Email Address
              </label>

              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="user@example.com"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Password
              </label>
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="••••••••"
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm py-2.5 rounded-lg shadow-sm transition-colors flex items-center justify-center space-x-2"
            >
              <LogIn className="h-4 w-4" />
              <span>{submitting ? 'Signing in...' : 'Sign In'}</span>
            </button>
          </form>

          <div className="mt-6 border-t border-slate-200 pt-5">
            <p className="text-xs font-semibold text-slate-500 mb-2 flex items-center space-x-1">
              <UserCheck className="h-3.5 w-3.5 text-blue-500" />
              <span>Demo Quick Login Presets:</span>
            </p>
            <div className="grid grid-cols-3 gap-2">
              <button
                type="button"
                onClick={() => fillDemoUser('customer@example.com', 'password123')}
                className="text-[11px] font-medium bg-slate-50 hover:bg-emerald-50 text-emerald-700 border border-emerald-200 py-1.5 px-2 rounded text-center transition-colors"
              >
                Customer
              </button>
              <button
                type="button"
                onClick={() => fillDemoUser('handler@example.com', 'password123')}
                className="text-[11px] font-medium bg-slate-50 hover:bg-blue-50 text-blue-700 border border-blue-200 py-1.5 px-2 rounded text-center transition-colors"
              >
                Handler
              </button>
              <button
                type="button"
                onClick={() => fillDemoUser('manager@example.com', 'password123')}
                className="text-[11px] font-medium bg-slate-50 hover:bg-purple-50 text-purple-700 border border-purple-200 py-1.5 px-2 rounded text-center transition-colors"
              >
                Manager
              </button>
            </div>
          </div>

          <div className="mt-6 text-center text-xs text-slate-500">
            Don't have an account?{' '}
            <Link to="/register" className="font-semibold text-blue-600 hover:underline">
              Register here
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};
