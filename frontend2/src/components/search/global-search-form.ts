import type { ExperimentStatus } from '@/lib/types/experiments.ts';
import { EXPERIMENT_STATUS_LABELS } from '@/lib/types/experiments.ts';
import type { GlobalSearchRequest, NumericSearch, ReactionRole, StructuralSearchType } from '@/lib/types/search.ts';
import { NUMERIC_SEARCH_OPERATOR_LABELS, REACTION_ROLE_LABELS } from '@/lib/types/search.ts';

import type { UserRef } from '@/lib/types/common.ts';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';

export interface GlobalSearchFormValues {
  query: string;
  /** molfile or rxnfile, as drawn in the sketcher. */
  structure: string | null;
  /** Which of the two the structure is — Ketcher decides this, not the user. */
  isReaction: boolean;
  structureType: StructuralSearchType;
  therapeuticArea: DictionaryItemRef | null;
  projectCode: DictionaryItemRef | null;
  batchYield: NumericSearch | null;
  batchPurity: NumericSearch | null;
  author: UserRef[];
  /** One status in the UI; the backend takes a set, so the request wraps it in an array. */
  experimentStatus: ExperimentStatus | null;
  reactionRole: ReactionRole | null;
}

export const EMPTY_GLOBAL_SEARCH_FORM: GlobalSearchFormValues = {
  query: '',
  structure: null,
  isReaction: false,
  structureType: 'SUBSTRUCTURE',
  therapeuticArea: null,
  projectCode: null,
  batchYield: null,
  batchPurity: null,
  author: [],
  experimentStatus: null,
  reactionRole: null,
};

/**
 * Whether Reaction Role applies at all. The backend rejects `reactionRole` unless
 * `moleculeStructure` is set (@AssertTrue isReactionRoleValid), and a drawn reaction goes
 * to `reactionStructure` instead — so the field is only meaningful for a drawn molecule.
 */
export function showReactionRole(values: GlobalSearchFormValues): boolean {
  return values.structure !== null && !values.isReaction;
}

/**
 * Nothing to search for — the Search button stays disabled. Mirrors the backend's own
 * GlobalSearchRequest.isEmpty(), which likewise does not count `reactionRole`: a request
 * carrying only a reaction role is a 400, not a search.
 */
export function isEmpty(values: GlobalSearchFormValues): boolean {
  return (
    values.query.trim() === '' &&
    !values.structure &&
    !values.therapeuticArea &&
    !values.projectCode &&
    !values.batchYield &&
    !values.batchPurity &&
    values.author.length === 0 &&
    !values.experimentStatus
  );
}

/**
 * Builds the request body. The drawn structure goes into whichever field matches what
 * Ketcher says it is, as in indigo-frontend's global-search.component.ts.
 */
export function toGlobalSearchRequest(values: GlobalSearchFormValues): GlobalSearchRequest {
  const query = values.query.trim();
  const structuralSearch = values.structure ? { type: values.structureType, query: values.structure } : null;
  const moleculeStructure = values.isReaction ? null : structuralSearch;

  return {
    query: query === '' ? null : query,
    moleculeStructure,
    reactionStructure: values.isReaction ? structuralSearch : null,
    therapeuticArea: values.therapeuticArea,
    projectCode: values.projectCode,
    batchYield: values.batchYield,
    batchPurity: values.batchPurity,
    author: values.author.length > 0 ? values.author : null,
    experimentStatus: values.experimentStatus ? [values.experimentStatus] : null,
    // Belt and braces over the UI gate: the backend rejects the pair outright.
    reactionRole: moleculeStructure ? values.reactionRole : null,
  };
}

/** One line of the collapsed Advanced Search summary: "**Batch Yield, %** ≥ 90". */
export interface AdvancedSummaryItem {
  label: string;
  /** "is" for a set membership, or the numeric operator symbol. */
  operator: string;
  value: string;
}

/**
 * What Advanced Search currently holds, for the header to show while collapsed. Ports the
 * four cases from indigo-frontend's search.util.ts, except that those built `<b>` markup
 * strings and these are structured rows the panel renders as JSX.
 */
export function summarizeAdvancedSearch(values: GlobalSearchFormValues): AdvancedSummaryItem[] {
  const items: AdvancedSummaryItem[] = [];

  function addIs(label: string, value: string | null) {
    if (value !== null) items.push({ label, operator: 'is', value });
  }

  function addNumeric(label: string, search: NumericSearch | null) {
    if (search)
      items.push({ label, operator: NUMERIC_SEARCH_OPERATOR_LABELS[search.type], value: String(search.value) });
  }

  addIs('Therapeutic Area', values.therapeuticArea?.name ?? null);
  addIs('Project Code', values.projectCode?.name ?? null);
  addNumeric('Batch Yield, %', values.batchYield);
  addNumeric('Batch Purity, %', values.batchPurity);
  // Several authors read as alternatives, matching how the backend treats the set.
  addIs('Author', values.author.length > 0 ? values.author.map((user) => user.displayName).join(' or ') : null);
  addIs('Experiment Status', values.experimentStatus ? EXPERIMENT_STATUS_LABELS[values.experimentStatus] : null);
  // Omitted when it does not apply, so the summary never claims a filter that is not sent.
  addIs(
    'Reaction Role',
    showReactionRole(values) && values.reactionRole ? REACTION_ROLE_LABELS[values.reactionRole] : null,
  );

  return items;
}
