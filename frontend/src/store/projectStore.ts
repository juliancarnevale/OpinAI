import { create } from 'zustand';
import { projectService } from '../features/projects/services/projectService';
import type { Project } from '../features/projects/types/Project';
import type { CreateProjectRequest } from '../features/projects/types/CreateProjectRequest';
import type { UpdateProjectRequest } from '../features/projects/types/UpdateProjectRequest';
import axios from 'axios';

interface ProjectState {
  projects: Project[];
  selectedProject: Project | null;
  isLoading: boolean;
  error: string | null;
  fetchProjects: () => Promise<void>;
  createProject: (data: CreateProjectRequest) => Promise<Project>;
  updateProject: (id: string, data: UpdateProjectRequest) => Promise<Project>;
  deleteProject: (id: string) => Promise<void>;
  selectProject: (project: Project | null) => void;
  clearError: () => void;
}

export const useProjectStore = create<ProjectState>((set) => ({
  projects: [],
  selectedProject: null,
  isLoading: false,
  error: null,

  fetchProjects: async () => {
    set({ isLoading: true, error: null });
    try {
      const projects = await projectService.getProjects();
      set({ projects, isLoading: false });
    } catch (err: unknown) {
      let message = 'Error al obtener la lista de proyectos.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        message = err.response.data.message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      set({ error: message, isLoading: false });
    }
  },

  createProject: async (data) => {
    set({ isLoading: true, error: null });
    try {
      const newProject = await projectService.createProject(data);
      set((state) => ({
        projects: [newProject, ...state.projects],
        isLoading: false,
      }));
      return newProject;
    } catch (err: unknown) {
      let message = 'Error al crear el proyecto.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        message = err.response.data.message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      set({ error: message, isLoading: false });
      throw err;
    }
  },

  updateProject: async (id, data) => {
    set({ isLoading: true, error: null });
    try {
      const updatedProject = await projectService.updateProject(id, data);
      set((state) => ({
        projects: state.projects.map((p) => (p.id === id ? updatedProject : p)),
        selectedProject: state.selectedProject?.id === id ? updatedProject : state.selectedProject,
        isLoading: false,
      }));
      return updatedProject;
    } catch (err: unknown) {
      let message = 'Error al actualizar el proyecto.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        message = err.response.data.message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      set({ error: message, isLoading: false });
      throw err;
    }
  },

  deleteProject: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await projectService.deleteProject(id);
      set((state) => ({
        projects: state.projects.filter((p) => p.id !== id),
        selectedProject: state.selectedProject?.id === id ? null : state.selectedProject,
        isLoading: false,
      }));
    } catch (err: unknown) {
      let message = 'Error al eliminar el proyecto.';
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        message = err.response.data.message;
      } else if (err instanceof Error) {
        message = err.message;
      }
      set({ error: message, isLoading: false });
      throw err;
    }
  },

  selectProject: (project) => {
    set({ selectedProject: project });
  },

  clearError: () => set({ error: null }),
}));
