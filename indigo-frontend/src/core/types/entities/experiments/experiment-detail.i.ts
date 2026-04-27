import { BaseEntity } from '../base-entity.i';
import { Attachment } from '../attachment.i';
import { ProjectAcl } from '../acl.i';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';
import { ExperimentModel } from '@core/types/entities/experiments/experiment.i';
import { UserMetadata } from '@core/types/entities/user.i';
import { ExperimentRef } from '@core/types/entities/experiments/experiment-shared.i';

export enum SignatureReason {
  AUTHOR = 'AUTHOR',
  WITNESS = 'WITNESS',
}

export enum SignatureStatus {
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

export interface TherapeuticArea {
  id: string;
  name: string;
}

export interface ProjectCode {
  id: string;
  name: string;
}

export interface SignatureUser {
  id: string;
  username: string;
  displayName: string;
}

export interface Signature {
  user: SignatureUser;
  reason: SignatureReason;
  status: SignatureStatus;
  signedAt: string;
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
  batchCreator: UserMetadata;
  linkedExperiments: ExperimentRef[];
  continuedFrom: ExperimentRef[];
  continuedTo: ExperimentRef[];
  attachments?: Attachment[];
  acl?: ProjectAcl[];
  model: ExperimentModel;
  projectName: string;
  notebookName: string;
}
