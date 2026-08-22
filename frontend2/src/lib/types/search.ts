/** Mirrors the backend's StructuralSearch.Type (eln-api, compound/model/search). */
export type StructuralSearchType = 'EXACT' | 'SUBSTRUCTURE' | 'SIMILARITY';

export interface StructuralSearch {
  type: StructuralSearchType;
  /** A molfile, or a rxnfile for a reaction search — never SMILES. */
  query: string;
}

/**
 * The body of POST /api/eln/search. The advanced-search fields the backend also
 * accepts (therapeuticArea, projectCode, experimentStatus, author, batchYield,
 * batchPurity, reactionRole) are left out until that section is built.
 */
export interface GlobalSearchRequest {
  query?: string | null;
  moleculeStructure?: StructuralSearch | null;
  reactionStructure?: StructuralSearch | null;
}
