import { describe, expect, it } from 'vitest';

import {
  EMPTY_GLOBAL_SEARCH_FORM,
  type GlobalSearchFormValues,
  isEmpty,
  showReactionRole,
  summarizeAdvancedSearch,
  toGlobalSearchRequest,
} from '@/components/search/global-search-form';

function form(overrides: Partial<GlobalSearchFormValues> = {}): GlobalSearchFormValues {
  return { ...EMPTY_GLOBAL_SEARCH_FORM, ...overrides };
}

const OBESITY = { id: 'ta-1', name: 'Obesity' };
const CODE_1 = { id: 'pc-1', name: 'Code 1' };
const MARK = { username: 'mark.liu', displayName: 'Mark Liu' };
const ANNA = { username: 'anna.petrova', displayName: 'Anna Petrova' };

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

  it.each<[string, Partial<GlobalSearchFormValues>]>([
    ['therapeuticArea', { therapeuticArea: OBESITY }],
    ['projectCode', { projectCode: CODE_1 }],
    ['batchYield', { batchYield: { type: 'ge', value: 90 } }],
    ['batchPurity', { batchPurity: { type: 'eq', value: 99 } }],
    ['author', { author: [MARK] }],
    ['experimentStatus', { experimentStatus: 'OPEN' }],
  ])('is not empty with only %s', (_name, overrides) => {
    expect(isEmpty(form(overrides))).toBe(false);
  });

  it('is still empty with only a reaction role, matching the backend isEmpty()', () => {
    expect(isEmpty(form({ reactionRole: 'REACTANT' }))).toBe(true);
  });
});

describe('showReactionRole', () => {
  it('is off until a structure is drawn', () => {
    expect(showReactionRole(form())).toBe(false);
  });

  it('is on for a molecule and off for a reaction', () => {
    expect(showReactionRole(form({ structure: 'molfile' }))).toBe(true);
    expect(showReactionRole(form({ structure: 'rxnfile', isReaction: true }))).toBe(false);
  });
});

describe('toGlobalSearchRequest', () => {
  it('sends a molecule structure as moleculeStructure', () => {
    expect(toGlobalSearchRequest(form({ structure: 'molfile', structureType: 'EXACT' }))).toMatchObject({
      moleculeStructure: { type: 'EXACT', query: 'molfile' },
      reactionStructure: null,
    });
  });

  it('sends a reaction as reactionStructure instead', () => {
    expect(toGlobalSearchRequest(form({ structure: 'rxnfile', isReaction: true }))).toMatchObject({
      moleculeStructure: null,
      reactionStructure: { type: 'SUBSTRUCTURE', query: 'rxnfile' },
    });
  });

  it('trims the term and drops it when blank', () => {
    expect(toGlobalSearchRequest(form({ query: '  aspirin  ' })).query).toBe('aspirin');
    expect(toGlobalSearchRequest(form({ query: '   ' })).query).toBeNull();
  });

  it('leaves every unset advanced field null', () => {
    expect(toGlobalSearchRequest(form({ query: 'aspirin' }))).toEqual({
      query: 'aspirin',
      moleculeStructure: null,
      reactionStructure: null,
      therapeuticArea: null,
      projectCode: null,
      batchYield: null,
      batchPurity: null,
      author: null,
      experimentStatus: null,
      reactionRole: null,
    });
  });

  it('wraps the single status in an array, as the backend takes a set', () => {
    expect(toGlobalSearchRequest(form({ experimentStatus: 'SIGNED' })).experimentStatus).toEqual(['SIGNED']);
  });

  it('sends authors as a list and drops an empty one', () => {
    expect(toGlobalSearchRequest(form({ author: [MARK, ANNA] })).author).toEqual([MARK, ANNA]);
    expect(toGlobalSearchRequest(form()).author).toBeNull();
  });

  it('passes the dictionary refs and numeric searches through unchanged', () => {
    const request = toGlobalSearchRequest(
      form({
        therapeuticArea: OBESITY,
        projectCode: CODE_1,
        batchYield: { type: 'ge', value: 90 },
        batchPurity: { type: 'le', value: 99.5 },
      }),
    );
    expect(request).toMatchObject({
      therapeuticArea: OBESITY,
      projectCode: CODE_1,
      batchYield: { type: 'ge', value: 90 },
      batchPurity: { type: 'le', value: 99.5 },
    });
  });

  it('keeps a reaction role alongside a molecule', () => {
    const request = toGlobalSearchRequest(form({ structure: 'molfile', reactionRole: 'CATALYST' }));
    expect(request.reactionRole).toBe('CATALYST');
  });

  it.each([
    ['no structure at all', form({ reactionRole: 'CATALYST' })],
    ['a reaction', form({ structure: 'rxnfile', isReaction: true, reactionRole: 'CATALYST' })],
  ])('drops a reaction role with %s, which the backend would reject', (_name, values) => {
    expect(toGlobalSearchRequest(values).reactionRole).toBeNull();
  });
});

describe('summarizeAdvancedSearch', () => {
  it('says nothing about an untouched form', () => {
    expect(summarizeAdvancedSearch(form())).toEqual([]);
  });

  it('reports dictionary picks as "is", in field order', () => {
    expect(summarizeAdvancedSearch(form({ therapeuticArea: OBESITY, projectCode: CODE_1 }))).toEqual([
      { label: 'Therapeutic Area', operator: 'is', value: 'Obesity' },
      { label: 'Project Code', operator: 'is', value: 'Code 1' },
    ]);
  });

  it('reports a numeric search with its operator symbol', () => {
    expect(summarizeAdvancedSearch(form({ batchYield: { type: 'ge', value: 90 } }))).toEqual([
      { label: 'Batch Yield, %', operator: '≥', value: '90' },
    ]);
  });

  it('joins several authors as alternatives', () => {
    expect(summarizeAdvancedSearch(form({ author: [MARK, ANNA] }))).toEqual([
      { label: 'Author', operator: 'is', value: 'Mark Liu or Anna Petrova' },
    ]);
  });

  it('uses the display name of an enum', () => {
    expect(summarizeAdvancedSearch(form({ experimentStatus: 'SIGNED' }))).toEqual([
      { label: 'Experiment Status', operator: 'is', value: 'Signed' },
    ]);
  });

  it('omits a reaction role that does not apply, so it never claims an unsent filter', () => {
    expect(summarizeAdvancedSearch(form({ reactionRole: 'CATALYST' }))).toEqual([]);
    expect(summarizeAdvancedSearch(form({ structure: 'molfile', reactionRole: 'CATALYST' }))).toEqual([
      { label: 'Reaction Role', operator: 'is', value: 'Catalyst' },
    ]);
  });
});
