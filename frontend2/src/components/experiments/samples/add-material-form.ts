import type { AdvancedSummaryItem } from '@/components/search/global-search-form';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { FindSamplesRequest, SampleCatalogFilter } from '@/lib/types/samples.ts';
import { CATALOGS_BY_FILTER } from '@/lib/types/samples.ts';
import type { NumericSearch, StructuralSearchType, TextSearch } from '@/lib/types/search.ts';
import { NUMERIC_SEARCH_OPERATOR_LABELS, TEXT_SEARCH_OPERATOR_LABELS } from '@/lib/types/search.ts';

/**
 * The ten filters behind Advanced search, as the form holds them. Split out from the rest of the
 * values because everything that gates on the catalog — the disabled set, the request trimming,
 * the collapsed summary — names these and only these.
 *
 * Each is `null` until it has something to search for; the two field components emit null for a
 * blank box rather than an empty `value`, which is not a filter the backend would understand.
 */
export interface AddMaterialFilters {
  compoundKey: TextSearch | null;
  nbkBatchNumber: TextSearch | null;
  molecularFormula: TextSearch | null;
  molWeight: NumericSearch | null;
  chemicalName: TextSearch | null;
  externalNumber: TextSearch | null;
  compoundState: DictionaryItemRef | null;
  batchComment: TextSearch | null;
  healthHazards: DictionaryItemRef | null;
  casNumber: TextSearch | null;
}

export type MaterialFilter = keyof AddMaterialFilters;

export interface AddMaterialFormValues extends AddMaterialFilters {
  quickSearch: string;
  catalog: SampleCatalogFilter;
  /**
   * A molfile, always: the sketcher can draw a reaction, but one is refused on Save rather than
   * stored here — see `REACTION_NOT_SEARCHABLE`.
   */
  structure: string | null;
  structureType: StructuralSearchType;
}

export const EMPTY_ADD_MATERIAL_FORM: AddMaterialFormValues = {
  quickSearch: '',
  catalog: 'ALL',
  structure: null,
  structureType: 'SUBSTRUCTURE',
  compoundKey: null,
  nbkBatchNumber: null,
  molecularFormula: null,
  molWeight: null,
  chemicalName: null,
  externalNumber: null,
  compoundState: null,
  batchComment: null,
  healthHazards: null,
  casNumber: null,
};

/** The label each filter shows, in the order the grid lays them out. */
export const MATERIAL_FILTER_LABELS: Record<MaterialFilter, string> = {
  compoundKey: 'Compound ID',
  nbkBatchNumber: 'Nbk Batch #',
  molecularFormula: 'Molecular Formula',
  molWeight: 'Molecular Weight',
  chemicalName: 'Chemical Name',
  externalNumber: 'External ID',
  compoundState: 'Component State',
  batchComment: 'Batch Comment',
  healthHazards: 'Health Hazards',
  casNumber: 'CAS Number',
};

/**
 * Whether the chosen catalog reaches PubChem — true for `ALL` as well as `PUBCHEM`, since `ALL`
 * is `[ELN, PUBCHEM]`.
 *
 * PubChem's API takes a name, a formula or a structure and nothing else, so the catalog decides
 * what the form may ask: the rest would be dropped on the way out, and a filter that is silently
 * ignored is worse than one that is visibly unavailable.
 */
export function pubchemIncluded(catalog: SampleCatalogFilter): boolean {
  return CATALOGS_BY_FILTER[catalog].includes('PUBCHEM');
}

/**
 * The filters PubChem cannot honour — every one in the grid except Molecular Formula, which its
 * API does accept. The port of indigo-frontend's `PUBCHEM_DISABLED_CONTROLS`, plus
 * `externalNumber`: that list omits it only because the Angular template never wired the box up.
 */
export const PUBCHEM_DISABLED_FILTERS: readonly MaterialFilter[] = [
  'compoundKey',
  'nbkBatchNumber',
  'molWeight',
  'chemicalName',
  'externalNumber',
  'compoundState',
  'batchComment',
  'healthHazards',
  'casNumber',
];

export const PUBCHEM_NOTICE = 'PubChem does not support fine-grained search. Use quick search instead';

/**
 * Why a drawn reaction is refused rather than searched for: `FindSamplesRequest` has one
 * structure field and a catalog holds compounds, so there is nothing for a rxnfile to match.
 * Global Search does route one, into `reactionStructure`; there is no such field here.
 */
export const REACTION_NOT_SEARCHABLE = 'Draw a single molecule, not a reaction';

/** Whether one field is unavailable under the chosen catalog. */
export function isFilterDisabled(catalog: SampleCatalogFilter, filter: MaterialFilter): boolean {
  return pubchemIncluded(catalog) && PUBCHEM_DISABLED_FILTERS.includes(filter);
}

/**
 * Builds the request body.
 *
 * A disabled filter is **dropped** rather than sent and ignored, as `performSearch` does with its
 * `delete` loop — a request has to say what was actually searched for, or the results answer a
 * different question from the one the form appears to be asking. The value survives in the form
 * while disabled, so choosing ELN again restores the search without retyping it.
 *
 * `quickSearch` is `@Size(min = 1)` server-side, so an empty box is omitted rather than sent as
 * `''`, which would be a 400. Everything else is simply absent when null.
 */
export function toFindSamplesRequest(values: AddMaterialFormValues): FindSamplesRequest {
  const quickSearch = values.quickSearch.trim();
  const keep = <T>(filter: MaterialFilter, value: T | null): T | undefined =>
    value == null || isFilterDisabled(values.catalog, filter) ? undefined : value;

  return {
    catalogs: CATALOGS_BY_FILTER[values.catalog],
    quickSearch: quickSearch === '' ? undefined : quickSearch,
    structure: values.structure ? { type: values.structureType, query: values.structure } : undefined,
    compoundKey: keep('compoundKey', values.compoundKey),
    nbkBatchNumber: keep('nbkBatchNumber', values.nbkBatchNumber),
    molecularFormula: keep('molecularFormula', values.molecularFormula),
    molWeight: keep('molWeight', values.molWeight),
    chemicalName: keep('chemicalName', values.chemicalName),
    externalNumber: keep('externalNumber', values.externalNumber),
    compoundState: keep('compoundState', values.compoundState),
    batchComment: keep('batchComment', values.batchComment),
    healthHazards: keep('healthHazards', values.healthHazards),
    casNumber: keep('casNumber', values.casNumber),
  };
}

/**
 * Every field of the request that is a search criterion — everything but the catalog, which is
 * always set and asks nothing on its own.
 */
const CRITERIA_KEYS = [
  'quickSearch',
  'structure',
  ...(Object.keys(MATERIAL_FILTER_LABELS) as MaterialFilter[]),
] as const;

/**
 * Nothing to search for — the Search button stays disabled.
 *
 * Read off the **request** rather than the form, so it can never disagree with what would be
 * sent: a filter the catalog has disabled is dropped by `toFindSamplesRequest` and therefore
 * does not count here either, which is what stops a form that looks filled in — nine boxes with
 * values, PubChem selected — from enabling a search that would carry none of them.
 *
 * The backend would accept the empty request (`FindSamplesRequest` has no `isEmpty` assertion,
 * unlike `GlobalSearchRequest`), and indigo-frontend does send it. It is refused here because a
 * whole catalog is not an answer to any question the user asked, and the first page of one is a
 * PubChem round trip spent saying so.
 */
export function isEmpty(values: AddMaterialFormValues): boolean {
  const request = toFindSamplesRequest(values);
  return CRITERIA_KEYS.every((key) => request[key] == null);
}

/** "**Compound ID** contains ASA", or the `between` form, or nothing for an unset filter. */
function textItem(label: string, search: TextSearch | null): AdvancedSummaryItem | null {
  if (search == null) return null;
  const operator = TEXT_SEARCH_OPERATOR_LABELS[search.type];
  return search.type === 'between'
    ? { label, operator, value: `${search.from} and ${search.to}` }
    : { label, operator, value: search.value };
}

function numericItem(label: string, search: NumericSearch | null): AdvancedSummaryItem | null {
  return search == null
    ? null
    : { label, operator: NUMERIC_SEARCH_OPERATOR_LABELS[search.type], value: String(search.value) };
}

function refItem(label: string, ref: DictionaryItemRef | null): AdvancedSummaryItem | null {
  return ref == null ? null : { label, operator: 'is', value: ref.name };
}

/**
 * What Advanced search currently holds, for the header to show while collapsed — the same
 * structured rows Global Search's summary renders, built by the three rules
 * `core/utils/search.util.ts` uses.
 *
 * A filter the catalog has disabled is left out, so the summary never claims something the
 * request will not carry.
 */
export function summarizeAddMaterialSearch(values: AddMaterialFormValues): AdvancedSummaryItem[] {
  const items: AdvancedSummaryItem[] = [];

  function add(filter: MaterialFilter, item: AdvancedSummaryItem | null) {
    if (item != null && !isFilterDisabled(values.catalog, filter)) items.push(item);
  }

  const text = (filter: MaterialFilter, search: TextSearch | null) =>
    add(filter, textItem(MATERIAL_FILTER_LABELS[filter], search));
  const ref = (filter: MaterialFilter, value: DictionaryItemRef | null) =>
    add(filter, refItem(MATERIAL_FILTER_LABELS[filter], value));

  text('compoundKey', values.compoundKey);
  text('nbkBatchNumber', values.nbkBatchNumber);
  text('molecularFormula', values.molecularFormula);
  add('molWeight', numericItem(MATERIAL_FILTER_LABELS.molWeight, values.molWeight));
  text('chemicalName', values.chemicalName);
  text('externalNumber', values.externalNumber);
  ref('compoundState', values.compoundState);
  text('batchComment', values.batchComment);
  ref('healthHazards', values.healthHazards);
  text('casNumber', values.casNumber);

  return items;
}
