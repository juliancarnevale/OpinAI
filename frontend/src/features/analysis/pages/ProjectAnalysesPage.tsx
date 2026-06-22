import React, { useEffect, useState, Suspense, lazy } from 'react';
import { useParams, Link, useSearchParams } from 'react-router-dom';
import { useAnalysisStore } from '../../../store/analysisStore';
import { useProjectStore } from '../../../store/projectStore';
import { AnalysisCard } from '../components/AnalysisCard';
import { CreateAnalysisModal } from '../components/CreateAnalysisModal';
import { ReportHistorySkeleton } from '../../reports/components/ReportHistorySkeleton';
import { ROUTES } from '../../../config/constants';
import { ArrowLeft, Plus, Loader2, AlertCircle, X, FileText, FolderOpen } from 'lucide-react';

// Carga perezosa real (React.lazy)
const ReportHistory = lazy(() =>
  import('../../reports/components/ReportHistory').then((module) => ({ default: module.ReportHistory }))
);

export const ProjectAnalysesPage: React.FC = () => {
  const { projectId } = useParams<{ projectId: string }>();
  const [searchParams, setSearchParams] = useSearchParams();
  
  // Sincronizar pestaña activa con URL query params (?tab=analyses o ?tab=reports)
  const activeTab = searchParams.get('tab') === 'reports' ? 'reports' : 'analyses';

  const {
    analyses,
    isLoading: isAnalysisLoading,
    error: analysisError,
    fetchProjectAnalyses,
    clearError
  } = useAnalysisStore();

  const { projects, fetchProjects, isLoading: isProjectLoading } = useProjectStore();
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    if (projectId) {
      fetchProjectAnalyses(projectId);
    }
  }, [projectId, fetchProjectAnalyses]);

  useEffect(() => {
    if (projects.length === 0) {
      fetchProjects();
    }
  }, [projects.length, fetchProjects]);

  const currentProject = projects.find((p) => p.id === projectId);
  const projectName = currentProject ? currentProject.name : 'Proyecto';
  const isLoading = isAnalysisLoading || (isProjectLoading && projects.length === 0);

  return (
    <div className="space-y-6">
      {/* Navegación y Cabecera */}
      <div className="flex flex-col space-y-4">
        <Link
          to={ROUTES.PROJECTS}
          className="inline-flex items-center space-x-2 text-sm text-slate-400 hover:text-white transition w-fit"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Volver a Proyectos</span>
        </Link>

        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <h1 className="text-3xl font-bold tracking-tight text-white flex items-center space-x-2">
              <FolderOpen className="w-8 h-8 text-violet-500 mr-2" />
              <span>{projectName}</span>
            </h1>
            <p className="text-slate-400 mt-1">
              {currentProject?.description || 'Gestiona los informes de comentarios y exportación.'}
            </p>
          </div>

          {activeTab === 'analyses' && !isLoading && analyses.length > 0 && projectId && (
            <button
              onClick={() => setIsModalOpen(true)}
              className="inline-flex items-center space-x-2 px-4 py-2.5 bg-violet-600 hover:bg-violet-700 text-white font-medium rounded-lg text-sm transition duration-200 shadow-lg shadow-violet-600/20 cursor-pointer"
            >
              <Plus className="w-5 h-5" />
              <span>Nuevo Análisis</span>
            </button>
          )}
        </div>
      </div>

      {/* Navegación por pestañas */}
      <div className="flex border-b border-slate-800">
        <button
          onClick={() => setSearchParams({ tab: 'analyses' })}
          className={`px-6 py-3 text-sm font-medium border-b-2 transition cursor-pointer ${
            activeTab === 'analyses'
              ? 'border-violet-500 text-violet-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          Análisis de Opinión
        </button>
        <button
          onClick={() => setSearchParams({ tab: 'reports' })}
          className={`px-6 py-3 text-sm font-medium border-b-2 transition cursor-pointer ${
            activeTab === 'reports'
              ? 'border-violet-500 text-violet-400'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          Reportes y Exportación
        </button>
      </div>

      {/* Contenido Dinámico */}
      {activeTab === 'analyses' ? (
        <div className="space-y-6">
          {analysisError && (
            <div className="bg-rose-950/40 border border-rose-800 text-rose-200 p-4 rounded-xl flex items-start justify-between">
              <div className="flex space-x-3">
                <AlertCircle className="w-5 h-5 text-rose-500 shrink-0 mt-0.5" />
                <div>
                  <p className="font-semibold text-sm">Ocurrió un error</p>
                  <p className="text-xs text-rose-300 mt-1">{analysisError}</p>
                </div>
              </div>
              <button onClick={clearError} className="text-rose-400 hover:text-rose-200 cursor-pointer">
                <X className="w-5 h-5" />
              </button>
            </div>
          )}

          {isLoading ? (
            <div className="flex flex-col items-center justify-center py-20 space-y-4">
              <Loader2 className="w-10 h-10 text-violet-500 animate-spin" />
              <p className="text-slate-400 text-sm">Cargando la lista de análisis...</p>
            </div>
          ) : analyses.length === 0 ? (
            <div className="flex flex-col items-center justify-center border-2 border-dashed border-slate-800 rounded-2xl py-16 px-4 text-center max-w-2xl mx-auto my-8">
              <div className="w-16 h-16 rounded-full bg-violet-500/10 flex items-center justify-center text-violet-500 mb-6">
                <FileText className="w-8 h-8" />
              </div>
              <h3 className="text-xl font-bold text-white mb-2">No tienes análisis todavía.</h3>
              <p className="text-slate-400 max-w-sm mb-8">
                Agrega opiniones de clientes para crear tu primer análisis.
              </p>
              <button
                onClick={() => setIsModalOpen(true)}
                className="inline-flex items-center space-x-2 px-6 py-3 bg-violet-600 hover:bg-violet-700 text-white font-medium rounded-lg text-sm transition shadow-lg shadow-violet-600/20 cursor-pointer"
              >
                <Plus className="w-5 h-5" />
                <span>Crear Análisis</span>
              </button>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {analyses.map((analysis) => (
                <div key={analysis.id}>
                  <AnalysisCard analysis={analysis} />
                </div>
              ))}
            </div>
          )}
        </div>
      ) : (
        /* Pestaña de Reportes con Carga Perezosa */
        projectId && (
          <Suspense fallback={<ReportHistorySkeleton />}>
            <ReportHistory projectId={projectId} />
          </Suspense>
        )
      )}

      {/* Modal de Creación */}
      {projectId && (
        <CreateAnalysisModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          projectId={projectId}
        />
      )}
    </div>
  );
};
