import { ExperimentStatus } from '@/core/enums/experiment-status.enum';
import { ExperimentRef, UUID } from '@core/types/entities/experiments/experiment-shared.i';
import { ExperimentModel } from '@core/types/entities/experiments/experiment.i';
import { ACLEntry } from '../acl.i';
import { Attachment } from '../attachment.i';
import { BaseEntity } from '../base-entity.i';
import { DictionaryItemRef } from '../dictionary.i';
import { UserRef } from '../user.i';

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
  marked?: boolean;
  model: ExperimentModel;
  projectId: UUID;
  projectName: string;
  notebookId: UUID;
  notebookName: string;
}
