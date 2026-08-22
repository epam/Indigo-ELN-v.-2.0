import type { GlobalSearchRequest, StructuralSearchType } from '@/lib/types/search.ts';

export interface GlobalSearchFormValues {
  query: string;
  /** molfile or rxnfile, as drawn in the sketcher. */
  structure: string | null;
  /** Which of the two the structure is — Ketcher decides this, not the user. */
  isReaction: boolean;
  structureType: StructuralSearchType;
}

export const EMPTY_GLOBAL_SEARCH_FORM: GlobalSearchFormValues = {
  query: '',
  structure: null,
  isReaction: false,
  structureType: 'SUBSTRUCTURE',
};

export const STRUCTURE_TYPE_LABELS: Record<StructuralSearchType, string> = {
  EXACT: 'Exact',
  SUBSTRUCTURE: 'Substructure',
  SIMILARITY: 'Similarity',
};

/** Nothing to search for — the Search button stays disabled. */
export function isEmpty(values: GlobalSearchFormValues): boolean {
  return values.query.trim() === '' && !values.structure;
}

/**
 * Builds the request body. The drawn structure goes into whichever field matches what
 * Ketcher says it is, as in indigo-frontend's global-search.component.ts.
 */
export function toGlobalSearchRequest(values: GlobalSearchFormValues): GlobalSearchRequest {
  const query = values.query.trim();
  const structuralSearch = values.structure ? { type: values.structureType, query: values.structure } : null;

  return {
    query: query === '' ? null : query,
    moleculeStructure: values.isReaction ? null : structuralSearch,
    reactionStructure: values.isReaction ? structuralSearch : null,
  };
}
