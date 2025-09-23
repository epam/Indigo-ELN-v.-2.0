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