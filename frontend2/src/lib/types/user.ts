export type ApplicationPermission =
  | 'MANAGE_USERS'
  | 'MANAGE_ROLES'
  | 'MANAGE_DICTIONARIES'
  | 'MANAGE_TEMPLATES'
  | 'VIEW_PROJECTS'
  | 'CREATE_PROJECTS'
  | 'EDIT_PROJECTS'
  | 'MANAGE_PROJECT_ACCESS'
  | 'DELETE_PROJECTS'
  | 'VIEW_NOTEBOOKS'
  | 'CREATE_NOTEBOOKS'
  | 'EDIT_NOTEBOOKS'
  | 'MANAGE_NOTEBOOK_ACCESS'
  | 'DELETE_NOTEBOOKS'
  | 'VIEW_EXPERIMENTS'
  | 'CREATE_EXPERIMENTS'
  | 'EDIT_EXPERIMENTS'
  | 'MANAGE_EXPERIMENT_ACCESS'
  | 'DELETE_EXPERIMENTS'
  | 'SUBMIT_EXPERIMENTS'
  | 'SIGN_EXPERIMENTS';

export interface CurrentUser {
  id: string;
  username: string;
  displayName: string;
  permissions: ApplicationPermission[];
}
