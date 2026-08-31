import type { ACLEntry, Attachment, BaseDTO, CollectionFilters, UserRef, UUID } from '@/lib/types/common.ts';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { ExperimentModel } from '@/lib/types/reactions.ts';
import type { ApplicationPermission } from '@/lib/types/user.ts';

/**
 * Display order for every surface that lists the statuses, and the source of truth for the
 * union below — one list rather than a `const` array and a hand-kept union that can drift.
 * A `readonly` tuple is also what `z.enum` needs for the experiments tab's status param.
 */
export const EXPERIMENT_STATUSES = [
  'OPEN',
  'REOPEN',
  'COMPLETED',
  'SIGNING',
  'SUBMITTED',
  'SIGNED',
  'REJECTED',
  'CANCELLED',
  'ARCHIVED',
] as const;

export type ExperimentStatus = (typeof EXPERIMENT_STATUSES)[number];

export type ExperimentStatusCounts = Partial<Record<ExperimentStatus, number>>;

export const EXPERIMENT_STATUS_DISPLAY: Record<ExperimentStatus, string> = {
  OPEN: 'Open',
  REOPEN: 'Reopen',
  COMPLETED: 'Completed',
  SIGNING: 'Signing',
  SUBMITTED: 'Submitted',
  SIGNED: 'Signed',
  REJECTED: 'Rejected',
  CANCELLED: 'Cancelled',
  ARCHIVED: 'Archived',
};

/**
 * Tailwind text colour per status. Statuses that read as near-equivalent share a
 * hue and are told apart by shade. Literal class strings — Tailwind scans source
 * text, so these must never be assembled at runtime.
 */
export const EXPERIMENT_STATUS_COLOR: Record<ExperimentStatus, string> = {
  OPEN: 'text-blue-400',
  REOPEN: 'text-blue-600',
  COMPLETED: 'text-violet-200',
  SIGNING: 'text-orange-200',
  SUBMITTED: 'text-orange-400',
  SIGNED: 'text-violet-400',
  REJECTED: 'text-red-200',
  CANCELLED: 'text-neutral-700',
  ARCHIVED: 'text-green-200',
};

export interface BaseExperiment extends BaseDTO {
  name: string;
  status: ExperimentStatus;
  marked: boolean;
  revision: number;
}

export interface Experiment extends BaseExperiment {
  acl: ACLEntry[];
  aclCount: number;
}

/**
 * What a notebook's experiment list is filtered by: the three `ActionBar` sets, plus the
 * status multi-select that only this list has. `/notebooks/{id}/experiments` is the one
 * collection endpoint declaring a repeatable `status` param, which is why this extends
 * `CollectionFilters` rather than widening it for every list.
 */
export interface ExperimentFilters extends CollectionFilters {
  statuses: ExperimentStatus[];
}

/**
 * The PATCH body. As with `NotebookEditRequest`, the backend wraps every field in
 * `JsonNullable` with `@JsonInclude(NON_ABSENT)`, so the three states are distinct over the
 * wire: **absent** leaves the field alone, **null** clears it, a value sets it.
 *
 * Only the fields the screen edits today are declared; the request carries five more.
 */
export interface ExperimentEditRequest {
  title?: string | null;
  therapeuticArea?: DictionaryItemRef | null;
  projectCode?: DictionaryItemRef | null;
  description?: string | null;
  literature?: string | null;
  /**
   * Sets the whole list. Never null — indigo-frontend sends `[]` to empty one, and the backend
   * takes a `Set`, so order is not meaningful either way.
   */
  linkedExperiments?: ExperimentRef[];
  continuedFrom?: ExperimentRef[];
  continuedTo?: ExperimentRef[];
}

/** Mirrors ExperimentRef — how an experiment names another one it is linked to. */
export interface ExperimentRef {
  id: UUID;
  name: string;
}

/**
 * ExperimentDetailsDTO — the single-experiment response.
 *
 * The four ancestor fields ride along on the payload, which is what lets the breadcrumb name the
 * project and the notebook without two more requests: the URL is flat (`/experiments/{id}`) and
 * carries neither.
 *
 * `templateId` is the other half of the screen — the template it points at says which tabs the
 * experiment has and what is in them.
 */
export interface ExperimentDetails extends BaseExperiment {
  title?: string;
  therapeuticArea?: DictionaryItemRef;
  projectCode?: DictionaryItemRef;
  description?: string;
  literature?: string;
  templateId: UUID;
  batchCreator: UserRef;
  linkedExperiments: ExperimentRef[];
  continuedFrom: ExperimentRef[];
  continuedTo: ExperimentRef[];
  attachments: Attachment[];
  acl: ACLEntry[];
  /** Scoped to VIEW/EDIT/MANAGE_EXPERIMENT_ACCESS/DELETE/SUBMIT/SIGN_EXPERIMENTS by the backend. */
  currentPermissions: ApplicationPermission[];
  model: ExperimentModel;
  projectId: UUID;
  projectName: string;
  notebookId: UUID;
  notebookName: string;
  signatureNumber?: string;
}
