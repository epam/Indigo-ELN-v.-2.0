export enum ExperimentStatus {
  OPEN = 'OPEN',
  REOPEN = 'REOPEN',
  COMPLETED = 'COMPLETED',
  SUBMITTED = 'SUBMITTED',
  SIGNING = 'SIGNING',
  REJECTED = 'REJECTED',
  SIGNED = 'SIGNED',
  ARCHIVED = 'ARCHIVED',
  CANCELLED = 'CANCELLED',
}

export const ExperimentStatusNames: Record<ExperimentStatus, string> = {
  OPEN: 'Open',
  REOPEN: 'Reopen',
  COMPLETED: 'Completed',
  SUBMITTED: 'Submitted',
  SIGNING: 'Signing',
  REJECTED: 'Rejected',
  SIGNED: 'Signed',
  ARCHIVED: 'Archived',
  CANCELLED: 'Cancelled',
}
