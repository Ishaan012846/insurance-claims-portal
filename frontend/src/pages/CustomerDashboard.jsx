import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../api/axios';
import { StatusBadge } from '../components/StatusBadge';
import { FilePlus, Search, FileText, ChevronRight, AlertCircle, PlusCircle, X } from 'lucide-react';

export const CustomerDashboard = () => {
  const [claims, setClaims] = useState([]);
  const [policies, setPolicies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // New claim modal state
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    policyId: '',
    incidentDate: new Date().toISOString().split('T')[0],
    description: '',
    claimedAmount: '',
  });
  const [formError, setFormError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchClaimsAndPolicies();
  }, []);

  const fetchClaimsAndPolicies = async () => {
    setLoading(true);
    try {
      const [claimsRes, policiesRes] = await Promise.all([
        api.get('/claims?size=50'),
        api.get('/policies?size=50'),
      ]);
      setClaims(claimsRes.data.content || []);
      setPolicies(policiesRes.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load claims & policies.');
    } finally {
      setLoading(false);
    }
  };

  const handleModalSubmit = async (e) => {
    e.preventDefault();
    setFormError('');

    const selectedPolicy = policies.find((p) => p.id === Number(formData.policyId));
    if (!selectedPolicy) {
      setFormError('Please select a valid policy.');
      return;
    }

    if (Number(formData.claimedAmount) > selectedPolicy.coverageAmount) {
      setFormError(
        `Claimed amount (₹${formData.claimedAmount}) exceeds policy coverage limit (₹${selectedPolicy.coverageAmount}).`
      );
      return;
    }

    setSubmitting(true);
    try {
      await api.post('/claims', {
        policyId: Number(formData.policyId),
        incidentDate: formData.incidentDate,
        description: formData.description,
        claimedAmount: Number(formData.claimedAmount),
      });

      setShowModal(false);
      setFormData({
        policyId: '',
        incidentDate: new Date().toISOString().split('T')[0],
        description: '',
        claimedAmount: '',
      });
      fetchClaimsAndPolicies();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Failed to create claim.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between pb-6 border-b border-slate-200">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">My Insurance Claims</h1>
          <p className="text-xs text-slate-500 mt-1">
            File claims against your active policies and track turnaround progress
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="mt-4 sm:mt-0 inline-flex items-center space-x-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs py-2.5 px-4 rounded-lg shadow-sm transition-colors"
        >
          <PlusCircle className="h-4 w-4" />
          <span>File New Claim</span>
        </button>
      </div>

      {error && (
        <div className="mt-4 p-4 bg-red-50 text-red-700 text-xs rounded-lg flex items-center space-x-2">
          <AlertCircle className="h-4 w-4" />
          <span>{error}</span>
        </div>
      )}

      {/* Claims List Table */}
      <div className="mt-6 bg-white shadow-sm rounded-xl border border-slate-200 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-slate-500 text-xs">Loading your claims...</div>
        ) : claims.length === 0 ? (
          <div className="p-12 text-center">
            <FileText className="h-10 w-10 text-slate-300 mx-auto mb-3" />
            <p className="text-sm font-semibold text-slate-700">No claims filed yet</p>
            <p className="text-xs text-slate-500 mt-1">
              File a new claim against your active policies above.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-200 text-xs">
              <thead className="bg-slate-50 font-semibold text-slate-700">
                <tr>
                  <th className="px-6 py-3.5 text-left">Claim Number</th>
                  <th className="px-6 py-3.5 text-left">Policy</th>
                  <th className="px-6 py-3.5 text-left">Incident Date</th>
                  <th className="px-6 py-3.5 text-right">Claimed Amount</th>
                  <th className="px-6 py-3.5 text-right">Approved Amount</th>
                  <th className="px-6 py-3.5 text-center">Status</th>
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
                      <div className="font-semibold text-slate-800">{claim.policyNumber}</div>
                      <div className="text-[10px] text-slate-400 font-medium">{claim.policyType}</div>
                    </td>
                    <td className="px-6 py-4 text-slate-600">{claim.incidentDate}</td>
                    <td className="px-6 py-4 text-right font-semibold text-slate-900">
                      ₹{Number(claim.claimedAmount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4 text-right font-semibold text-emerald-600">
                      {claim.approvedAmount != null ? `₹${Number(claim.approvedAmount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : '-'}
                    </td>
                    <td className="px-6 py-4 text-center">
                      <StatusBadge status={claim.status} />
                    </td>
                    <td className="px-6 py-4 text-right">
                      <Link
                        to={`/claims/${claim.id}`}
                        className="inline-flex items-center text-blue-600 hover:text-blue-800 font-semibold"
                      >
                        <span>View</span>
                        <ChevronRight className="h-4 w-4 ml-0.5" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* File New Claim Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 overflow-y-auto bg-slate-900/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full border border-slate-200 p-6 relative">
            <button
              onClick={() => setShowModal(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="h-5 w-5" />
            </button>

            <h3 className="text-lg font-bold text-slate-900 mb-4 flex items-center space-x-2">
              <FilePlus className="h-5 w-5 text-blue-600" />
              <span>File Insurance Claim</span>
            </h3>

            {formError && (
              <div className="mb-4 p-3 bg-red-50 text-red-700 text-xs rounded-lg flex items-center space-x-2 border border-red-200">
                <AlertCircle className="h-4 w-4 flex-shrink-0" />
                <span>{formError}</span>
              </div>
            )}

            <form onSubmit={handleModalSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Select Active Policy
                </label>
                <select
                  required
                  value={formData.policyId}
                  onChange={(e) => setFormData({ ...formData, policyId: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs bg-white focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">-- Choose Policy --</option>
                  {policies.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.policyNumber} ({p.type}) - Max Coverage: ₹{Number(p.coverageAmount).toLocaleString('en-IN')}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Incident Date
                </label>
                <input
                  type="date"
                  required
                  value={formData.incidentDate}
                  onChange={(e) => setFormData({ ...formData, incidentDate: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Claimed Amount (₹)
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  required
                  value={formData.claimedAmount}
                  onChange={(e) => setFormData({ ...formData, claimedAmount: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g. 1500.00"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Incident Description
                </label>
                <textarea
                  required
                  rows={3}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-blue-500"
                  placeholder="Describe the incident and loss details..."
                />
              </div>

              <div className="flex justify-end space-x-3 pt-3">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border border-slate-300 rounded-lg text-xs font-semibold text-slate-700 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-lg shadow-sm"
                >
                  {submitting ? 'Creating...' : 'Save Draft Claim'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
