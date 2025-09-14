import { BaseEntity } from './base-entity.i';
import { ProjectAcl } from './acl.i';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';
import { DictionaryItemRef } from '@core/types/entities/dictionary.i';

export interface Experiment extends BaseEntity {
  name: string;
  status: ExperimentStatus;
  reactionSchemaUrl?: string;
  marked: boolean;
  acl?: ProjectAcl[];
  aclCount?: number;
  description: string | null;
  therapeuticArea: DictionaryItemRef | null;
  projectCode: DictionaryItemRef | null;
  templateId?: string;
}
