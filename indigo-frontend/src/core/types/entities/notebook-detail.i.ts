import { BaseEntity } from './base-entity.i';
import { Attachment } from './attachment.i';
import { ACLEntry } from './acl.i';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';

export interface NotebookDetail extends BaseEntity {
  name: string;
  description: string;
  experimentCount?: Record<string, number>;
  attachments: Attachment[];
  projectId: UUID;
  projectName: string;
  acl: ACLEntry[];
}
