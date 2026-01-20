import { UUID } from '@core/types/entities/experiments/experiment-shared.i';

export enum ApplicationPermission {
  // system
  MANAGE_USERS = 'MANAGE_USERS',
  MANAGE_ROLES = 'MANAGE_ROLES',
  MANAGE_DICTIONARIES = 'MANAGE_DICTIONARIES',
  MANAGE_TEMPLATES = 'MANAGE_TEMPLATES',

  // projects
  VIEW_PROJECTS = 'VIEW_PROJECTS',
  CREATE_PROJECTS = 'CREATE_PROJECTS',
  EDIT_PROJECTS = 'EDIT_PROJECTS',
  MANAGE_PROJECT_ACCESS = 'MANAGE_PROJECT_ACCESS',
  DELETE_PROJECTS = 'DELETE_PROJECTS',

  // notebooks
  VIEW_NOTEBOOKS = 'VIEW_NOTEBOOKS',
  CREATE_NOTEBOOKS = 'CREATE_NOTEBOOKS',
  EDIT_NOTEBOOKS = 'EDIT_NOTEBOOKS',
  MANAGE_NOTEBOOK_ACCESS = 'MANAGE_NOTEBOOK_ACCESS',
  DELETE_NOTEBOOKS = 'DELETE_NOTEBOOKS',

  // experiments
  VIEW_EXPERIMENTS = 'VIEW_EXPERIMENTS',
  CREATE_EXPERIMENTS = 'CREATE_EXPERIMENTS',
  EDIT_EXPERIMENTS = 'EDIT_EXPERIMENTS',
  MANAGE_EXPERIMENT_ACCESS = 'MANAGE_EXPERIMENT_ACCESS',
  DELETE_EXPERIMENTS = 'DELETE_EXPERIMENTS',
  SUBMIT_EXPERIMENTS = 'SUBMIT_EXPERIMENTS',
}

export interface UsersResponse {
  pageNo: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  items: User[];
}

export interface CurrentUser {
  id: UUID;
  username: string;
  displayName: string;
  permissions: ApplicationPermission[];
}

export interface User {
  id: string;
  createdBy: UserMetadata;
  createdAt: string;
  modifiedBy: UserMetadata;
  modifiedAt: string;
  username: string;
  firstName: string | null;
  lastName: string | null;
  displayName: string;
  roles: Role[];
}

export interface UserMetadata {
  id: string;
  username: string;
  displayName: string;
}

export interface Role {
  id: string;
  name: string;
}
