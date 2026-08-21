import type { ACLEntry, BaseDTO } from '@/lib/types/common.ts';

export type ExperimentStatus =
  'OPEN' | 'REOPEN' | 'COMPLETED' | 'SIGNING' | 'SUBMITTED' | 'SIGNED' | 'REJECTED' | 'CANCELLED' | 'ARCHIVED';

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

/** Display order for every surface that lists the statuses. */
export const EXPERIMENT_STATUSES: readonly ExperimentStatus[] = [
  'OPEN',
  'REOPEN',
  'COMPLETED',
  'SIGNING',
  'SUBMITTED',
  'SIGNED',
  'REJECTED',
  'CANCELLED',
  'ARCHIVED',
];

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
