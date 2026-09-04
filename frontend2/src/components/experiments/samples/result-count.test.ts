import { describe, expect, it } from 'vitest';

import { resultCountLabel } from '@/components/experiments/samples/result-count';

describe('resultCountLabel', () => {
  it('reports the count the server gave, however many rows have loaded', () => {
    expect(resultCountLabel({ totalItems: 120, loaded: 20, hasMore: true, loading: false })).toBe('120');
  });

  /** PubChem reports no count, and the sum goes null the moment it contributes. */
  it('reports a lower bound when the count is unknown and more remains', () => {
    expect(resultCountLabel({ totalItems: null, loaded: 12, hasMore: true, loading: false })).toBe('12+');
  });

  /**
   * The `+` is a claim that there is more. Once the cursor is spent there is not, so what has
   * loaded is the total after all — which is the common case here, since the catalogs that
   * cannot count also cannot page.
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
