import { ExperimentStatus } from '@/core/enums/experiment-status.enum';
import { BaseEntity } from './base-entity.i';

export interface Project extends BaseEntity {
  name: string;
  notebookCount: number;
  experimentCount: ExperimentCount;
}

export type ExperimentCount = Record<keyof typeof ExperimentStatus, number>;
