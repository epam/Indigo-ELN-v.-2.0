import { BaseEntity } from '../base-entity.i';
import { Attachment } from '../attachment.i';
import { ProjectAcl } from '../acl.i';
import { ExperimentStatus } from '@/core/enums/experiment-status.enum';

export enum SignatureReason {
  AUTHOR = 'AUTHOR',
  WITNESS = 'WITNESS',
}

export enum SignatureStatus {
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
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
  status: ExperimentStatus;
  marked: boolean;
  therapeuticArea?: TherapeuticArea;
  projectCode?: ProjectCode;
  description?: string;
  templateId?: string;
  attachments?: Attachment[];
  acl?: ProjectAcl[];
  signatures?: Signature[];
}