import React from 'react';
import { Link } from 'react-router-dom';
import type { RecentProject } from '../types/DashboardOverview';
import { FolderKanban, MessageSquare, ArrowRight, Clock } from 'lucide-react';
import { ROUTES } from '../../../config/constants';

interface RecentProjectsListProps {
  projects: RecentProject[];
}

export const RecentProjectsList: React.FC<RecentProjectsListProps> = ({ projects }) => {
  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('es-ES', {
        month: 'short',
        day: 'numeric',
        year: 'numeric'
      });
    } catch (e) {
      return dateString;
    }
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl flex flex-col h-full">
      <div className="flex items-center justify-between border-b border-slate-800/80 pb-4 mb-4 shrink-0">
        <div className="flex items-center space-x-2 text-violet-400">
          <FolderKanban className="w-5 h-5" />
          <h3 className="font-bold text-white text-base">Proyectos Recientes</h3>
        </div>
        <Link
          to={ROUTES.PROJECTS}
          className="text-xs font-semibold text-violet-400 hover:text-violet-300 transition"
        >
          Ver todos
        </Link>
      </div>

      <div className="flex-1 overflow-y-auto space-y-4">
        {projects.length === 0 ? (
          <div className="py-12 text-center text-slate-500 text-sm">
            No has creado proyectos todavía.
          </div>
        ) : (
          projects.map((project) => (
            <div
              key={project.id}
              className="p-4 bg-slate-950/40 border border-slate-850 rounded-xl hover:border-slate-800 hover:bg-slate-950/80 transition duration-200 flex items-center justify-between gap-4 group"
            >
              <div className="min-w-0 space-y-1.5">
                <h4 className="font-bold text-white text-sm truncate group-hover:text-violet-400 transition" title={project.name}>
                  {project.name}
                </h4>
                {project.description && (
                  <p className="text-xs text-slate-400 truncate max-w-xs md:max-w-sm">
                    {project.description}
                  </p>
                )}
                <div className="flex items-center space-x-4 text-[11px] text-slate-500 font-semibold">
                  <div className="flex items-center space-x-1">
                    <MessageSquare className="w-3.5 h-3.5 shrink-0" />
                    <span>{project.feedbackItemsCount} opiniones</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <Clock className="w-3.5 h-3.5 shrink-0" />
                    <span>{formatDate(project.createdAt)}</span>
                  </div>
                </div>
              </div>

              <Link
                to={ROUTES.PROJECT_ANALYSES(project.id)}
                className="p-2 bg-slate-900 border border-slate-800 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 hover:border-slate-700 transition shrink-0 group-hover:translate-x-0.5"
                title="Ver análisis de proyecto"
              >
                <ArrowRight className="w-4 h-4" />
              </Link>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
