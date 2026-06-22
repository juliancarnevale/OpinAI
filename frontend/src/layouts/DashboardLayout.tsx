import React, { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { ROUTES } from '../config/constants';
import {
  FolderKanban,
  LayoutDashboard,
  Settings,
  LogOut,
  Menu,
  X,
  BarChart3
} from 'lucide-react';

const DashboardLayout: React.FC = () => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate(ROUTES.LOGIN);
  };

  const navItems = [
    { to: ROUTES.DASHBOARD, label: 'Dashboard', icon: LayoutDashboard, end: true },
    { to: ROUTES.PROJECTS, label: 'Proyectos', icon: FolderKanban },
    { to: ROUTES.ANALYTICS, label: 'Analíticas', icon: BarChart3 },
    { to: ROUTES.SETTINGS, label: 'Configuración', icon: Settings },
  ];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col md:flex-row font-sans">
      {/* Botón de Menú Móvil */}
      <div className="md:hidden bg-slate-900 border-b border-slate-800 p-4 flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <BarChart3 className="w-8 h-8 text-violet-500" />
          <span className="font-bold text-xl tracking-wider text-white">OpinAI</span>
        </div>
        <button
          onClick={() => setIsSidebarOpen(!isSidebarOpen)}
          className="text-slate-400 hover:text-white focus:outline-none focus:ring-2 focus:ring-violet-500 rounded p-1"
        >
          {isSidebarOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      {/* Sidebar Navigation */}
      <aside
        className={`fixed inset-y-0 left-0 z-40 w-64 bg-slate-900 border-r border-slate-800 flex flex-col justify-between transform transition-transform duration-300 ease-in-out md:translate-x-0 md:static ${
          isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex flex-col">
          {/* Logo y Encabezado */}
          <div className="p-6 border-b border-slate-800 hidden md:flex items-center space-x-3">
            <BarChart3 className="w-8 h-8 text-violet-500" />
            <span className="font-bold text-xl tracking-wider text-white">OpinAI</span>
          </div>

          {/* Enlaces de Navegación */}
          <nav className="p-4 space-y-2">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.end}
                  onClick={() => setIsSidebarOpen(false)}
                  className={({ isActive }) =>
                    `flex items-center space-x-3 px-4 py-3 rounded-lg text-sm font-medium transition-all duration-200 ${
                      isActive
                        ? 'bg-violet-600/20 text-violet-400 border-l-4 border-violet-500 pl-3'
                        : 'text-slate-400 hover:bg-slate-800/60 hover:text-slate-200'
                    }`
                  }
                >
                  <Icon className="w-5 h-5" />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </nav>
        </div>

        {/* Información de Usuario y Botón de Logout */}
        <div className="p-4 border-t border-slate-800 space-y-4">
          <div className="flex items-center space-x-3 p-2 bg-slate-800/40 rounded-lg">
            <div className="w-10 h-10 rounded-full bg-violet-600 flex items-center justify-center text-white font-semibold shadow-inner">
              {user?.firstName?.charAt(0) || 'U'}
              {user?.lastName?.charAt(0) || ''}
            </div>
            <div className="overflow-hidden">
              <p className="text-sm font-semibold text-white truncate">
                {user?.firstName || 'Usuario'} {user?.lastName || ''}
              </p>
              <p className="text-xs text-slate-400 truncate">{user?.email || ''}</p>
            </div>
          </div>
          
          <button
            onClick={handleLogout}
            className="w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-sm font-medium text-rose-400 hover:bg-rose-950/20 transition-all duration-200"
          >
            <LogOut className="w-5 h-5" />
            <span>Cerrar Sesión</span>
          </button>
        </div>
      </aside>

      {/* Backdrop para móvil */}
      {isSidebarOpen && (
        <div
          onClick={() => setIsSidebarOpen(false)}
          className="fixed inset-0 z-30 bg-black/50 md:hidden md:pointer-events-none md:bg-transparent"
        />
      )}

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col min-w-0 overflow-x-hidden">
        {/* Header Superior del Dashboard (Solo visible en pantalla grande) */}
        <header className="hidden md:flex bg-slate-900/40 backdrop-blur-sm border-b border-slate-800 px-8 py-4 items-center justify-between">
          <div className="flex items-center space-x-2">
            <h2 className="text-lg font-medium text-slate-300">Espacio de Trabajo</h2>
            <span className="text-xs px-2.5 py-0.5 rounded-full bg-violet-500/10 text-violet-400 border border-violet-500/20 font-medium">SaaS Admin</span>
          </div>
          <div className="flex items-center space-x-4">
            <span className="text-sm text-slate-400">Estado de API: <span className="inline-block w-2 h-2 rounded-full bg-emerald-500 ml-1"></span></span>
          </div>
        </header>

        {/* Contenido Dinámico de la Página */}
        <div className="flex-1 p-6 md:p-8 max-w-7xl w-full mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default DashboardLayout;
