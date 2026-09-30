import { describe, expect, it } from 'vitest';

import { resultCountLabel, sampleKeyOf, sampleRowKey } from '@/lib/search';
import { makeSample } from '@/mocks/fixtures';

describe('resultCountLabel', () => {
  it('reports the count the server gave, however many rows have loaded', () => {
    expect(resultCountLabel({ totalItems: 120, loaded: 20, hasMore: true, loading: false })).toBe('120');
  });

  /** PubChem reports no count. */
  it('reports a lower bound when the count is unknown and more remains', () => {
    expect(resultCountLabel({ totalItems: null, loaded: 12, hasMore: true, loading: false })).toBe('12+');
  });

  /**
   * The `+` is a claim that there is more. Once there is no next page there is not, so what has
   * loaded is the total after all — which is the common case here, since PubChem cannot count
   * and cannot page either.
   */
  it('drops the plus once the search is exhausted', () => {
    expect(resultCountLabel({ totalItems: null, loaded: 12, hasMore: false, loading: false })).toBe('12');
  });

  it('says nothing while the first page is still in flight', () => {
    expect(resultCountLabel({ totalItems: null, loaded: 0, hasMore: false, loading: true })).toBeNull();
  });

  it('says zero rather than nothing for a search that found none', () => {
    expect(resultCountLabel({ totalItems: 0, loaded: 0, hasMore: false, loading: false })).toBe('0');
    // The same search, from a catalog that does not count.
    expect(resultCountLabel({ totalItems: null, loaded: 0, hasMore: false, loading: false })).toBe('0');
  });
});

describe('sampleRowKey', () => {
  it('is the source system and the key there', () => {
    expect(sampleRowKey(makeSample({ source: 'SRS', sampleKey: 'STR-00000000-89-123' }))).toBe(
      'SRS:STR-00000000-89-123',
    );
  });

  /** My Materials answers with the same sample the source catalog does; marking one shows in both. */
  it('ignores which catalog answered', () => {
    const fromSrs = makeSample({ catalog: 'SRS' });
    expect(sampleRowKey(makeSample({ catalog: 'MY_MATERIALS' }))).toBe(sampleRowKey(fromSrs));
  });

  it('matches the key of the same sample once it is in the model', () => {
    expect(sampleRowKey(makeSample({ source: 'PUBCHEM', sampleKey: '2244' }))).toBe(sampleKeyOf('PUBCHEM', '2244'));
  });
});
