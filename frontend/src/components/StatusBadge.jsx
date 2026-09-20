import React from 'react';

export const StatusBadge = ({ status }) => {
  const getStyle = (s) => {
    switch (s) {
      case 'DRAFT':
        return 'bg-slate-100 text-slate-700 border-slate-300';
      case 'SUBMITTED':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'UNDER_REVIEW':
        return 'bg-amber-50 text-amber-700 border-amber-300';
      case 'INFO_REQUESTED':
        return 'bg-purple-50 text-purple-700 border-purple-300';
      case 'APPROVED':
        return 'bg-emerald-50 text-emerald-700 border-emerald-300';
      case 'REJECTED':
        return 'bg-rose-50 text-rose-700 border-rose-300';
      case 'SETTLED':
        return 'bg-teal-50 text-teal-700 border-teal-300';
      default:
        return 'bg-gray-100 text-gray-700 border-gray-300';
    }
  };

  return (
    <span
      className={`inline-flex items-center px-2.5 py-1 rounded-md text-xs font-semibold border ${getStyle(
        status
      )}`}
    >
      <span className="h-1.5 w-1.5 rounded-full mr-1.5 bg-current"></span>
      {status ? status.replace('_', ' ') : 'UNKNOWN'}
    </span>
  );
};
