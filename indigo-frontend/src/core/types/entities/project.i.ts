import { BaseEntity } from './base-entity.i';

export interface Project extends BaseEntity {
  name: string;
  notebookCount: number;
  experimentCount: ExperimentCount;
}

export interface ExperimentCount {
  additionalProp1: number;
  additionalProp2: number;
  additionalProp3: number;
  [key: string]: number;
}
