import React from 'react';

const SettingsPage: React.FC = () => {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-white">Configuración</h1>
        <p className="text-slate-400 mt-1">Administra las opciones de tu cuenta e integración de API.</p>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 space-y-6 max-w-2xl">
        <div className="border-b border-slate-800 pb-4">
          <h3 className="text-lg font-semibold text-white">API Keys</h3>
          <p className="text-sm text-slate-400 mt-1">Configuración del modelo de Inteligencia Artificial para Gemini.</p>
        </div>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-300">Gemini API Key</label>
            <input
              type="password"
              placeholder="••••••••••••••••••••••••••••••••"
              disabled
              className="mt-1.5 block w-full rounded-lg bg-slate-950 border border-slate-800 px-4 py-2.5 text-sm text-slate-400 focus:outline-none cursor-not-allowed"
            />
            <p className="text-xs text-slate-500 mt-1.5">La API Key de Gemini se gestiona de manera centralizada en el servidor por seguridad.</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SettingsPage;
