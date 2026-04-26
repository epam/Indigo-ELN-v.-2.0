import { BaseEntity } from './base-entity.i';
import { Attachment } from './attachment.i';
import { ProjectAcl } from './acl.i';

export interface NotebookDetail extends BaseEntity {
  name: string;
  description: string;
  experimentCount?: Record<string, number>;
  attachments: Attachment[];
  projectName: string;
  acl: ProjectAcl[];
}
