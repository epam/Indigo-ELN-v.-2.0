import type { UUID } from '@/lib/types/common.ts';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { StructuralSearch } from '@/lib/types/search.ts';

/**
 * `src/lib/types/samples.ts` mirrors the backend `compound/model` package — the registered
 * samples the catalogs answer with, and the request that searches them.
 *
 * `StructuralSearch` is not redeclared here: it is the same Java record `GlobalSearchRequest`
 * uses, already ported in `search.ts`.
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
 * compound/model/search) — **partially**.
 *
 * The Java class declares nine more filters: `quickSearch`, and `compoundKey`,
 * `nbkBatchNumber`, `casNumber`, `externalNumber`, `molecularFormula`, `chemicalName` and
 * `batchComment` as `TextSearch`, `molWeight` as `NumericSearch`, plus `compoundState` and
 * `healthHazards` dictionary refs. They belong to indigo-frontend's Add Material search form,
 * which is not ported; Analyze RXN searches by structure alone. Add them here — with
 * `TextSearch`, which `search.ts` also lacks — when that form arrives.
 */
export interface FindSamplesRequest {
  /** `@NotNull @Size(min = 1)` — a search with no catalog is a 400. */
  catalogs: SearchCatalog[];
  structure?: StructuralSearch;
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
