import type { ExperimentStatus, ExperimentStatusCounts } from '@/lib/types/experiments.ts';

export type AccessLevel = 'NONE' | 'IMPLICIT_VIEW' | 'VIEW' | 'EDIT' | 'ADMIN' | 'AUTHOR';

export interface UserRef {
  username: string;
  displayName: string;
}

export interface ACLEntry {
  username: string;
  displayName: string;
  level: AccessLevel;
  inherited: boolean;
}

export type UUID = string;

export interface BaseDTO {
  id: UUID;
  createdBy: UserRef;
  createdAt: string;
  modifiedBy: UserRef;
  modifiedAt: string;
}

export interface Page<T> {
  pageNo: number;
  pageSize: number;
  totalItems: number;
  totalPages: number;
  items: T[];
}

/**
 * The design shows six status cells while the API reports nine statuses, so the
 * near-equivalent ones are summed into a single cell.
 */
export type StatusGroupKey = 'open' | 'completed' | 'signing' | 'rejected' | 'cancelled' | 'archived';

export interface StatusGroup {
  key: StatusGroupKey;
  label: string;
  statuses: ExperimentStatus[];
  /** Tailwind text colour utility for the count. */
  colorClass: string;
}

export const STATUS_GROUPS: StatusGroup[] = [
  { key: 'open', label: 'Open', statuses: ['OPEN', 'REOPEN'], colorClass: 'text-blue-400' },
  {
    key: 'completed',
    label: 'Completed',
    statuses: ['COMPLETED', 'SIGNED'],
    colorClass: 'text-violet-200',
  },
  {
    key: 'signing',
    label: 'Signing',
    statuses: ['SIGNING', 'SUBMITTED'],
    colorClass: 'text-orange-200',
  },
  { key: 'rejected', label: 'Rejected', statuses: ['REJECTED'], colorClass: 'text-red-200' },
  { key: 'cancelled', label: 'Canceled', statuses: ['CANCELLED'], colorClass: 'text-neutral-700' },
  { key: 'archived', label: 'Archived', statuses: ['ARCHIVED'], colorClass: 'text-green-200' },
];

/** Every status belongs to exactly one group — asserted by eln-status.test.ts. */
export function statusGroupOf(status: ExperimentStatus): StatusGroup {
  return STATUS_GROUPS.find((group) => group.statuses.includes(status))!;
}

export function sumStatuses(counts: ExperimentStatusCounts | undefined, statuses: ExperimentStatus[]): number {
  if (!counts) return 0;
  return statuses.reduce((total, status) => total + (counts[status] ?? 0), 0);
}
