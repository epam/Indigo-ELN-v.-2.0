import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';

export enum StructuralSearchType {
  EXACT = 'EXACT',
  SUBSTRUCTURE = 'SUBSTRUCTURE',
  SIMILARITY = 'SIMILARITY',
}

export interface StructuralSearch {
  type: StructuralSearchType;
  query: string;
}

export interface TextSearchExact {
  type: 'exact';
  value: string;
}

export interface TextSearchStartsWith {
  type: 'startsWith';
  value: string;
}

export interface TextSearchContains {
  type: 'contains';
  value: string;
}

export interface TextSearchEndsWith {
  type: 'endsWith';
  value: string;
}

export interface TextSearchBetween {
  type: 'between';
  from: string;
  to: string;
}

export type TextSearch =
  | TextSearchExact
  | TextSearchStartsWith
  | TextSearchEndsWith
  | TextSearchContains
  | TextSearchBetween;

export interface NumericSearchEquals {
  type: 'eq';
  value: number;
}

export interface NumericSearchLessThanOrEquals {
  type: 'le';
  value: number;
}

export interface NumericSearchGreaterThanOrEquals {
  type: 'ge';
  value: number;
}

export type NumericSearch =
  | NumericSearchEquals
  | NumericSearchLessThanOrEquals
  | NumericSearchGreaterThanOrEquals;

export interface FindSamplesRequest {
  quickSearch?: string;
  structure?: StructuralSearch;
  strCode?: TextSearch;
  nbkBatchNumber?: TextSearch;
  casNumber?: TextSearch;
  externalNumber?: TextSearch;
  molecularFormula?: TextSearch;
  molWeight?: NumericSearch;
  chemicalName?: TextSearch;
  compoundState?: DictionaryItemRef;
  batchComment?: TextSearch;
  healthHazards?: DictionaryItemRef;
}

export const TextSearchTypeNames = {
  exact: 'exact',
  startsWith: 'starts with',
  contains: 'contains',
  endsWith: 'ends with',
  between: 'between',
};

export const NumericSearchTypeNames = {
  eq: '=',
  le: '≤',
  ge: '≥',
};

export interface FindSamplesResult {
  id: UUID;
  strCode?: string;
  nbkBatchNumber?: string;
  molecularFormula?: string;
  molWeight: number;
  name?: string;
  saltCode?: DictionaryItemRef;
  saltEQ?: number;
  compoundID: UUID;
}
