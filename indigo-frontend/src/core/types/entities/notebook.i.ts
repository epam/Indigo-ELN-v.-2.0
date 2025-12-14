import { BaseEntity } from './base-entity.i';

interface NotebookAcl {
  userId: string;
  displayName: string;
  level: string;
  avatarUrl: string;
}

type NotebookAcls = NotebookAcl[];
export interface Notebook extends BaseEntity {
  name: string;
  description: string;
  projectId: string;

  aclCount?: number;
  acl?: NotebookAcls;

  [key: string]: any;
}
