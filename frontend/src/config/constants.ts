export const CONSTANTS = {
  APP_NAME: 'OpinAI',
  TOKEN_STORAGE_KEY: 'opinai_jwt_token',
};

export const ROUTES = {
  LOGIN: '/login',
  REGISTER: '/register',
  DASHBOARD: '/dashboard',
  PROJECTS: '/dashboard/projects',
  ANALYTICS: '/dashboard/analytics',
  SETTINGS: '/dashboard/settings',
  PROJECT_ANALYSES: (projectId: string) => `/dashboard/projects/${projectId}/analyses`,
  ANALYSIS_DETAIL: (analysisId: string) => `/dashboard/analyses/${analysisId}`,
} as const;
