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

export const EXPERIMENT_STATUS_LABELS: Record<ExperimentStatus, string> = {
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

/**
 * The two statuses in which an experiment's content can still be changed. Every other status
 * is a stage of the signing workflow or an end state, and the backend rejects edits there.
 */
const EDITABLE_STATUSES: readonly ExperimentStatus[] = ['OPEN', 'REOPEN'];

/**
 * The workflow transitions, keyed by the URL segment each one posts to — so the action *is* the
 * endpoint (`POST /experiments/{id}/workflow/{action}`), and there is no second table mapping one
 * to the other.
 *
 * The status lists are `ExperimentWorkflowHandlers.doValidateStatus` verbatim. Two of them are
 * worth stating outright, because indigo-frontend gets them wrong:
 *
 * - **Reopen excludes `SIGNING` and `SIGNED`.** `ReopenExperimentHandler` allows only the five
 *   listed here; offering it on the other two guarantees a 400. (`SIGNED` barely exists anyway —
 *   the signature handler sets it and then `ARCHIVED` in the same breath.)
 * - **Submit covers `REJECTED` as well as `COMPLETED`.** Resubmitting after a rejection is the
 *   same endpoint and the same handler, not a separate action.
 *
 * `SIGNING` and `SIGNED` therefore permit nothing at all: an experiment out for signature is the
 * signature service's to move, not the browser's.
 */
const WORKFLOW_ACTION_STATUSES = {
  complete: ['OPEN', 'REOPEN'],
  completeAndSubmit: ['OPEN', 'REOPEN'],
  cancel: ['OPEN', 'REOPEN'],
  submit: ['COMPLETED', 'REJECTED'],
  reopen: ['COMPLETED', 'SUBMITTED', 'REJECTED', 'CANCELLED', 'ARCHIVED'],
} as const satisfies Record<string, readonly ExperimentStatus[]>;

/** The URL segment of each transition, derived from the table rather than hand-kept beside it. */
export type WorkflowAction = keyof typeof WORKFLOW_ACTION_STATUSES;

/** Declaration order is display order, so the header reads the same on every status. */
export const WORKFLOW_ACTIONS = Object.keys(WORKFLOW_ACTION_STATUSES) as WorkflowAction[];

/**
 * Which transitions this experiment's **status** allows — what the header renders.
 *
 * Visibility and enablement are separate gates here, mirroring the two the backend applies in
 * order: `doValidateStatus` decides whether an action exists at all, `doValidateAccess` decides
 * whether this user may take it. See `canRunWorkflow` for the second.
 */
export function workflowActionsFor(experiment: Pick<ExperimentDetails, 'status'>): WorkflowAction[] {
  return WORKFLOW_ACTIONS.filter((action) =>
    (WORKFLOW_ACTION_STATUSES[action] as readonly ExperimentStatus[]).includes(experiment.status),
  );
}

/**
 * Whether the current user may move this experiment through the workflow at all — the permission
 * half of the gate, status-independent.
 *
 * All five transitions check the same one permission (`aclService.ensureAccess(entity,
 * SUBMIT_EXPERIMENTS)` in every handler), so there is nothing per-action to ask.
 *
 * **Deliberately not `canEditExperiment`.** They are different users: `AccessLevel.EDIT` grants
 * `EDIT_EXPERIMENTS` but *not* `SUBMIT_EXPERIMENTS`, which only `ADMIN`/`AUTHOR` or a global role
 * carries. A collaborator who can fill in the stoichiometry table may well not be able to
 * complete the experiment.
 */
export function canRunWorkflow(experiment: Pick<ExperimentDetails, 'currentPermissions'>): boolean {
  return experiment.currentPermissions.includes('SUBMIT_EXPERIMENTS');
}

/** Mirrors the `SignatureTemplateRef` record — what the submit dialog picks from. */
export interface SignatureTemplateRef {
  id: UUID;
  name: string;
}

/**
 * Whether the current user may edit this experiment's content — **permission and status**,
 * which are two separate gates and are easy to conflate. Holding `EDIT_EXPERIMENTS` on a
 * signed experiment does not make it writable, and a completed one is read-only to its own
 * author.
 *
 * Every editable surface should ask this rather than testing `currentPermissions` alone.
 */
export function canEditExperiment(experiment: Pick<ExperimentDetails, 'currentPermissions' | 'status'>): boolean {
  return experiment.currentPermissions.includes('EDIT_EXPERIMENTS') && EDITABLE_STATUSES.includes(experiment.status);
}

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
 * Body of POST /notebooks/{id}/experiments.
 *
 * `templateID` is the only required field, and the only one Add Experiment sends — everything
 * else about a new experiment is either edited afterwards on its own page or assigned by the
 * server (the name is `<notebook name>-0001`, from `CreateExperimentHandler`).
 *
 * The capital `ID` is not a typo: Lombok's getter for `templateID` lowercases only the first
 * character, so `templateID` is what Jackson names on the wire.
 */
export interface ExperimentRequest {
  templateID: UUID;
  description?: string;
  therapeuticArea?: DictionaryItemRef;
  projectCode?: DictionaryItemRef;
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
  /**
   * Scoped to VIEW/EDIT/MANAGE_EXPERIMENT_ACCESS/DELETE/SUBMIT_EXPERIMENTS by the backend —
   * the `retainAll` in `ExperimentService.getExperimentDetails`. `SIGN_EXPERIMENTS` is *not*
   * among them, and is never checked anywhere in the Java: who may sign is decided per
   * signature block inside the signature service, not by a permission.
   */
  currentPermissions: ApplicationPermission[];
  model: ExperimentModel;
  projectId: UUID;
  projectName: string;
  notebookId: UUID;
  notebookName: string;
  signatureNumber?: string;
}
