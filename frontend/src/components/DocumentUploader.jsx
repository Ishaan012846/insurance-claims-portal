import React, { useState } from 'react';
import api from '../api/axios';
import { UploadCloud, FileText, CheckCircle2, AlertCircle, Trash2 } from 'lucide-react';

export const DocumentUploader = ({ claimId, onUploadSuccess }) => {
  const [selectedFile, setSelectedFile] = useState(null);
  const [error, setError] = useState('');
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);

  const ALLOWED_TYPES = ['application/pdf', 'image/png', 'image/jpeg', 'image/jpg'];
  const MAX_SIZE_MB = 10;

  const handleFileSelect = (e) => {
    setError('');
    const file = e.target.files[0];
    if (!file) return;

    if (!ALLOWED_TYPES.includes(file.type.toLowerCase())) {
      setError('Invalid file format. Only PDF, PNG, and JPEG files are allowed.');
      setSelectedFile(null);
      return;
    }

    if (file.size > MAX_SIZE_MB * 1024 * 1024) {
      setError(`File size exceeds maximum limit of ${MAX_SIZE_MB}MB.`);
      setSelectedFile(null);
      return;
    }

    setSelectedFile(file);
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    const formData = new FormData();
    formData.append('file', selectedFile);

    setUploading(true);
    setUploadProgress(0);
    setError('');

    try {
      await api.post(`/claims/${claimId}/documents`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          setUploadProgress(percentCompleted);
        },
      });

      setSelectedFile(null);
      setUploadProgress(0);
      if (onUploadSuccess) onUploadSuccess();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload document.');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="bg-white p-5 rounded-lg border border-slate-200 shadow-sm">
      <h3 className="text-sm font-semibold text-slate-900 mb-3 flex items-center space-x-2">
        <UploadCloud className="h-4 w-4 text-blue-600" />
        <span>Upload Supporting Document</span>
      </h3>

      {error && (
        <div className="mb-4 p-3 bg-red-50 text-red-700 text-xs rounded-lg flex items-center space-x-2 border border-red-200">
          <AlertCircle className="h-4 w-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}

      {!selectedFile ? (
        <label className="flex flex-col items-center justify-center w-full h-32 border-2 border-dashed border-slate-300 rounded-lg cursor-pointer bg-slate-50 hover:bg-blue-50/50 hover:border-blue-400 transition-colors">
          <div className="flex flex-col items-center justify-center pt-5 pb-6">
            <UploadCloud className="w-8 h-8 mb-2 text-slate-400" />
            <p className="text-xs text-slate-600 font-medium">
              Click to select or drag PDF, PNG, or JPEG
            </p>
            <p className="text-[10px] text-slate-400 mt-1">Max file size: 10MB</p>
          </div>
          <input
            type="file"
            className="hidden"
            accept=".pdf,.png,.jpg,.jpeg"
            onChange={handleFileSelect}
          />
        </label>
      ) : (
        <div className="space-y-3">
          <div className="flex items-center justify-between p-3 bg-slate-50 rounded-lg border border-slate-200">
            <div className="flex items-center space-x-3 overflow-hidden">
              <FileText className="h-5 w-5 text-blue-600 flex-shrink-0" />
              <div className="truncate">
                <p className="text-xs font-medium text-slate-800 truncate">{selectedFile.name}</p>
                <p className="text-[10px] text-slate-400">
                  {(selectedFile.size / (1024 * 1024)).toFixed(2)} MB
                </p>
              </div>
            </div>

            {!uploading && (
              <button
                type="button"
                onClick={() => setSelectedFile(null)}
                className="text-slate-400 hover:text-red-600 p-1 transition-colors"
              >
                <Trash2 className="h-4 w-4" />
              </button>
            )}
          </div>

          {uploading && (
            <div>
              <div className="flex justify-between text-xs text-slate-600 mb-1 font-medium">
                <span>Uploading...</span>
                <span>{uploadProgress}%</span>
              </div>
              <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                <div
                  className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                  style={{ width: `${uploadProgress}%` }}
                ></div>
              </div>
            </div>
          )}

          {!uploading && (
            <button
              onClick={handleUpload}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold py-2 px-4 rounded-lg shadow-sm transition-colors flex items-center justify-center space-x-1.5"
            >
              <CheckCircle2 className="h-4 w-4" />
              <span>Confirm & Upload File</span>
            </button>
          )}
        </div>
      )}
    </div>
  );
};
