import { describe, expect, it } from 'vitest';

import { resultCountLabel, sampleRowKey } from '@/lib/search';
import { makeSample } from '@/mocks/fixtures';

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

describe('sampleRowKey', () => {
  it('is the sample id when the hit is already an ELN sample', () => {
    expect(sampleRowKey(makeSample({ id: 'sample-1' }))).toBe('sample-1');
  });

  /**
   * A PubChem hit has no id until it is imported, so it is keyed by the catalog and that
   * catalog's own key — its CID. Two hits from different catalogs never collide.
   */
  it('falls back to the catalog and its key for a hit that is not in the ELN', () => {
    expect(sampleRowKey(makeSample({ source: 'PUBCHEM', id: undefined, compoundKey: '2244' }))).toBe('PUBCHEM:2244');
  });

  it('falls back to the formula when even the catalog key is missing', () => {
    const sample = makeSample({ source: 'PUBCHEM', id: undefined, compoundKey: undefined, molFormula: 'C6H6' });
    expect(sampleRowKey(sample)).toBe('PUBCHEM:C6H6');
  });
});
