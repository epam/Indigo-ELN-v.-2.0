import { BaseEntity } from '../base-entity.i';
import { Attachment } from '../attachment.i';
import { ACLEntry } from '../acl.i';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';
import { ExperimentModel } from '@core/types/entities/experiments/experiment.i';
import { UserRef } from '@core/types/entities/user.i';
import { ExperimentRef } from '@core/types/entities/experiments/experiment-shared.i';
import { DictionaryItemRef } from '@core/types/entities/dictionary.i';

export interface TherapeuticArea {
  id: string;
  name: string;
}

export interface ProjectCode {
  id: string;
  name: string;
}

export interface ExperimentEditRequest {
  title?: string | null;
  therapeuticArea?: DictionaryItemRef | null;
  projectCode?: DictionaryItemRef | null;
  description?: string | null;
  literature?: string | null;
  linkedExperiments?: ExperimentRef[] | null;
  continuedFrom?: ExperimentRef[] | null;
  continuedTo?: ExperimentRef[] | null;
}

export interface ExperimentDetail extends BaseEntity {
  name: string;
  title?: string;
  status: ExperimentStatus;
  revision: number;
  therapeuticArea?: TherapeuticArea;
  projectCode?: ProjectCode;
  description?: string;
  literature?: string;
  templateId: string;
  batchCreator: UserRef;
  linkedExperiments: ExperimentRef[];
  continuedFrom: ExperimentRef[];
  continuedTo: ExperimentRef[];
  attachments?: Attachment[];
  acl?: ACLEntry[];
  model: ExperimentModel;
  projectName: string;
  notebookName: string;
}
