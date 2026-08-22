import { describe, expect, it } from 'vitest';

import {
  EMPTY_GLOBAL_SEARCH_FORM,
  type GlobalSearchFormValues,
  isEmpty,
  toGlobalSearchRequest,
} from '@/components/search/global-search-form';

function form(overrides: Partial<GlobalSearchFormValues> = {}): GlobalSearchFormValues {
  return { ...EMPTY_GLOBAL_SEARCH_FORM, ...overrides };
}

describe('isEmpty', () => {
  it('is empty with neither a term nor a structure', () => {
    expect(isEmpty(form())).toBe(true);
  });

  it('ignores whitespace-only terms', () => {
    expect(isEmpty(form({ query: '   ' }))).toBe(true);
  });

  it('is not empty with only a structure', () => {
    expect(isEmpty(form({ structure: 'molfile' }))).toBe(false);
  });
});

describe('toGlobalSearchRequest', () => {
  it('sends a molecule structure as moleculeStructure', () => {
    expect(toGlobalSearchRequest(form({ structure: 'molfile', structureType: 'EXACT' }))).toEqual({
      query: null,
      moleculeStructure: { type: 'EXACT', query: 'molfile' },
      reactionStructure: null,
    });
  });

  it('sends a reaction as reactionStructure instead', () => {
    expect(toGlobalSearchRequest(form({ structure: 'rxnfile', isReaction: true }))).toEqual({
      query: null,
      moleculeStructure: null,
      reactionStructure: { type: 'SUBSTRUCTURE', query: 'rxnfile' },
    });
  });

  it('trims the term and drops it when blank', () => {
    expect(toGlobalSearchRequest(form({ query: '  aspirin  ' })).query).toBe('aspirin');
    expect(toGlobalSearchRequest(form({ query: '   ' })).query).toBeNull();
  });
});
