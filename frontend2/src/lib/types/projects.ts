import type {ACLEntry, Attachment, BaseDTO} from '@/lib/types/common.ts';
import type {ExperimentStatusCounts} from '@/lib/types/experiments.ts';
import type {ApplicationPermission} from '@/lib/types/user.ts';

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

/**
 * The PATCH body. `ProjectEditRequest` on the backend wraps every field in `JsonNullable` with
 * `@JsonInclude(NON_ABSENT)`, so the three states are distinct over the wire: **absent** leaves
 * the field alone, **null** clears it, a value sets it.
 */
export interface ProjectEditRequest {
  name?: string;
  keywords?: string[];
  literature?: string | null;
  description?: string | null;
}

export interface TotalCounts {
  projects: number;
  notebooks: number;
  experiments: number;
  experimentsByStatus: ExperimentStatusCounts;
}
