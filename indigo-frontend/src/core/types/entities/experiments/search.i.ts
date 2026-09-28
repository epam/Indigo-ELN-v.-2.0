import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { ReactionRole, UUID } from '@core/types/entities/experiments/experiment-shared.i';
import { ExperimentStatus } from '@core/enums/experiment-status.enum';
import { UserRef } from '@core/types/entities/user.i';

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

export type NumericSearch = NumericSearchEquals | NumericSearchLessThanOrEquals | NumericSearchGreaterThanOrEquals;

export interface FindSamplesRequest {
  catalogs: SearchCatalog[];
  quickSearch?: string;
  structure?: StructuralSearch;
  compoundKey?: TextSearch;
  nbkBatchNumber?: TextSearch;
  casNumber?: TextSearch;
  externalNumber?: TextSearch;
  molecularFormula?: TextSearch;
  molWeight?: NumericSearch;
  chemicalName?: TextSearch;
  compoundState?: DictionaryItemRef;
  batchComment?: TextSearch;
  healthHazards?: DictionaryItemRef;
  marked?: boolean;
  state?: unknown;
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

export interface Sample {
  id?: UUID;
  strCode?: string;
  nbkBatchNumber?: string;
  compoundKey?: string;
  molFormula?: string;
  molWeight: number;
  name?: string;
  saltCode?: DictionaryItemRef;
  saltEQ?: number;
  compoundID?: UUID;
  image: string;
  marked?: boolean;
}

export enum SearchCatalog {
  ELN = 'ELN',
  PUBCHEM = 'PUBCHEM',
  MY_MATERIALS = 'MY_MATERIALS',
}

export enum SearchCatalogUI {
  ALL = 'ALL',
  ELN = 'ELN',
  PUBCHEM = 'PUBCHEM',
  MY_MATERIALS = 'MY_MATERIALS',
}

export const SEARCH_CATALOG_MAPPING = {
  [SearchCatalogUI.ALL]: [SearchCatalog.ELN, SearchCatalog.PUBCHEM],
  [SearchCatalogUI.ELN]: [SearchCatalog.ELN],
  [SearchCatalogUI.PUBCHEM]: [SearchCatalog.PUBCHEM],
  [SearchCatalogUI.MY_MATERIALS]: [SearchCatalog.MY_MATERIALS],
};

export interface SampleSearchResult {
  items: Sample[];
  totalItems?: number;
  next?: unknown;
}

export interface GlobalSearchRequest {
  query?: string;
  therapeuticArea?: DictionaryItemRef;
  projectCode?: DictionaryItemRef;
  experimentStatus?: ExperimentStatus[];
  author?: UserRef[];
  batchYield?: NumericSearch;
  batchPurity?: NumericSearch;
  moleculeStructure?: StructuralSearch;
  reactionRole?: ReactionRole;
  reactionStructure?: StructuralSearch;
}

export enum GlobalSearchEntityType {
  PROJECT = 'PROJECT',
  NOTEBOOK = 'NOTEBOOK',
  EXPERIMENT = 'EXPERIMENT',
}

export interface GlobalSearchResult {
  id: string;
  createdBy: UserRef;
  type: GlobalSearchEntityType;
  name: string;
  fragment?: string;
  reactionRoles?: ReactionRole[];
  experimentStatus?: ExperimentStatus;
  revision?: number;
}
