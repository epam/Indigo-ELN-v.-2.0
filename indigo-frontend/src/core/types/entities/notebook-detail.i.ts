import { UUID } from '@core/types/entities/experiments/experiment-shared.i';
import { ACLEntry } from './acl.i';
import { Attachment } from './attachment.i';
import { BaseEntity } from './base-entity.i';

export interface NotebookDetail extends BaseEntity {
  name: string;
  description: string;
  experimentCount?: Record<string, number>;
  attachments: Attachment[];
  projectId: UUID;
  projectName: string;
  acl: ACLEntry[];
}
