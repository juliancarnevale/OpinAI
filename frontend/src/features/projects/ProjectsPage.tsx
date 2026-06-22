import React, { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { ROUTES } from '../../config/constants';
import { useProjectStore } from '../../store/projectStore';
import {
  Plus,
  Folder,
  Edit2,
  Trash2,
  Loader2,
  X,
  AlertCircle,
  FolderKanban,
  CheckCircle2,
  BarChart3
} from 'lucide-react';
import type { Project } from './types/Project';

const ProjectsPage: React.FC = () => {
  const {
    projects,
    isLoading,
    error,
    fetchProjects,
    createProject,
    updateProject,
    deleteProject,
    clearError
  } = useProjectStore();

  // Estados locales para la UI
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<'create' | 'edit'>('create');
  const [editingProjectId, setEditingProjectId] = useState<string | null>(null);
  const [formValues, setFormValues] = useState({ name: '', description: '' });
  const [formValidationErrors, setFormValidationErrors] = useState<{ name?: string }>({});

  // Confirmación de borrado
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const location = useLocation();

  useEffect(() => {
    fetchProjects();
  }, [fetchProjects]);

  // Auto-open creation form if redirected from Dashboard empty state
  useEffect(() => {
    if (location.state?.openCreateForm) {
      setIsFormOpen(true);
      setFormMode('create');
      setFormValues({ name: '', description: '' });
      // Clear location state to avoid reopen on page refresh
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  // Limpiar mensajes de éxito automáticamente
  useEffect(() => {
    if (successMessage) {
      const timer = setTimeout(() => setSuccessMessage(null), 4000);
      return () => clearTimeout(timer);
    }
  }, [successMessage]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormValues((prev) => ({ ...prev, [name]: value }));
    // Limpiar error de validación cuando el usuario escribe
    if (name === 'name' && value.trim()) {
      setFormValidationErrors((prev) => ({ ...prev, name: undefined }));
    }
  };

  const validateForm = () => {
    const errors: { name?: string } = {};
    if (!formValues.name.trim()) {
      errors.name = 'El nombre del proyecto es obligatorio';
    } else if (formValues.name.length > 100) {
      errors.name = 'El nombre no puede exceder los 100 caracteres';
    }
    setFormValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) return;

    try {
      if (formMode === 'create') {
        await createProject({
          name: formValues.name.trim(),
          description: formValues.description.trim() || undefined,
        });
        setSuccessMessage('Proyecto creado correctamente.');
      } else if (formMode === 'edit' && editingProjectId) {
        await updateProject(editingProjectId, {
          name: formValues.name.trim(),
          description: formValues.description.trim() || undefined,
        });
        setSuccessMessage('Proyecto actualizado correctamente.');
      }
      handleCloseForm();
    } catch (err) {
      // El error ya se gestiona en el store de Zustand
    }
  };

  const handleOpenCreate = () => {
    setFormMode('create');
    setEditingProjectId(null);
    setFormValues({ name: '', description: '' });
    setFormValidationErrors({});
    setIsFormOpen(true);
    // Limpiar confirmaciones de borrado activas
    setConfirmDeleteId(null);
  };

  const handleOpenEdit = (project: Project) => {
    setFormMode('edit');
    setEditingProjectId(project.id);
    setFormValues({
      name: project.name,
      description: project.description || '',
    });
    setFormValidationErrors({});
    setIsFormOpen(true);
    setConfirmDeleteId(null);
  };

  const handleCloseForm = () => {
    setIsFormOpen(false);
    setFormValues({ name: '', description: '' });
    setFormValidationErrors({});
    setEditingProjectId(null);
  };

  const handleDelete = async (id: string) => {
    setIsDeleting(true);
    try {
      await deleteProject(id);
      setSuccessMessage('Proyecto eliminado correctamente.');
      setConfirmDeleteId(null);
    } catch (err) {
      // Gestionado en store
    } finally {
      setIsDeleting(false);
    }
  };

  const formatDate = (dateString: string) => {
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('es-ES', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      });
    } catch (e) {
      return dateString;
    }
  };

  return (
    <div className="space-y-6">
      {/* Encabezado y Acciones */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white flex items-center space-x-2">
            <FolderKanban className="w-8 h-8 text-violet-500 mr-2" />
            <span>Proyectos</span>
          </h1>
          <p className="text-slate-400 mt-1">Administra tus espacios de trabajo de opiniones y comentarios.</p>
        </div>
        {!isFormOpen && projects.length > 0 && (
          <button
            onClick={handleOpenCreate}
            className="inline-flex items-center space-x-2 px-4 py-2.5 bg-violet-600 hover:bg-violet-700 text-white font-medium rounded-lg text-sm transition duration-200 shadow-lg shadow-violet-600/20 cursor-pointer"
          >
            <Plus className="w-5 h-5" />
            <span>Crear Proyecto</span>
          </button>
        )}
      </div>

      {/* Alerta de Error de Zustand */}
      {error && (
        <div className="bg-rose-950/40 border border-rose-800 text-rose-200 p-4 rounded-xl flex items-start justify-between">
          <div className="flex space-x-3">
            <AlertCircle className="w-5 h-5 text-rose-500 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-sm">Ocurrió un error</p>
              <p className="text-xs text-rose-300 mt-1">{error}</p>
            </div>
          </div>
          <button onClick={clearError} className="text-rose-400 hover:text-rose-200">
            <X className="w-5 h-5" />
          </button>
        </div>
      )}

      {/* Alerta de Éxito */}
      {successMessage && (
        <div className="bg-emerald-950/40 border border-emerald-800 text-emerald-200 p-4 rounded-xl flex items-start justify-between">
          <div className="flex space-x-3">
            <CheckCircle2 className="w-5 h-5 text-emerald-500 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-sm">Operación exitosa</p>
              <p className="text-xs text-emerald-300 mt-1">{successMessage}</p>
            </div>
          </div>
          <button onClick={() => setSuccessMessage(null)} className="text-emerald-400 hover:text-emerald-200">
            <X className="w-5 h-5" />
          </button>
        </div>
      )}

      {/* Formulario de Creación/Edición Inline */}
      {isFormOpen && (
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
          <div className="flex justify-between items-center pb-3 border-b border-slate-800">
            <h2 className="text-lg font-bold text-white">
              {formMode === 'create' ? 'Crear Nuevo Proyecto' : 'Editar Proyecto'}
            </h2>
            <button
              onClick={handleCloseForm}
              className="text-slate-400 hover:text-white p-1 hover:bg-slate-800 rounded-lg transition"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <label htmlFor="name" className="block text-sm font-medium text-slate-300">
                Nombre del Proyecto <span className="text-violet-400">*</span>
              </label>
              <input
                type="text"
                id="name"
                name="name"
                value={formValues.name}
                onChange={handleInputChange}
                className={`w-full bg-slate-950 border ${
                  formValidationErrors.name ? 'border-rose-500 focus:ring-rose-500' : 'border-slate-800 focus:ring-violet-500'
                } rounded-lg px-4 py-2.5 text-white text-sm focus:outline-none focus:ring-2`}
                placeholder="Ej. Ecommerce España, Soporte Móvil..."
                maxLength={100}
              />
              {formValidationErrors.name && (
                <p className="text-xs text-rose-400 flex items-center mt-1">
                  <AlertCircle className="w-3.5 h-3.5 mr-1" />
                  {formValidationErrors.name}
                </p>
              )}
            </div>

            <div className="space-y-2">
              <label htmlFor="description" className="block text-sm font-medium text-slate-300">
                Descripción <span className="text-slate-500">(Opcional)</span>
              </label>
              <textarea
                id="description"
                name="description"
                value={formValues.description}
                onChange={handleInputChange}
                rows={3}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-4 py-2.5 text-white text-sm focus:outline-none focus:ring-2 focus:ring-violet-500"
                placeholder="Escribe brevemente el propósito de este proyecto..."
                maxLength={1000}
              />
              <div className="flex justify-end text-xs text-slate-500">
                {formValues.description.length}/1000 caracteres
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-2">
              <button
                type="button"
                onClick={handleCloseForm}
                className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 font-medium rounded-lg text-sm transition cursor-pointer"
                disabled={isLoading}
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="inline-flex items-center space-x-2 px-4 py-2 bg-violet-600 hover:bg-violet-700 text-white font-medium rounded-lg text-sm transition shadow-lg shadow-violet-600/20 cursor-pointer"
                disabled={isLoading}
              >
                {isLoading && <Loader2 className="w-4 h-4 animate-spin" />}
                <span>{formMode === 'create' ? 'Crear' : 'Guardar Cambios'}</span>
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Lista de Proyectos u otros estados */}
      {isLoading && projects.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-20 space-y-4">
          <Loader2 className="w-10 h-10 text-violet-500 animate-spin" />
          <p className="text-slate-400 text-sm">Cargando tus proyectos...</p>
        </div>
      ) : projects.length === 0 ? (
        /* Empty State Profesional */
        <div className="flex flex-col items-center justify-center border-2 border-dashed border-slate-800 rounded-2xl py-16 px-4 text-center max-w-2xl mx-auto my-8">
          <div className="w-16 h-16 rounded-full bg-violet-500/10 flex items-center justify-center text-violet-500 mb-6 animate-pulse">
            <Folder className="w-8 h-8" />
          </div>
          <h3 className="text-xl font-bold text-white mb-2">No tienes proyectos todavía</h3>
          <p className="text-slate-400 max-w-sm mb-8">
            Crea tu primer proyecto para comenzar a analizar opiniones.
          </p>
          {!isFormOpen && (
            <button
              onClick={handleOpenCreate}
              className="inline-flex items-center space-x-2 px-6 py-3 bg-violet-600 hover:bg-violet-700 text-white font-medium rounded-lg text-sm transition shadow-lg shadow-violet-600/20 cursor-pointer"
            >
              <Plus className="w-5 h-5" />
              <span>Crear Proyecto</span>
            </button>
          )}
        </div>
      ) : (
        /* Grid de Proyectos */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {projects.map((project) => {
            const isDeletingThis = confirmDeleteId === project.id;
            return (
              <div
                key={project.id}
                className="bg-slate-900 border border-slate-800 rounded-xl p-6 hover:border-slate-700 transition duration-300 flex flex-col justify-between"
              >
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3 text-violet-500">
                      <Folder className="w-6 h-6 shrink-0" />
                      <h3 className="text-lg font-semibold text-white truncate max-w-[170px] sm:max-w-[200px]" title={project.name}>
                        {project.name}
                      </h3>
                    </div>
                  </div>
                  <p className="text-sm text-slate-400 line-clamp-3 min-h-[60px]">
                    {project.description || <span className="text-slate-600 italic">Sin descripción.</span>}
                  </p>
                </div>

                <div className="mt-6 pt-4 border-t border-slate-800/80 space-y-4">
                  {/* Fecha de Creación */}
                  <div className="flex items-center justify-between text-xs text-slate-500">
                    <span>Creado: {formatDate(project.createdAt)}</span>
                  </div>

                  {/* Acciones del Proyecto */}
                  <div className="flex items-center justify-end space-x-2">
                    {isDeletingThis ? (
                      /* Flujo de Confirmación de Borrado */
                      <div className="flex items-center space-x-2 bg-slate-950 p-1.5 rounded-lg border border-rose-950">
                        <span className="text-xs text-rose-400 px-2 font-medium">¿Confirmar?</span>
                        <button
                          onClick={() => handleDelete(project.id)}
                          disabled={isDeleting}
                          className="px-2 py-1 bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold rounded transition cursor-pointer"
                        >
                          {isDeleting ? '...' : 'Sí'}
                        </button>
                        <button
                          onClick={() => setConfirmDeleteId(null)}
                          disabled={isDeleting}
                          className="px-2 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded transition cursor-pointer"
                        >
                          No
                        </button>
                      </div>
                    ) : (
                      <>
                        <Link
                          to={ROUTES.PROJECT_ANALYSES(project.id)}
                          className="p-2 text-slate-400 hover:text-violet-400 hover:bg-slate-800 rounded-lg transition mr-auto flex items-center space-x-1"
                          title="Ver análisis"
                        >
                          <BarChart3 className="w-4 h-4" />
                          <span className="text-xs font-semibold hidden sm:inline">Análisis</span>
                        </Link>
                        <button
                          onClick={() => handleOpenEdit(project)}
                          className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-lg transition"
                          title="Editar proyecto"
                        >
                          <Edit2 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => setConfirmDeleteId(project.id)}
                          className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-950/20 rounded-lg transition"
                          title="Eliminar proyecto"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </>
                    )}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default ProjectsPage;
