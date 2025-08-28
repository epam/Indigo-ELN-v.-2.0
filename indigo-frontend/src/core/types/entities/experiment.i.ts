import { BaseEntity } from './base-entity.i';
import { ProjectAcl } from './acl.i';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';

export interface Experiment extends BaseEntity {
  name: string;
  status: ExperimentStatus;
  marked: boolean;
  acl?: ProjectAcl[];
  aclCount?: number;
}
