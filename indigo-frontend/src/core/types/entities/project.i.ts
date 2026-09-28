import { ExperimentStatus } from '@/core/enums/experiment-status.enum';
import { ACLEntry } from './acl.i';
import { Attachment } from './attachment.i';
import { BaseEntity } from './base-entity.i';
import { PermissionedEntity } from './permission.i';

export interface TeamMember {
  name: string;
  email: string;
  role: string;
  avatar: string;
  inherited?: boolean;
}

export interface Project extends BaseEntity, PermissionedEntity {
  name: string;
  notebookCount: number;
  experimentCount: number;
  experimentCountByStatus: ExperimentCountByStatus;
  acl?: ACLEntry[];
  aclCount?: number;
  keywords?: string[];
  literature?: string;
  description?: string;
  attachments?: Attachment[];
  team?: TeamMember[];
  details?: string;
  references?: string;
}
export type ExperimentCountByStatus = Record<keyof typeof ExperimentStatus, number>;
