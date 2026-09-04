import { describe, expect, it } from 'vitest';

import { sampleRowKey } from '@/components/experiments/samples/sample-row-key';
import { makeSample } from '@/mocks/fixtures';

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
