import type { ACLEntry, Attachment, BaseDTO, SortOrder } from '@/lib/types/common.ts';
import type { ExperimentStatusCounts } from '@/lib/types/experiments.ts';
import type { ApplicationPermission } from '@/lib/types/user.ts';

export interface BaseProject extends BaseDTO {
  name: string;
  notebookCount: number;
  experimentCount: number;
  experimentCountByStatus: ExperimentStatusCounts;
}

export interface Project extends BaseProject {
  acl: ACLEntry[];
  aclCount: number;
}

/** Body of POST /projects. Optional fields are omitted rather than sent empty. */
export interface ProjectRequest {
  name: string;
  keywords?: string[];
  literature?: string;
  description?: string;
}

/** ProjectDetailsDTO — the single-project response, richer than the list's Project. */
export interface ProjectDetails extends BaseProject {
  revision: number;
  keywords: string[];
  literature?: string;
  description?: string;
  attachments: Attachment[];
  acl: ACLEntry[];
  /** Scoped to VIEW/EDIT/MANAGE_ACCESS/DELETE_PROJECTS by the backend. */
  currentPermissions: ApplicationPermission[];
}

export interface ProjectFilters {
  search: string;
  sort: SortOrder;
  createdByMe: boolean;
}

export interface TotalCounts {
  projects: number;
  notebooks: number;
  experiments: number;
  experimentsByStatus: ExperimentStatusCounts;
}
