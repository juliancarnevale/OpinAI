import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './features/auth/pages/LoginPage';
import RegisterPage from './features/auth/pages/RegisterPage';
import DashboardLayout from './layouts/DashboardLayout';
import DashboardPage from './features/dashboard/DashboardPage';
import AnalyticsPage from './features/dashboard/AnalyticsPage';
import ProjectsPage from './features/projects/ProjectsPage';
import { ProjectAnalysesPage } from './features/analysis/pages/ProjectAnalysesPage';
import { AnalysisDetailPage } from './features/analysis/pages/AnalysisDetailPage';
import ProtectedRoute from './components/ProtectedRoute';
import { useAuthStore } from './store/authStore';
import { ROUTES } from './config/constants';
import SettingsPage from './features/settings/SettingsPage';

const App: React.FC = () => {
  const checkAuth = useAuthStore((state) => state.checkAuth);

  // Verificamos si existe una sesión activa (token JWT) al iniciar la aplicación
  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  return (
    <BrowserRouter>
      <Routes>
        {/* Rutas Públicas */}
        <Route path={ROUTES.LOGIN} element={<LoginPage />} />
        <Route path={ROUTES.REGISTER} element={<RegisterPage />} />

        {/* Rutas Protegidas */}
        <Route
          path={ROUTES.DASHBOARD}
          element={
            <ProtectedRoute>
              <DashboardLayout />
            </ProtectedRoute>
          }
        >
          {/* Index de /dashboard (DashboardPage) */}
          <Route index element={<DashboardPage />} />
          
          {/* Proyectos CRUD */}
          <Route path="projects" element={<ProjectsPage />} />

          {/* Analíticas Avanzadas */}
          <Route path="analytics" element={<AnalyticsPage />} />
          
          {/* Lista de análisis de un proyecto */}
          <Route path="projects/:projectId/analyses" element={<ProjectAnalysesPage />} />

          {/* Detalle de un análisis */}
          <Route path="analyses/:analysisId" element={<AnalysisDetailPage />} />
          
          <Route path="settings" element={<SettingsPage />} />
        </Route>

        {/* Redirección por defecto */}
        <Route path="/" element={<Navigate to={ROUTES.DASHBOARD} replace />} />
        <Route path="*" element={<Navigate to={ROUTES.DASHBOARD} replace />} />
      </Routes>
    </BrowserRouter>
  );
};

export default App;
