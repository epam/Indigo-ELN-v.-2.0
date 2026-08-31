import type {ACLEntry, BaseDTO, CollectionFilters} from '@/lib/types/common.ts';

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
