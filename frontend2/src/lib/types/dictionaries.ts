import type { UUID } from '@/lib/types/common.ts';

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
