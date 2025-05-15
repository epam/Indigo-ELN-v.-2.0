import { BaseEntity } from './base-entity.i';

export interface Attachment extends BaseEntity {
  name: string;
  size: number;
}
