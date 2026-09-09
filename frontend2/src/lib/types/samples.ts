import type { UUID } from '@/lib/types/common.ts';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { NumericSearch, StructuralSearch, TextSearch } from '@/lib/types/search.ts';

/**
 * `src/lib/types/samples.ts` mirrors the backend `compound/model` package — the registered
 * samples the catalogs answer with, and the request that searches them.
 *
 * `StructuralSearch`, `TextSearch` and `NumericSearch` are not redeclared here: they are the
 * same Java records the global search uses, already ported in `search.ts`.
 */

/** Mirrors SearchCatalog (eln-api, compound/model/search). The numbers are its priorities. */
export type SearchCatalog = 'ELN' | 'PUBCHEM' | 'MY_MATERIALS';

/**
 * The cursor. `SampleSearchResult.next` is handed straight back as the next request's `state`,
 * and it is opaque to the client — it carries the catalogs still to walk, the page reached in
 * the current one, and the count accumulated from the catalogs already finished.
 */
export interface FindSamplesState {
  catalogs: SearchCatalog[];
  pageNo: number;
  pageSize: number;
  oldCatalogsTotalItems: number | null;
}

/**
 * The body of POST /api/eln/samples/search, mirroring FindSamplesRequest (eln-api,
 * compound/model/search) in full.
 *
 * Every field but `catalogs` is optional and every combination is legal: unlike
 * `GlobalSearchRequest` there is no `isEmpty` assertion server-side, so a request carrying
 * nothing but a catalog is a browse rather than a 400. Add Material refuses to send one all the
 * same — see `isEmpty` in `add-material-form.ts` for why that gate is ours rather than the
 * server's.
 *
 * The filters are `@JsonInclude(NON_NULL)` on the far side, so an absent one and an explicit
 * `null` mean the same thing; the form omits what it is not filtering on.
 */
export interface FindSamplesRequest {
  /** `@NotNull @Size(min = 1)` — a search with no catalog is a 400. */
  catalogs: SearchCatalog[];
  /**
   * The free-text box. `@Size(min = 1)` server-side, so a blank string is a 400 rather than
   * "no filter" — the form sends `undefined` for an empty box.
   */
  quickSearch?: string;
  structure?: StructuralSearch;
  compoundKey?: TextSearch;
  nbkBatchNumber?: TextSearch;
  casNumber?: TextSearch;
  /** The External ID box. Angular's template binds that box to `chemicalName` by mistake. */
  externalNumber?: TextSearch;
  molecularFormula?: TextSearch;
  chemicalName?: TextSearch;
  batchComment?: TextSearch;
  molWeight?: NumericSearch;
  /** `ComponentStateRef` server-side, which serialises as a plain dictionary ref. */
  compoundState?: DictionaryItemRef;
  /** `HealthHazardRef`, likewise. Singular on the wire despite the plural name. */
  healthHazards?: DictionaryItemRef;
  /** The `next` of the previous page; absent starts at the first. */
  state?: FindSamplesState;
}

/**
 * One catalog hit, mirroring SampleDTO (eln-api, compound/model).
 *
 * Two fields carry most of the branching. `id` is null for a hit that is not in the ELN yet —
 * a PubChem row — which is what `POST /samples/importFromSearch` exists to fix, and until then
 * the row cannot be marked or bound to an input. `source` says which catalog answered, and the
 * import endpoint dispatches on it, so it has to survive the round trip untouched.
 */
export interface SampleDTO {
  source: SearchCatalog;
  id?: UUID;
  nbkBatchNumber?: string;
  /** The ELN compound key, or the PubChem CID. */
  compoundKey?: string;
  strCode?: string;
  /** HTML, via `@JsonValue toHTMLString()`: `C<sub>9</sub>H<sub>8</sub>O<sub>4</sub>`. */
  molFormula: string;
  molWeight: number;
  /** The ELN chemical name, or PubChem's IUPACName. */
  name?: string;
  saltCode?: DictionaryItemRef;
  saltEQ?: number;
  compoundID?: UUID;
  /** PubChem only, and the only way to render its structure — `/samples/external/picture`. */
  inchi?: string;
  /** `NON_DEFAULT`, so absent rather than `false` on an unmarked sample. */
  marked?: boolean;
}

/**
 * A page of hits. Unlike every other list in the app this is **not** a `Page<T>`: there is no
 * page number and no total page count, because the search walks several catalogs in priority
 * order and PubChem can neither count its matches nor be paged.
 *
 * So `totalItems` is null as soon as a catalog that cannot count has contributed, and `next`
 * being null is the only reliable end-of-results signal.
 */
export interface SampleSearchResult {
  items: SampleDTO[];
  totalItems: number | null;
  next: FindSamplesState | null;
}

/** What the catalog radio offers. `ALL` is a UI-only value; the wire has no such catalog. */
export type SampleCatalogFilter = 'ALL' | 'ELN' | 'PUBCHEM' | 'MY_MATERIALS';

export const SAMPLE_CATALOG_FILTERS: readonly SampleCatalogFilter[] = ['ALL', 'ELN', 'PUBCHEM', 'MY_MATERIALS'];

export const SAMPLE_CATALOG_FILTER_LABELS: Record<SampleCatalogFilter, string> = {
  ALL: 'All Catalogs',
  ELN: 'Indigo ELN',
  PUBCHEM: 'PubChem',
  MY_MATERIALS: 'My Materials',
};

/**
 * Which catalogs each choice searches — the port of indigo-frontend's `SEARCH_CATALOG_MAPPING`.
 *
 * `ALL` is `[ELN, PUBCHEM]` and deliberately not all three: `MyMaterialsCatalogSearchProvider`
 * answers `isEnabled(request) === false` whenever ELN is also asked for, since ELN results
 * already include the marked samples. Sending it would be ignored, not additive.
 */
export const CATALOGS_BY_FILTER: Record<SampleCatalogFilter, SearchCatalog[]> = {
  ALL: ['ELN', 'PUBCHEM'],
  ELN: ['ELN'],
  PUBCHEM: ['PUBCHEM'],
  MY_MATERIALS: ['MY_MATERIALS'],
};

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
