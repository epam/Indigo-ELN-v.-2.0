import { BaseEntity } from './base-entity.i';

export interface Notebook extends BaseEntity {
  name: string;
  description: string;
  projectId: string;

  [key: string]: any;
}
