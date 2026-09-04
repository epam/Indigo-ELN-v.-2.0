import type { BaseDTO, UserRef } from '@/lib/types/common.ts';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { ExperimentStatus } from '@/lib/types/experiments.ts';

/** Mirrors the backend's StructuralSearch.Type (eln-api, compound/model/search). */
export type StructuralSearchType = 'EXACT' | 'SUBSTRUCTURE' | 'SIMILARITY';

export interface StructuralSearch {
  type: StructuralSearchType;
  /** A molfile, or a rxnfile for a reaction search — never SMILES. */
  query: string;
}

/**
 * Mirrors NumericSearch (eln-api, compound/model/search): a Jackson-polymorphic sealed
 * interface discriminated on `type`. Only these three operators exist server-side — there
 * is no strict greater/less than, and no between.
 */
export type NumericSearchOperator = 'eq' | 'le' | 'ge';

export interface NumericSearch {
  type: NumericSearchOperator;
  value: number;
}

/** Display order for the operator picker. */
export const NUMERIC_SEARCH_OPERATORS: readonly NumericSearchOperator[] = ['eq', 'le', 'ge'];

/** As in indigo-frontend's NumericSearchTypeNames. */
export const NUMERIC_SEARCH_OPERATOR_LABELS: Record<NumericSearchOperator, string> = {
  eq: '=',
  le: '≤',
  ge: '≥',
};

/**
 * Mirrors TextSearch (eln-api, compound/model/search): a Jackson-polymorphic sealed interface
 * discriminated on `type`, four of whose five members carry a `value` while `between` carries a
 * range instead. Modelled as a union rather than one optional-everything object so a `between`
 * with a stray `value`, or an `exact` with a `from`, cannot be constructed.
 */
export type TextSearchOperator = 'exact' | 'startsWith' | 'contains' | 'endsWith' | 'between';

export type TextSearch =
  { type: Exclude<TextSearchOperator, 'between'>; value: string } | { type: 'between'; from: string; to: string };

/** Display order for the operator picker, as in indigo-frontend's TextSearchTypeNames. */
export const TEXT_SEARCH_OPERATORS: readonly TextSearchOperator[] = [
  'exact',
  'startsWith',
  'contains',
  'endsWith',
  'between',
];

export const TEXT_SEARCH_OPERATOR_LABELS: Record<TextSearchOperator, string> = {
  exact: 'exact',
  startsWith: 'starts with',
  contains: 'contains',
  endsWith: 'ends with',
  between: 'between',
};

/** Mirrors ReactionRole (eln-api, reaction/model). */
export type ReactionRole = 'REACTANT' | 'REAGENT' | 'CATALYST' | 'SOLVENT' | 'OUTPUT';

export const REACTION_ROLES: readonly ReactionRole[] = ['REACTANT', 'REAGENT', 'CATALYST', 'SOLVENT', 'OUTPUT'];

export const REACTION_ROLE_DISPLAY: Record<ReactionRole, string> = {
  REACTANT: 'Reactant',
  REAGENT: 'Reagent',
  CATALYST: 'Catalyst',
  SOLVENT: 'Solvent',
  OUTPUT: 'Output',
};

/**
 * The body of POST /api/eln/search, mirroring GlobalSearchRequest (eln-api, eln/model).
 *
 * Two server-side rules the caller has to respect: `reactionRole` is rejected unless
 * `moleculeStructure` is also set (@AssertTrue isReactionRoleValid), and a request with
 * every field null is a 400 (isEmpty — which notably does not count `reactionRole`).
 */
export interface GlobalSearchRequest {
  query?: string | null;
  therapeuticArea?: DictionaryItemRef | null;
  projectCode?: DictionaryItemRef | null;
  /** A Set server-side, so an array here even though the UI offers one status. */
  experimentStatus?: ExperimentStatus[] | null;
  author?: UserRef[] | null;
  batchYield?: NumericSearch | null;
  batchPurity?: NumericSearch | null;
  moleculeStructure?: StructuralSearch | null;
  reactionRole?: ReactionRole | null;
  reactionStructure?: StructuralSearch | null;
}

/** The three ELNEntityType values global search can emit. */
export type SearchEntityType = 'PROJECT' | 'NOTEBOOK' | 'EXPERIMENT';

/**
 * One hit, mirroring GlobalSearchResultDTO (eln-api, eln/model). Most fields apply to only
 * one entity type and are null otherwise — the DTO has no @JsonInclude(NON_NULL), so they
 * arrive explicitly as null rather than being absent.
 */
export interface GlobalSearchResult extends BaseDTO {
  type: SearchEntityType;
  /**
   * A code, not a title: a project's name, a notebook's "00000001", an experiment's
   * "00000001-0001". The experiment's human subject is `title`.
   */
  name: string;
  /** Experiments only. */
  title: string | null;
  /** Experiments only. */
  experimentStatus: ExperimentStatus | null;
  /** Experiments only, and only when the request carried a `moleculeStructure`: the roles
   * the searched molecule occurs under in this experiment. */
  reactionRoles: ReactionRole[] | null;
  /** Experiments only. Busts the picture endpoint's 30-day cache. */
  revision: number | null;
  /** Projects only. */
  notebookCount: number | null;
  /** Projects and notebooks. */
  experimentCount: number | null;
  /** The description excerpt, with <mark> markup when the request carried a `query`.
   * Not rendered yet. */
  fragment: string | null;
}
