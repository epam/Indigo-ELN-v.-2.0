import { ExperimentStatus } from '@/core/enums/experiment-status.enum';
import { BaseEntity } from './base-entity.i';
import { Attachment } from './attachment.i';
import { ProjectAcl } from './acl.i';

export interface TeamMember {
  name: string;
  email: string;
  role: string;
  avatar: string;
  userId?: string;
  inherited?: boolean;
}

export interface Template extends BaseEntity {
  name: string;
  notebookCount: number;
  experimentCount: ExperimentCount;
  acl?: ProjectAcl[];
  aclCount?: number;
  keywords?: string[];
  literature?: string;
  description?: string;
  attachments?: Attachment[];
  team?: TeamMember[];
  details?: string;
  references?: string;
}
export type ExperimentCount = Record<keyof typeof ExperimentStatus, number>;
