import type { ACLEntry, Attachment, BaseDTO } from '@/lib/types/common.ts';
import type { ExperimentStatusCounts } from '@/lib/types/experiments.ts';
import type { ApplicationPermission } from '@/lib/types/user.ts';

/** `NOTEBOOK_NAME_LENGTH` in indigo-frontend: a notebook is numbered, never named. */
export const NOTEBOOK_NAME_LENGTH = 8;

/** Mirrors BaseNotebookDTO. */
export interface BaseNotebook extends BaseDTO {
  name: string;
  experimentCount: number;
  experimentCountByStatus: ExperimentStatusCounts;
}

/** Mirrors NotebookDTO — the shape the project's notebook list returns. */
export interface Notebook extends BaseNotebook {
  acl: ACLEntry[];
  aclCount: number;
}

/**
 * NotebookDetailsDTO — the single-notebook response.
 *
 * `projectId`/`projectName` ride along on the payload, which is what lets the breadcrumb
 * name the parent project without a second request: the URL is flat (`/notebooks/{id}`)
 * and never carries the project.
 */
export interface NotebookDetails extends BaseNotebook {
  revision: number;
  description?: string;
  attachments: Attachment[];
  acl: ACLEntry[];
  /** Scoped to VIEW/EDIT/MANAGE_NOTEBOOK_ACCESS/DELETE_NOTEBOOKS by the backend. */
  currentPermissions: ApplicationPermission[];
  projectId: string;
  projectName: string;
}

/** Body of POST /projects/{id}/notebooks. */
export interface NotebookRequest {
  name: string;
  description?: string;
}

/**
 * The PATCH body. As with `ProjectEditRequest`, the backend wraps every field in
 * `JsonNullable` with `@JsonInclude(NON_ABSENT)`, so the three states are distinct over the
 * wire: **absent** leaves the field alone, **null** clears it, a value sets it.
 */
export interface NotebookEditRequest {
  name?: string;
  description?: string | null;
}
