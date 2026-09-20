import React from 'react';
import { StatusBadge } from './StatusBadge';
import { Clock, User, ArrowRight } from 'lucide-react';

export const AuditTimeline = ({ auditLogs = [] }) => {
  if (!auditLogs || auditLogs.length === 0) {
    return (
      <div className="text-center py-6 text-slate-500 text-sm italic">
        No audit log records available.
      </div>
    );
  }

  return (
    <div className="flow-root">
      <ul className="-mb-8">
        {auditLogs.map((log, index) => {
          const isLast = index === auditLogs.length - 1;
          const formattedDate = new Date(log.timestamp).toLocaleString();

          return (
            <li key={log.id || index}>
              <div className="relative pb-8">
                {!isLast && (
                  <span
                    className="absolute top-4 left-4 -ml-px h-full w-0.5 bg-slate-200"
                    aria-hidden="true"
                  />
                )}
                <div className="relative flex space-x-3">
                  <div>
                    <span className="h-8 w-8 rounded-full bg-blue-100 flex items-center justify-center ring-8 ring-white text-blue-600">
                      <Clock className="h-4 w-4" />
                    </span>
                  </div>
                  <div className="flex-1 min-w-0 bg-slate-50 rounded-lg p-4 border border-slate-200">
                    <div className="flex items-center justify-between text-sm">
                      <div className="flex items-center space-x-2">
                        {log.fromStatus ? (
                          <>
                            <StatusBadge status={log.fromStatus} />
                            <ArrowRight className="h-3.5 w-3.5 text-slate-400" />
                          </>
                        ) : (
                          <span className="text-xs text-slate-400 font-medium">NEW CLAIM</span>
                        )}
                        <StatusBadge status={log.toStatus} />
                      </div>
                      <span className="text-xs text-slate-400 font-medium">{formattedDate}</span>
                    </div>

                    {log.remarks && (
                      <p className="mt-2 text-xs text-slate-600 bg-white p-2.5 rounded border border-slate-100">
                        {log.remarks}
                      </p>
                    )}

                    <div className="mt-2 flex items-center space-x-1.5 text-xs text-slate-500">
                      <User className="h-3.5 w-3.5" />
                      <span>{log.actorName || `Actor ID: ${log.actorId}`}</span>
                    </div>
                  </div>
                </div>
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
};
