import { BaseEntity } from './base-entity.i';

export interface DictionaryListItem extends BaseEntity {
  name: string;
  description: string;
  code: string;
}

export type DictionaryList = DictionaryListItem[];

export interface DictionaryFullItem {
  id: string;
  createdAt: Date;
  name: string;
  description: string;
  ordinal: number;
  active: boolean;
}

export type DictionaryFull = DictionaryFullItem[];

export interface DictionaryItemRef {
  id: string;
  name: string;
}

export interface SaltCodeRef {
  id: string;
  code: string;
  name: string;
  formula: string;
  charge: number;
  molWeight: number;
}

export enum BuiltInDictionary {
  THERAPEUTIC_AREA = 'THERAPEUTIC_AREA',
  PROJECT_CODE = 'PROJECT_CODE',
  PROJECT_KEYWORD = 'PROJECT_KEYWORD',
  STEREOISOMER_CODE = 'STEREOISOMER_CODE',
  HEALTH_HAZARD = 'HEALTH_HAZARD',
  HANDLING_PRECAUTIONS = 'HANDLING_PRECAUTIONS',
  STORAGE_INSTRUCTIONS = 'STORAGE_INSTRUCTIONS',
  COMPOUND_PROTECTION = 'COMPOUND_PROTECTION',
  SOLVENT = 'SOLVENT',
  EXTERNAL_SUPPLIER = 'EXTERNAL_SUPPLIER',
  SAMPLE_SOURCE = 'SAMPLE_SOURCE',
  SAMPLE_SOURCE_DETAILS = 'SAMPLE_SOURCE_DETAILS',
  COMPONENT_STATE = 'COMPONENT_STATE',
}
