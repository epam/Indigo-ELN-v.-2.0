import { BaseEntity } from './base-entity.i';
import { ExperimentCountByStatus } from './project.i';
import { ACLEntry } from '@core/types/entities/acl.i';
import { PermissionedEntity } from './permission.i';

export interface Notebook extends BaseEntity, PermissionedEntity {
  name: string;
  description: string;
  projectId: string;

  aclCount?: number;
  acl?: ACLEntry[];
  experimentCount: number;
  experimentCountByStatus: ExperimentCountByStatus;

  [key: string]: any;
}
