import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { StatusBadge } from '../components/StatusBadge';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import {
  AlertTriangle,
  Clock,
  CheckCircle2,
  Users,
  UserCheck,
  RefreshCw,
  X,
  ChevronRight,
} from 'lucide-react';

const STATUS_COLORS = {
  DRAFT: '#94a3b8',
  SUBMITTED: '#3b82f6',
  UNDER_REVIEW: '#f59e0b',
  INFO_REQUESTED: '#a855f7',
  APPROVED: '#10b981',
  REJECTED: '#ef4444',
  SETTLED: '#14b8a6',
};

export const ManagerSlaDashboard = () => {
  const [metrics, setMetrics] = useState(null);
  const [handlers, setHandlers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Reassign modal state
  const [selectedClaim, setSelectedClaim] = useState(null);
  const [selectedHandlerId, setSelectedHandlerId] = useState('');
  const [reassigning, setReassigning] = useState(false);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError('');
    try {
      const [slaRes, handlersRes] = await Promise.all([
        api.get('/dashboard/sla'),
        api.get('/users/handlers'),
      ]);
      setMetrics(slaRes.data);
      setHandlers(handlersRes.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch SLA metrics.');
    } finally {
      setLoading(false);
    }
  };

  const handleReassign = async (e) => {
    e.preventDefault();
    if (!selectedClaim || !selectedHandlerId) return;

    setReassigning(true);
    try {
      await api.patch(`/claims/${selectedClaim.id}/reassign`, {
        handlerId: Number(selectedHandlerId),
      });

      setSelectedClaim(null);
      setSelectedHandlerId('');
      fetchDashboardData();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to reassign handler.');
    } finally {
      setReassigning(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error || !metrics) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="p-4 bg-red-50 text-red-700 rounded-xl text-xs flex items-center space-x-2">
          <AlertTriangle className="h-4 w-4" />
          <span>{error || 'Failed to load dashboard data.'}</span>
        </div>
      </div>
    );
  }

  // Format data for Recharts
  const barChartData = Object.entries(metrics.statusCounts || {}).map(([status, count]) => ({
    status: status.replace('_', ' '),
    count,
    fill: STATUS_COLORS[status] || '#64748b',
  }));

  const pieChartData = barChartData.filter((item) => item.count > 0);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between pb-4 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Manager SLA Dashboard</h1>
          <p className="text-xs text-slate-500 mt-1">
            Turnaround time SLA compliance, workload distribution & claims at risk
          </p>
        </div>

        <button
          onClick={fetchDashboardData}
          className="mt-4 sm:mt-0 inline-flex items-center space-x-1.5 bg-white hover:bg-slate-50 text-slate-700 text-xs font-semibold py-2 px-3 rounded-lg border border-slate-300 shadow-sm transition-colors"
        >
          <RefreshCw className="h-3.5 w-3.5" />
          <span>Refresh Data</span>
        </button>
      </div>

      {/* Top Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500">Total Claims</span>
            <CheckCircle2 className="h-5 w-5 text-blue-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900 mt-2">{metrics.totalClaims}</div>
          <div className="text-[11px] text-slate-400 mt-1">Across all policy types</div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-rose-200 shadow-sm bg-rose-50/20">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-rose-700">SLA Breached (&gt;5 Days)</span>
            <AlertTriangle className="h-5 w-5 text-rose-600" />
          </div>
          <div className="text-2xl font-extrabold text-rose-600 mt-2">
            {metrics.slaBreachedCount}
          </div>
          <div className="text-[11px] text-rose-500 mt-1">Immediate action required</div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-amber-200 shadow-sm bg-amber-50/20">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-amber-700">At Risk (3 - 5 Days)</span>
            <Clock className="h-5 w-5 text-amber-600" />
          </div>
          <div className="text-2xl font-extrabold text-amber-600 mt-2">{metrics.slaAtRiskCount}</div>
          <div className="text-[11px] text-amber-600 mt-1">Approaching SLA limit</div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-500">Active Handlers</span>
            <Users className="h-5 w-5 text-purple-500" />
          </div>
          <div className="text-2xl font-extrabold text-slate-900 mt-2">{handlers.length}</div>
          <div className="text-[11px] text-slate-400 mt-1">Available for reassignment</div>
        </div>
      </div>

      {/* Recharts Visualizations */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Bar Chart: Status Distribution */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h2 className="text-sm font-bold text-slate-900 mb-4">Claims Count by Status</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barChartData}>
                <XAxis dataKey="status" tick={{ fontSize: 10 }} />
                <YAxis allowDecimals={false} tick={{ fontSize: 10 }} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#1e293b',
                    borderColor: '#334155',
                    borderRadius: '8px',
                    color: '#fff',
                    fontSize: '12px',
                  }}
                />
                <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                  {barChartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Pie Chart: Status Breakdown */}
        <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
          <h2 className="text-sm font-bold text-slate-900 mb-4">Portfolio Status Breakdown</h2>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieChartData}
                  cx="50%"
                  cy="50%"
                  outerRadius={80}
                  dataKey="count"
                  nameKey="status"
                  label={({ name, percent }) => `${name} (${(percent * 100).toFixed(0)}%)`}
                >
                  {pieChartData.map((entry, index) => (
                    <Cell key={`pie-cell-${index}`} fill={entry.fill} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* SLA Breached Claims Table */}
      <div className="bg-white rounded-xl border border-rose-200 shadow-sm overflow-hidden">
        <div className="bg-rose-50/50 px-6 py-4 border-b border-rose-100 flex items-center justify-between">
          <h2 className="text-sm font-bold text-rose-900 flex items-center space-x-2">
            <AlertTriangle className="h-4 w-4 text-rose-600" />
            <span>SLA Breached Claims (&gt; 5 Days in UNDER_REVIEW)</span>
          </h2>
          <span className="bg-rose-100 text-rose-800 text-xs px-2.5 py-0.5 rounded-full font-bold">
            {metrics.breachedClaims.length} Breached
          </span>
        </div>

        {metrics.breachedClaims.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-500">
            No claims currently breaching turnaround SLA.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-xs">
              <thead className="bg-slate-50 text-slate-700 font-semibold">
                <tr>
                  <th className="px-6 py-3 text-left">Claim Number</th>
                  <th className="px-6 py-3 text-left">Policyholder</th>
                  <th className="px-6 py-3 text-left">Submitted At</th>
                  <th className="px-6 py-3 text-left">Assigned Handler</th>
                  <th className="px-6 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 bg-white">
                {metrics.breachedClaims.map((claim) => (
                  <tr key={claim.id} className="hover:bg-rose-50/30">
                    <td className="px-6 py-4 font-mono font-medium text-slate-900">
                      {claim.claimNumber}
                    </td>
                    <td className="px-6 py-4 font-medium text-slate-800">{claim.holderName}</td>
                    <td className="px-6 py-4 text-slate-600">
                      {new Date(claim.submittedAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 font-medium text-slate-800">
                      {claim.assignedHandlerName || (
                        <span className="italic text-rose-500 font-normal">UNASSIGNED</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-right space-x-2">
                      <button
                        onClick={() => {
                          setSelectedClaim(claim);
                          setSelectedHandlerId(claim.assignedHandlerId || '');
                        }}
                        className="inline-flex items-center space-x-1 bg-purple-50 text-purple-700 hover:bg-purple-100 border border-purple-200 px-2.5 py-1 rounded text-xs font-semibold"
                      >
                        <UserCheck className="h-3.5 w-3.5" />
                        <span>Reassign</span>
                      </button>

                      <Link
                        to={`/claims/${claim.id}`}
                        className="inline-flex items-center text-blue-600 hover:text-blue-800 font-semibold"
                      >
                        <span>View</span>
                        <ChevronRight className="h-3.5 w-3.5 ml-0.5" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Reassign Handler Modal */}
      {selectedClaim && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-sm w-full border border-slate-200 p-6 relative">
            <button
              onClick={() => setSelectedClaim(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="h-5 w-5" />
            </button>

            <h3 className="text-base font-bold text-slate-900 mb-2">Reassign Claim Handler</h3>
            <p className="text-xs text-slate-500 mb-4">
              Claim: <span className="font-mono font-bold text-slate-800">{selectedClaim.claimNumber}</span>
            </p>

            <form onSubmit={handleReassign} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Target Claim Handler
                </label>
                <select
                  required
                  value={selectedHandlerId}
                  onChange={(e) => setSelectedHandlerId(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-purple-500"
                >
                  <option value="">-- Select Handler --</option>
                  {handlers.map((h) => (
                    <option key={h.id} value={h.id}>
                      {h.fullName} ({h.email})
                    </option>
                  ))}
                </select>
              </div>

              <div className="flex justify-end space-x-2 pt-2">
                <button
                  type="button"
                  onClick={() => setSelectedClaim(null)}
                  className="px-3 py-1.5 border border-slate-300 rounded text-xs font-semibold text-slate-700 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={reassigning}
                  className="px-3 py-1.5 bg-purple-600 hover:bg-purple-700 text-white text-xs font-semibold rounded shadow-sm"
                >
                  {reassigning ? 'Assigning...' : 'Save Reassignment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
