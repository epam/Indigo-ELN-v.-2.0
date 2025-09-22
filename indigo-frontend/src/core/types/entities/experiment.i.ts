import { BaseEntity } from './base-entity.i';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';

export interface Experiment extends BaseEntity {
  name: string;
  status: ExperimentStatus;
  reactionSchemaUrl?: string;
}
