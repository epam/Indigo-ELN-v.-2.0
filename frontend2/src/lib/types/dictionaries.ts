import type { BaseDTO, DateString, UUID } from '@/lib/types/common.ts';

/**
 * The wire shape of the backend's DictionaryItemRef (eln-api, eln/model). Its `active`,
 * `deleted` and `dictionaryID` fields are @JsonIgnore, so only these two arrive — and the
 * per-dictionary subclasses (TherapeuticAreaRef, ProjectCodeRef, …) serialise identically,
 * so one type covers every dictionary.
 */
export interface DictionaryItemRef {
  id: UUID;
  name: string;
}

/**
 * Mirrors BuiltInDictionary (eln-api, eln/model). The name is accepted wherever the API
 * takes a `{dictionary}` path segment — DictionaryService.refToID looks the enum up first
 * and only then parses a UUID — so the ids themselves never need to be duplicated here.
 */
export const BUILT_IN_DICTIONARIES = [
  'THERAPEUTIC_AREA',
  'PROJECT_CODE',
  'STEREOISOMER_CODE',
  'HEALTH_HAZARD',
  'HANDLING_PRECAUTIONS',
  'STORAGE_INSTRUCTIONS',
  'COMPOUND_PROTECTION',
  'SOLVENT',
  'EXTERNAL_SUPPLIER',
  'SAMPLE_SOURCE',
  'SAMPLE_SOURCE_DETAILS',
  'COMPONENT_STATE',
  'SALT_CODE',
] as const;

export type BuiltInDictionary = (typeof BUILT_IN_DICTIONARIES)[number];

/**
 * True for a code that names a `BuiltInDictionary`. The only reason the distinction matters to
 * the UI: those thirteen have a persisted `dictionaryKeys.items` entry to invalidate after a
 * write, and a dictionary an admin created has none.
 */
export function isBuiltInDictionary(code: string): code is BuiltInDictionary {
  return (BUILT_IN_DICTIONARIES as readonly string[]).includes(code);
}

/**
 * Mirrors DictionaryDTO (eln-api, eln/model), the row `GET /dictionaries` answers with.
 *
 * `code` is what `BuiltInDictionary` matches on, but it is **not** usable as the `{dictionary}`
 * path segment for one an admin created: `DictionaryService.refToID` accepts an enum name or a
 * UUID and nothing else, so every URL here is built from `id`.
 */
export interface Dictionary extends BaseDTO {
  code: string;
  name: string;
  userEditable: boolean;
  description: string;
}

/**
 * Mirrors DictionaryItemDTO (eln-api, eln/model) — one word, as `/full` returns it. Unlike
 * `DictionaryItemRef` this carries the fields the admin table edits, including the inactive
 * ones `GET /dictionaries/{ref}` filters out. There is no `modifiedBy`/`modifiedAt` on an item.
 *
 * `ordinal` is 1-based and dense: the server renumbers the whole list on every write, so a
 * `PATCH {ordinal: n}` reads as "move this word to position n".
 */
export interface DictionaryItem {
  id: UUID;
  createdAt: DateString;
  name: string;
  description: string | null;
  ordinal: number;
  active: boolean;
}

/** Mirrors DictionaryItemRequest (eln-api, eln/model) — the body `POST /dictionaries/{ref}` takes. */
export interface DictionaryItemRequest {
  name: string;
  description?: string | null;
}

/**
 * Mirrors DictionaryItemEditRequest (eln-api, eln/model). Every field is a `JsonNullable` the
 * server leaves alone when absent, so an omitted key means "unchanged" and an explicit `null`
 * description means "cleared" — which is why `description` is optional *and* nullable.
 *
 * `ordinal` is a 1-based destination: the server moves the word there and renumbers the rest.
 */
export interface DictionaryItemEditRequest {
  name?: string;
  description?: string | null;
  ordinal?: number;
  active?: boolean;
}
