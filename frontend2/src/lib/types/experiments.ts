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
