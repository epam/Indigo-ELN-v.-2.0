import { BaseEntity } from './base-entity.i';
import { ProjectAcl } from './acl.i';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';
import { Template } from '@core/types/entities/template.i';
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
}

export interface ExperimentData {
  experiment: Experiment;
  template: Template;
  // TODO experiment model
}
