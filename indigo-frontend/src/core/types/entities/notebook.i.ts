import { BaseEntity } from './base-entity.i';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';

interface NotebookAcl {
  userId: string;
  displayName: string;
  level: string;
  avatarUrl: string;
}

type ExperimentCountByStatus  = Partial<Record<ExperimentStatus, number>>

type NotebookAcls = NotebookAcl[];
export interface Notebook extends BaseEntity {
  name: string;
  description: string;
  projectId: string;

  aclCount?: number;
  acl?: NotebookAcls;
  experimentCount: number;
  experimentCountByStatus: ExperimentCountByStatus;

  [key: string]: any;
}
