import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { StatusBadge } from '../components/StatusBadge';
import { Filter, CheckSquare, ChevronRight, RefreshCw, AlertCircle } from 'lucide-react';

export const HandlerQueue = () => {
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Filter state
  const [statusFilter, setStatusFilter] = useState('');
  const [policyTypeFilter, setPolicyTypeFilter] = useState('');

  useEffect(() => {
    fetchQueue();
  }, [statusFilter, policyTypeFilter]);

  const fetchQueue = async () => {
    setLoading(true);
    setError('');
    try {
      const params = new URLSearchParams();
      if (statusFilter) params.append('status', statusFilter);
      if (policyTypeFilter) params.append('policyType', policyTypeFilter);
      params.append('size', '50');

      const res = await api.get(`/claims?${params.toString()}`);
      setClaims(res.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch review queue.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between pb-6 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Claim Review Queue</h1>
          <p className="text-xs text-slate-500 mt-1">
            Review submitted claims, request additional information, or issue approval/rejection decisions
          </p>
        </div>

        <button
          onClick={fetchQueue}
          className="mt-4 sm:mt-0 inline-flex items-center space-x-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold py-2 px-3 rounded-lg border border-slate-300 transition-colors"
        >
          <RefreshCw className="h-3.5 w-3.5" />
          <span>Refresh Queue</span>
        </button>
      </div>

      {/* Filter Bar */}
      <div className="mt-6 bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-wrap items-center gap-4">
        <div className="flex items-center space-x-2 text-slate-500 text-xs font-semibold">
          <Filter className="h-4 w-4" />
          <span>Filters:</span>
        </div>

        <div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-1.5 border border-slate-300 rounded-lg text-xs bg-slate-50 font-medium focus:ring-2 focus:ring-blue-500"
          >
            <option value="">All Statuses</option>
            <option value="SUBMITTED">SUBMITTED (Needs Review)</option>
            <option value="UNDER_REVIEW">UNDER_REVIEW</option>
            <option value="INFO_REQUESTED">INFO_REQUESTED</option>
            <option value="APPROVED">APPROVED</option>
            <option value="REJECTED">REJECTED</option>
            <option value="SETTLED">SETTLED</option>
          </select>
        </div>

        <div>
          <select
            value={policyTypeFilter}
            onChange={(e) => setPolicyTypeFilter(e.target.value)}
            className="px-3 py-1.5 border border-slate-300 rounded-lg text-xs bg-slate-50 font-medium focus:ring-2 focus:ring-blue-500"
          >
            <option value="">All Policy Types</option>
            <option value="HEALTH">HEALTH</option>
            <option value="MOTOR">MOTOR</option>
            <option value="LIFE">LIFE</option>
            <option value="PROPERTY">PROPERTY</option>
          </select>
        </div>
      </div>

      {error && (
        <div className="mt-4 p-4 bg-red-50 text-red-700 text-xs rounded-lg flex items-center space-x-2">
          <AlertCircle className="h-4 w-4" />
          <span>{error}</span>
        </div>
      )}

      {/* Claims Queue Table */}
      <div className="mt-6 bg-white shadow-sm rounded-xl border border-slate-200 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-slate-500 text-xs">Loading queue claims...</div>
        ) : claims.length === 0 ? (
          <div className="p-12 text-center">
            <CheckSquare className="h-10 w-10 text-slate-300 mx-auto mb-3" />
            <p className="text-sm font-semibold text-slate-700">No claims matching selected filters</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-xs">
              <thead className="bg-slate-50 font-semibold text-slate-700">
                <tr>
                  <th className="px-6 py-3.5 text-left">Claim Number</th>
                  <th className="px-6 py-3.5 text-left">Policyholder</th>
                  <th className="px-6 py-3.5 text-left">Policy Type</th>
                  <th className="px-6 py-3.5 text-right">Claimed Amount</th>
                  <th className="px-6 py-3.5 text-center">Status</th>
                  <th className="px-6 py-3.5 text-left">Assigned Handler</th>
                  <th className="px-6 py-3.5 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-200 bg-white">
                {claims.map((claim) => (
                  <tr key={claim.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="px-6 py-4 font-mono font-medium text-slate-900">
                      {claim.claimNumber}
                    </td>
                    <td className="px-6 py-4">
                      <div className="font-semibold text-slate-800">{claim.holderName}</div>
                      <div className="text-[10px] text-slate-400">{claim.policyNumber}</div>
                    </td>
                    <td className="px-6 py-4 font-medium text-slate-600">{claim.policyType}</td>
                    <td className="px-6 py-4 text-right font-semibold text-slate-900">
                      ₹{Number(claim.claimedAmount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4 text-center">
                      <StatusBadge status={claim.status} />
                    </td>
                    <td className="px-6 py-4 text-slate-600">
                      {claim.assignedHandlerName ? (
                        <span className="font-medium text-slate-800">{claim.assignedHandlerName}</span>
                      ) : (
                        <span className="italic text-slate-400">Unassigned</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-right">
                      <Link
                        to={`/claims/${claim.id}`}
                        className="inline-flex items-center text-blue-600 hover:text-blue-800 font-semibold bg-blue-50 px-2.5 py-1 rounded-md border border-blue-200"
                      >
                        <span>Process</span>
                        <ChevronRight className="h-3.5 w-3.5 ml-1" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
