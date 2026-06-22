import React, { useEffect, useState, useRef } from 'react';
import { useReportStore } from '../store/reportStore';
import { formatFileSize } from '../utils/formatFileSize';
import { ReportStatusBadge } from './ReportStatusBadge';
import { ConfirmDeleteModal } from './ConfirmDeleteModal';
import { FileText, FileSpreadsheet, Download, Trash2, AlertCircle, RefreshCw } from 'lucide-react';

interface Props {
  projectId: string;
}

export const ReportHistory: React.FC<Props> = ({ projectId }) => {
  const {
    reports,
    isLoading,
    isGenerating,
    error,
    errorStatus,
    fetchReports,
    generateReport,
    downloadReport,
    deleteReport,
    clearError
  } = useReportStore();

  const [selectedFormat, setSelectedFormat] = useState<'PDF' | 'CSV'>('PDF');
  const [deleteReportId, setDeleteReportId] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Referencias para control de polling recursivo y abort signals
  const pollTimeoutRef = useRef<any>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  // Carga inicial
  useEffect(() => {
    const controller = new AbortController();
    fetchReports(projectId, controller.signal);
    return () => controller.abort();
  }, [projectId, fetchReports]);

  // Polling recursivo controlado por estado
  useEffect(() => {
    const hasGenerating = reports.some((r) => r.status === 'GENERATING');

    if (!hasGenerating) {
      if (pollTimeoutRef.current) {
        clearTimeout(pollTimeoutRef.current);
      }
      return;
    }

    // Inicializar o limpiar el AbortController previo para el polling
    abortControllerRef.current = new AbortController();

    const runPoll = async () => {
      try {
        await fetchReports(projectId, abortControllerRef.current?.signal);
        // Lanzar la siguiente petición únicamente al resolverse la actual
        pollTimeoutRef.current = setTimeout(runPoll, 3000);
      } catch (err) {
        // Ignorar cancelaciones de AbortController
      }
    };

    pollTimeoutRef.current = setTimeout(runPoll, 3000);

    return () => {
      if (pollTimeoutRef.current) {
        clearTimeout(pollTimeoutRef.current);
      }
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [reports, projectId, fetchReports]);

  const handleGenerate = async () => {
    try {
      await generateReport(projectId, selectedFormat);
    } catch {
      // Controlado por el store
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteReportId) return;
    setIsDeleting(true);
    try {
      await deleteReport(projectId, deleteReportId);
      setDeleteReportId(null);
    } catch {
      // Controlado por el store
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Panel de Configuración de Reporte */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-lg">
        <h3 className="text-lg font-bold text-white mb-2">Generar Nuevo Reporte</h3>
        <p className="text-xs text-slate-400 mb-6">Selecciona el formato de exportación para tu reporte ejecutivo.</p>
        
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
          <button
            onClick={() => setSelectedFormat('PDF')}
            className={`flex items-start space-x-4 p-4 rounded-xl text-left border transition ${
              selectedFormat === 'PDF'
                ? 'border-violet-500 bg-violet-500/10 text-white'
                : 'border-slate-800 bg-slate-950 text-slate-400 hover:border-slate-700'
            }`}
          >
            <FileText className={`w-8 h-8 shrink-0 ${selectedFormat === 'PDF' ? 'text-violet-400' : 'text-slate-500'}`} />
            <div>
              <p className="font-semibold text-sm text-white">Documento PDF</p>
              <p className="text-xs text-slate-400 mt-1">Genera un reporte visual estructurado con KPIs, gráficos y análisis ejecutivo.</p>
            </div>
          </button>

          <button
            onClick={() => setSelectedFormat('CSV')}
            className={`flex items-start space-x-4 p-4 rounded-xl text-left border transition ${
              selectedFormat === 'CSV'
                ? 'border-violet-500 bg-violet-500/10 text-white'
                : 'border-slate-800 bg-slate-950 text-slate-400 hover:border-slate-700'
            }`}
          >
            <FileSpreadsheet className={`w-8 h-8 shrink-0 ${selectedFormat === 'CSV' ? 'text-violet-400' : 'text-slate-500'}`} />
            <div>
              <p className="font-semibold text-sm text-white">Hoja de Cálculo CSV</p>
              <p className="text-xs text-slate-400 mt-1">Genera un dataset estructurado con todos los comentarios limpios para análisis en Excel.</p>
            </div>
          </button>
        </div>

        <button
          onClick={handleGenerate}
          disabled={isGenerating || isLoading}
          className="w-full sm:w-auto inline-flex items-center justify-center space-x-2 px-6 py-3 bg-violet-600 hover:bg-violet-700 text-white font-medium rounded-lg text-sm transition shadow-lg shadow-violet-600/20 disabled:opacity-50 cursor-pointer"
        >
          {isGenerating ? (
            <>
              <RefreshCw className="w-4 h-4 animate-spin" />
              <span>Generando Reporte...</span>
            </>
          ) : (
            <span>Generar Reporte</span>
          )}
        </button>
      </div>

      {/* Alertas semánticas diferenciadas (422 vs Crítico) */}
      {error && (
        <div className={`p-4 rounded-xl flex items-start justify-between border animate-fade-in ${
          errorStatus === 422 
            ? 'bg-amber-950/40 border-amber-800 text-amber-200'
            : 'bg-rose-950/40 border-rose-800 text-rose-200'
        }`}>
          <div className="flex space-x-3">
            <AlertCircle className={`w-5 h-5 shrink-0 mt-0.5 ${errorStatus === 422 ? 'text-amber-500' : 'text-rose-500'}`} />
            <div>
              <p className="font-semibold text-sm">
                {errorStatus === 422 ? 'Aviso del Dominio (Límites)' : 'Error de Sistema'}
              </p>
              <p className="text-xs mt-1">{error}</p>
            </div>
          </div>
          <button onClick={clearError} className="font-bold hover:opacity-80">X</button>
        </div>
      )}

      {/* Tabla del Historial */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-lg">
        <div className="p-6 border-b border-slate-800">
          <h3 className="text-lg font-bold text-white">Historial de Reportes</h3>
        </div>

        {reports.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 px-4 text-center">
            <div className="w-12 h-12 rounded-full bg-slate-800 flex items-center justify-center text-slate-400 mb-4">
              <FileText className="w-6 h-6" />
            </div>
            <h4 className="font-semibold text-white">No hay reportes generados.</h4>
            <p className="text-xs text-slate-400 max-w-sm mt-1">Elige un formato arriba para descargar un reporte consolidado de este proyecto.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full border-collapse text-left">
              <thead>
                <tr className="border-b border-slate-800 text-xs font-semibold text-slate-400 uppercase bg-slate-950/40">
                  <th className="p-4">Archivo</th>
                  <th className="p-4">Tamaño</th>
                  <th className="p-4">Estado</th>
                  <th className="p-4">Creado</th>
                  <th className="p-4 text-right">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800 text-sm text-slate-300">
                {reports.map((report) => (
                  <tr key={report.id} className="hover:bg-slate-800/20 transition">
                    <td className="p-4 font-medium text-white flex items-center space-x-3">
                      {report.format === 'PDF' ? (
                        <FileText className="w-5 h-5 text-rose-400 shrink-0" />
                      ) : (
                        <FileSpreadsheet className="w-5 h-5 text-emerald-400 shrink-0" />
                      )}
                      <span className="truncate max-w-xs">{report.name}</span>
                    </td>
                    <td className="p-4">{formatFileSize(report.fileSize)}</td>
                    <td className="p-4">
                      <ReportStatusBadge status={report.status} />
                    </td>
                    <td className="p-4">
                      {new Date(report.createdAt).toLocaleString('es-ES', {
                        day: '2-digit',
                        month: '2-digit',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </td>
                    <td className="p-4 text-right space-x-2">
                      <button
                        onClick={() => downloadReport(report.id, report.name)}
                        disabled={report.status !== 'READY'}
                        className="inline-flex items-center justify-center p-2 text-slate-400 hover:text-white bg-slate-800 hover:bg-slate-700 disabled:opacity-30 disabled:pointer-events-none rounded-lg transition cursor-pointer"
                        title="Descargar reporte"
                      >
                        <Download className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => setDeleteReportId(report.id)}
                        className="inline-flex items-center justify-center p-2 text-slate-400 hover:text-rose-400 bg-slate-800 hover:bg-rose-950/20 rounded-lg transition cursor-pointer"
                        title="Eliminar reporte"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <ConfirmDeleteModal
        isOpen={deleteReportId !== null}
        title="¿Eliminar reporte definitivo?"
        message="Esta acción es irreversible y eliminará de forma permanente el archivo del servidor local y el registro de la base de datos."
        isLoading={isDeleting}
        onConfirm={handleDeleteConfirm}
        onClose={() => setDeleteReportId(null)}
      />
    </div>
  );
};
