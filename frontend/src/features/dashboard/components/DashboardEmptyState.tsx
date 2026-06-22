import React from 'react';
import { useNavigate } from 'react-router-dom';
import { FolderKanban, Plus } from 'lucide-react';
import { ROUTES } from '../../../config/constants';

export const DashboardEmptyState: React.FC = () => {
  const navigate = useNavigate();

  const handleStart = () => {
    // Redirect to projects list, passing state to automatically open the creation form
    navigate(ROUTES.PROJECTS, { state: { openCreateForm: true } });
  };

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-3xl p-8 md:p-12 shadow-2xl max-w-3xl mx-auto my-6 relative overflow-hidden text-center space-y-8">
      <div className="absolute top-0 right-0 w-80 h-80 bg-violet-600/5 rounded-full blur-3xl pointer-events-none" />
      
      {/* Icon with glow halo */}
      <div className="p-5 bg-violet-500/10 rounded-full w-fit mx-auto border border-violet-500/20 shadow-inner relative">
        <div className="absolute inset-0 bg-violet-500/5 rounded-full animate-ping" />
        <FolderKanban className="w-12 h-12 text-violet-400" />
      </div>

      <div className="space-y-3">
        <h2 className="text-2xl md:text-3xl font-extrabold text-white tracking-tight">¡Bienvenido a OpinAI!</h2>
        <p className="text-slate-400 text-sm md:text-base max-w-lg mx-auto leading-relaxed">
          Comienza a transformar la opinión de tus clientes en información accionable para tu negocio a través de Inteligencia Artificial.
        </p>
      </div>

      {/* Guided Steps */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-xl mx-auto text-left">
        <div className="bg-slate-950/40 p-4 rounded-xl border border-slate-850 flex space-x-3 items-start">
          <span className="flex items-center justify-center w-6 h-6 rounded-full bg-violet-500/10 text-violet-400 text-xs font-bold shrink-0 border border-violet-500/20">1</span>
          <div>
            <h4 className="font-bold text-white text-xs uppercase tracking-wider">Crea un Proyecto</h4>
            <p className="text-slate-500 text-[11px] mt-0.5 leading-relaxed">Define el producto o servicio que deseas auditar.</p>
          </div>
        </div>
        <div className="bg-slate-950/40 p-4 rounded-xl border border-slate-850 flex space-x-3 items-start">
          <span className="flex items-center justify-center w-6 h-6 rounded-full bg-violet-500/10 text-violet-400 text-xs font-bold shrink-0 border border-violet-500/20">2</span>
          <div>
            <h4 className="font-bold text-white text-xs uppercase tracking-wider">Genera Análisis de IA</h4>
            <p className="text-slate-500 text-[11px] mt-0.5 leading-relaxed">Carga opiniones y deja que Gemini extraiga los insights por ti.</p>
          </div>
        </div>
      </div>

      {/* CTA Button */}
      <div className="pt-4">
        <button
          onClick={handleStart}
          className="inline-flex items-center space-x-2 px-6 py-3 bg-violet-600 hover:bg-violet-700 text-white font-bold rounded-xl text-sm transition duration-200 shadow-lg shadow-violet-600/20 hover:shadow-violet-600/35 cursor-pointer"
        >
          <Plus className="w-5 h-5" />
          <span>Crear tu primer proyecto</span>
        </button>
      </div>
    </div>
  );
};
