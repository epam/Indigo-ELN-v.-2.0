import { ProjectAcl } from './acl.i';
import { Attachment } from './attachment.i';
import { BaseEntity } from './base-entity.i';

export interface NotebookDetail extends BaseEntity {
  name: string;
  description: string;
  experimentCount?: Record<string, number>;
  attachments: Attachment[];
  projectName: string;
  acl: ProjectAcl[];
  projectId: string;
}
