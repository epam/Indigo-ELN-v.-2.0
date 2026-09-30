import type { UUID } from '@/lib/types/common.ts';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { MolarityUnit, NbkBatchNumber, SampleSource } from '@/lib/types/reactions.ts';
import type { NumericSearch, StructuralSearch, TextSearch } from '@/lib/types/search.ts';

/**
 * `src/lib/types/samples.ts` mirrors the backend `compound/model` package — the samples the
 * catalogs answer with, and the request that searches them.
 *
 * `StructuralSearch`, `TextSearch` and `NumericSearch` are not redeclared here: they are the
 * same Java records the global search uses, already ported in `search.ts`.
 */

/** Mirrors SearchCatalog (eln-api, compound/model/search). A search asks exactly one. */
export type SearchCatalog = 'SRS' | 'PUBCHEM' | 'MY_MATERIALS';

/**
 * The body of POST /api/eln/samples/search, mirroring FindSamplesRequest (eln-api,
 * compound/model/search) in full.
 *
 * Every field but `catalog` is optional and every combination is legal: unlike
 * `GlobalSearchRequest` there is no `isEmpty` assertion server-side, so a request carrying
 * nothing but a catalog is a browse rather than a 400. Add Material refuses to send one all the
 * same — see `isEmpty` in `add-material-form.ts` for why that gate is ours rather than the
 * server's. PubChem honours only `quickSearch`, `structure` and `molecularFormula`, and 400s on
 * the rest.
 *
 * The filters are `@JsonInclude(NON_NULL)` on the far side, so an absent one and an explicit
 * `null` mean the same thing; the form omits what it is not filtering on.
 */
export interface FindSamplesRequest {
  /** `@NotNull`. */
  catalog: SearchCatalog;
  /**
   * The free-text box. `@Size(min = 1)` server-side, so a blank string is a 400 rather than
   * "no filter" — the form sends `undefined` for an empty box.
   */
  quickSearch?: string;
  structure?: StructuralSearch;
  /** SRS: the compound STR code. */
  compoundKey?: TextSearch;
  nbkBatchNumber?: TextSearch;
  casNumber?: TextSearch;
  /** SRS: the sample STR code. */
  sampleKey?: TextSearch;
  molecularFormula?: TextSearch;
  chemicalName?: TextSearch;
  batchComment?: TextSearch;
  molWeight?: NumericSearch;
  /** `ComponentStateRef` server-side, which serialises as a plain dictionary ref. */
  compoundState?: DictionaryItemRef;
  /** `HealthHazardRef`, likewise. Singular on the wire despite the plural name. */
  healthHazards?: DictionaryItemRef;
}

/**
 * One catalog hit, mirroring SampleDTO (eln-api, compound/model).
 *
 * A hit is identified by `source` + `sampleKey`, never by an ELN id: it lives in its catalog,
 * and the mutations that put it into the model (`AddInput`, `ResolveInputs`) and mark/unmark
 * all take the whole DTO back, so it has to survive the round trip untouched.
 */
export interface SampleDTO {
  /** Which catalog answered. */
  catalog: SearchCatalog;
  /** Which system the sample lives in — not the same as `catalog`: My Materials holds SRS and PubChem samples. */
  source: SampleSource;
  /** The compound's key in `source`: an STR code for SRS, the CID for PubChem. */
  compoundKey: string;
  /**
   * The compound's id **in `source`**, when that system has one. For an SRS hit it is an SRS
   * id, so it only names an ELN compound when `catalog` is `MY_MATERIALS`.
   */
  compoundID?: UUID;
  saltCode?: DictionaryItemRef;
  saltEQ?: number;
  /** The chemical name, or PubChem's IUPACName. */
  chemicalName?: string;
  /** PubChem only, and the only way to render its structure — client-side, via Ketcher. */
  inchi?: string;
  /** The sample's key in `source`: an STR code for SRS, the CID again for PubChem. */
  sampleKey: string;
  nbkBatchNumber?: NbkBatchNumber;
  /** HTML, via `@JsonValue toHTMLString()`: `C<sub>9</sub>H<sub>8</sub>O<sub>4</sub>`. */
  molFormula: string;
  molWeight: number;
  density?: number;
  molarity?: number;
  molarityUnit?: MolarityUnit;
  purity?: number;
  healthHazards?: DictionaryItemRef[];
  compoundState?: DictionaryItemRef;
  batchComment?: string;
  /** `NON_DEFAULT`, so absent rather than `false` on an unmarked sample. */
  marked?: boolean;
}

/** What the catalog radio offers, in its order. */
export const SAMPLE_CATALOGS: readonly SearchCatalog[] = ['SRS', 'PUBCHEM', 'MY_MATERIALS'];

export const SAMPLE_CATALOG_LABELS: Record<SearchCatalog, string> = {
  SRS: 'Sample Registration',
  PUBCHEM: 'PubChem',
  MY_MATERIALS: 'My Materials',
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
  sampleKey: TextSearch | null;
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
  sampleKey: 'Sample ID',
  compoundState: 'Component State',
  batchComment: 'Batch Comment',
  healthHazards: 'Health Hazards',
  casNumber: 'CAS Number',
};

/**
 * The filters PubChem cannot honour — every one in the grid except Molecular Formula, which its
 * API does accept. The backend 400s on any of them (`PubChemCatalogSearchProvider.executeQuery`).
 */
export const PUBCHEM_DISABLED_FILTERS: readonly MaterialFilter[] = [
  'compoundKey',
  'nbkBatchNumber',
  'molWeight',
  'chemicalName',
  'sampleKey',
  'compoundState',
  'batchComment',
  'healthHazards',
  'casNumber',
];
