export const BREADCRUMB_LABELS = {
  allProjects: 'All Projects',
  project: 'Project',
  notebook: 'Notebook',
  experiment: 'Experiment',
} as const;

export interface BreadcrumbItem {
  label: string;
  url?: string;
  active: boolean;
}

export interface RouteParams {
  projectId?: string;
  notebookId?: string;
  experimentId?: string;
}

export enum BreadcrumbType {
  Root = 'root',
  Project = 'project',
  Notebook = 'notebook',
  Experiment = 'experiment',
}