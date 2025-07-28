import { BaseEntity } from './base-entity.i';
import { ExperimentStatus } from '../../enums/experiment-status.enum';

export interface Experiment extends BaseEntity {
  name: string;
  status: string;
  reactionSchemaUrl?: string;
}
