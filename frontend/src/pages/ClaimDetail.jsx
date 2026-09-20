import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { useAuth } from '../context/AuthContext';
import { StatusBadge } from '../components/StatusBadge';
import { AuditTimeline } from '../components/AuditTimeline';
import { DocumentUploader } from '../components/DocumentUploader';
import {
  ShieldAlert,
  ArrowLeft,
  FileText,
  Clock,
  Download,
  CheckCircle2,
  XCircle,
  HelpCircle,
  DollarSign,
  AlertCircle,
  X,
} from 'lucide-react';

export const ClaimDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [claim, setClaim] = useState(null);
  const [auditLogs, setAuditLogs] = useState([]);
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Status transition modal states
  const [showApproveModal, setShowApproveModal] = useState(false);
  const [approvedAmountInput, setApprovedAmountInput] = useState('');
  const [remarksInput, setRemarksInput] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError] = useState('');

  useEffect(() => {
    fetchClaimDetails();
  }, [id]);

  const fetchClaimDetails = async () => {
    setLoading(true);
    setError('');
    try {
      const [claimRes, auditRes, docsRes] = await Promise.all([
        api.get(`/claims/${id}`),
        api.get(`/claims/${id}/audit-logs`),
        api.get(`/claims/${id}/documents`),
      ]);
      setClaim(claimRes.data);
      setAuditLogs(auditRes.data || []);
      setDocuments(docsRes.data || []);
      setApprovedAmountInput(claimRes.data.claimedAmount);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load claim details.');
    } finally {
      setLoading(false);
    }
  };

  const handleStateTransition = async (targetStatus, customApprovedAmount = null) => {
    setActionError('');
    setActionLoading(true);
    try {
      await api.patch(`/claims/${id}/status`, {
        status: targetStatus,
        approvedAmount: customApprovedAmount,
        remarks: remarksInput || `Status transitioned to ${targetStatus}`,
      });

      setShowApproveModal(false);
      setRemarksInput('');
      fetchClaimDetails();
    } catch (err) {
      setActionError(err.response?.data?.message || 'State transition failed.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleSubmitClaim = async () => {
    setActionLoading(true);
    setActionError('');
    try {
      await api.post(`/claims/${id}/submit`);
      fetchClaimDetails();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to submit claim.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDownloadDoc = async (docId, fileName) => {
    try {
      const response = await api.get(`/documents/${docId}/download`, {
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert('Failed to download document file.');
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[60vh]">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  if (error || !claim) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="p-4 bg-red-50 text-red-700 rounded-xl text-xs flex items-center space-x-2">
          <AlertCircle className="h-4 w-4" />
          <span>{error || 'Claim not found.'}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
      {/* Back Header */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center space-x-1.5 text-slate-500 hover:text-slate-800 text-xs font-semibold"
        >
          <ArrowLeft className="h-4 w-4" />
          <span>Back</span>
        </button>

        <div className="flex items-center space-x-3">
          <StatusBadge status={claim.status} />
          <span className="font-mono text-sm font-bold text-slate-800">{claim.claimNumber}</span>
        </div>
      </div>

      {actionError && (
        <div className="p-4 bg-red-50 text-red-700 text-xs rounded-xl flex items-center space-x-2 border border-red-200">
          <AlertCircle className="h-4 w-4 flex-shrink-0" />
          <span>{actionError}</span>
        </div>
      )}

      {/* Main Grid Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 Columns: Details & Actions */}
        <div className="lg:col-span-2 space-y-6">
          {/* Claim Summary Card */}
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
            <h2 className="text-base font-bold text-slate-900 border-b border-slate-100 pb-3">
              Claim Details & Policy Overview
            </h2>

            <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 text-xs">
              <div>
                <span className="text-slate-400 block font-medium">Policyholder</span>
                <span className="font-bold text-slate-900">{claim.holderName}</span>
              </div>
              <div>
                <span className="text-slate-400 block font-medium">Policy Number</span>
                <span className="font-bold text-slate-900">{claim.policyNumber}</span>
              </div>
              <div>
                <span className="text-slate-400 block font-medium">Policy Type</span>
                <span className="font-bold text-slate-900">{claim.policyType}</span>
              </div>

              <div>
                <span className="text-slate-400 block font-medium">Incident Date</span>
                <span className="font-semibold text-slate-800">{claim.incidentDate}</span>
              </div>
              <div>
                <span className="text-slate-400 block font-medium">Claimed Amount</span>
                <span className="font-extrabold text-slate-900 text-sm">
                  ₹{Number(claim.claimedAmount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </span>
              </div>
              <div>
                <span className="text-slate-400 block font-medium">Approved Amount</span>
                <span className="font-extrabold text-emerald-600 text-sm">
                  {claim.approvedAmount != null ? `₹${Number(claim.approvedAmount).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : 'N/A'}
                </span>
              </div>
            </div>

            <div className="pt-2">
              <span className="text-xs text-slate-400 font-medium block mb-1">
                Incident Description
              </span>
              <p className="text-xs text-slate-700 bg-slate-50 p-3 rounded-lg border border-slate-200">
                {claim.description}
              </p>
            </div>
          </div>

          {/* Workflow Action Buttons */}
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-3">
            <h3 className="text-xs font-bold text-slate-900 uppercase tracking-wider">
              Available Workflow Actions
            </h3>

            <div className="flex flex-wrap gap-3">
              {/* Customer Actions */}
              {user.role === 'CUSTOMER' && claim.status === 'DRAFT' && (
                <button
                  onClick={handleSubmitClaim}
                  disabled={actionLoading}
                  className="bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center space-x-1.5"
                >
                  <CheckCircle2 className="h-4 w-4" />
                  <span>Submit Claim for Processing</span>
                </button>
              )}

              {/* Handler / Manager Actions */}
              {(user.role === 'HANDLER' || user.role === 'MANAGER') && (
                <>
                  {claim.status === 'SUBMITTED' && (
                    <button
                      onClick={() => handleStateTransition('UNDER_REVIEW')}
                      disabled={actionLoading}
                      className="bg-amber-600 hover:bg-amber-700 text-white text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center space-x-1.5"
                    >
                      <Clock className="h-4 w-4" />
                      <span>Start Review (Move to UNDER_REVIEW)</span>
                    </button>
                  )}

                  {claim.status === 'UNDER_REVIEW' && (
                    <>
                      <button
                        onClick={() => setShowApproveModal(true)}
                        disabled={actionLoading}
                        className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center space-x-1.5"
                      >
                        <CheckCircle2 className="h-4 w-4" />
                        <span>Approve Claim</span>
                      </button>

                      <button
                        onClick={() => handleStateTransition('INFO_REQUESTED')}
                        disabled={actionLoading}
                        className="bg-purple-600 hover:bg-purple-700 text-white text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center space-x-1.5"
                      >
                        <HelpCircle className="h-4 w-4" />
                        <span>Request Info</span>
                      </button>

                      <button
                        onClick={() => handleStateTransition('REJECTED')}
                        disabled={actionLoading}
                        className="bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center space-x-1.5"
                      >
                        <XCircle className="h-4 w-4" />
                        <span>Reject Claim</span>
                      </button>
                    </>
                  )}

                  {claim.status === 'INFO_REQUESTED' && (
                    <button
                      onClick={() => handleStateTransition('UNDER_REVIEW')}
                      disabled={actionLoading}
                      className="bg-amber-600 hover:bg-amber-700 text-white text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center space-x-1.5"
                    >
                      <Clock className="h-4 w-4" />
                      <span>Resume Review (UNDER_REVIEW)</span>
                    </button>
                  )}

                  {claim.status === 'APPROVED' && (
                    <button
                      onClick={() => handleStateTransition('SETTLED')}
                      disabled={actionLoading}
                      className="bg-teal-600 hover:bg-teal-700 text-white text-xs font-semibold px-4 py-2 rounded-lg shadow-sm flex items-center space-x-1.5"
                    >
                      <DollarSign className="h-4 w-4" />
                      <span>Mark Claim as SETTLED</span>
                    </button>
                  )}
                </>
              )}
            </div>
          </div>

          {/* Attached Documents List */}
          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
            <h3 className="text-sm font-bold text-slate-900 flex items-center space-x-2">
              <FileText className="h-4 w-4 text-blue-600" />
              <span>Supporting Documents ({documents.length})</span>
            </h3>

            {documents.length === 0 ? (
              <p className="text-xs text-slate-400 italic">No supporting documents attached yet.</p>
            ) : (
              <div className="divide-y divide-slate-100">
                {documents.map((doc) => (
                  <div key={doc.id} className="py-3 flex items-center justify-between text-xs">
                    <div className="flex items-center space-x-3">
                      <FileText className="h-4 w-4 text-slate-400" />
                      <div>
                        <p className="font-semibold text-slate-800">{doc.fileName}</p>
                        <p className="text-[10px] text-slate-400">
                          {(doc.sizeBytes / (1024 * 1024)).toFixed(2)} MB • {doc.contentType}
                        </p>
                      </div>
                    </div>

                    <button
                      onClick={() => handleDownloadDoc(doc.id, doc.fileName)}
                      className="inline-flex items-center space-x-1 text-blue-600 hover:text-blue-800 font-semibold bg-blue-50 px-2.5 py-1 rounded border border-blue-200"
                    >
                      <Download className="h-3.5 w-3.5" />
                      <span>Download</span>
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Uploader & Status Timeline */}
        <div className="space-y-6">
          <DocumentUploader claimId={id} onUploadSuccess={fetchClaimDetails} />

          <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm space-y-4">
            <h3 className="text-sm font-bold text-slate-900 border-b border-slate-100 pb-3">
              Status Audit History Timeline
            </h3>

            <AuditTimeline auditLogs={auditLogs} />
          </div>
        </div>
      </div>

      {/* Approve Modal */}
      {showApproveModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full border border-slate-200 p-6 relative">
            <button
              onClick={() => setShowApproveModal(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="h-5 w-5" />
            </button>

            <h3 className="text-base font-bold text-slate-900 mb-2">Approve Claim Settlement</h3>
            <p className="text-xs text-slate-500 mb-4">
              Enter approved payout amount. Cannot exceed claimed amount (₹{claim.claimedAmount}).
            </p>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Approved Amount (₹)
                </label>
                <input
                  type="number"
                  step="0.01"
                  max={claim.claimedAmount}
                  required
                  value={approvedAmountInput}
                  onChange={(e) => setApprovedAmountInput(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Remarks / Justification
                </label>
                <textarea
                  rows={2}
                  value={remarksInput}
                  onChange={(e) => setRemarksInput(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-300 rounded-lg text-xs focus:ring-2 focus:ring-emerald-500"
                  placeholder="Approved based on supporting medical receipts..."
                />
              </div>

              <div className="flex justify-end space-x-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowApproveModal(false)}
                  className="px-3 py-1.5 border border-slate-300 rounded text-xs font-semibold text-slate-700 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  onClick={() =>
                    handleStateTransition('APPROVED', Number(approvedAmountInput))
                  }
                  disabled={actionLoading}
                  className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded shadow-sm"
                >
                  {actionLoading ? 'Approving...' : 'Confirm Approval'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
