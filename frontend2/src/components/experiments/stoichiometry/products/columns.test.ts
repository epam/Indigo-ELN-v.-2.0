import { describe, expect, it } from 'vitest';

import { PRODUCT_COLUMNS, productHaystack } from '@/components/experiments/stoichiometry/products/columns';
import { makeReactionOutput } from '@/mocks/fixtures';

import type { ReactionOutput } from '@/lib/types/reactions.ts';
import { OUTPUT_TYPE_LABELS, OUTPUT_TYPE_TRIGGER_CLASS, OUTPUT_TYPES } from '@/lib/types/reactions.ts';

function row(overrides: Partial<ReactionOutput> = {}) {
  return { output: makeReactionOutput('f0000000-0000-4000-8000-0000000000ff', overrides), step: 0 };
}

describe('the product columns', () => {
  it('gives every column a distinct id, since the id is what addresses a cell', () => {
    const ids = PRODUCT_COLUMNS.map((column) => column.id);
    expect(new Set(ids).size).toBe(ids.length);
  });

  /**
   * The one action a product row has. There is no `RemoveOutputRow` mutation — a product is
   * created and destroyed by editing the reaction scheme — so this is also asserting that no
   * delete column has quietly been added alongside the input table's.
   */
  it('ends on Add Batch and offers no delete', () => {
    expect(PRODUCT_COLUMNS.at(-1)?.kind).toBe('addBatch');
    expect(PRODUCT_COLUMNS.map((column) => column.kind)).not.toContain('delete');
  });

  /**
   * A missing label would render as `undefined` in the picker, and a missing colour would drop
   * the type's only visual distinction — neither throws, so neither shows up anywhere else.
   */
  it('labels and colours all three output types', () => {
    for (const type of OUTPUT_TYPES) {
      expect(OUTPUT_TYPE_LABELS[type]).toBeTruthy();
      expect(OUTPUT_TYPE_TRIGGER_CLASS[type]).toBeTruthy();
    }
    expect(OUTPUT_TYPES).toHaveLength(Object.keys(OUTPUT_TYPE_LABELS).length);
  });

  /** Reaction Step is shown 1-based, off a 0-based index into `model.reactions`. */
  it('shows the reaction step 1-based', () => {
    const column = PRODUCT_COLUMNS.find((each) => each.id === 'reactionStep');
    expect(column?.kind === 'readonly' && column.value({ ...row(), step: 1 })).toBe('2');
  });
});

describe('productHaystack', () => {
  it('covers the name, the chemical name, the formula and the compound key', () => {
    const haystack = productHaystack(row({ outputName: 'P7', chemicalName: 'Aspirin' }));
    expect(haystack).toContain('p7');
    expect(haystack).toContain('aspirin');
    expect(haystack).toContain('str-00000000-89');
  });

  /** An unknown compound has no registry fields at all — reading them would throw. */
  it('skips the registry fields of an unknown compound', () => {
    expect(productHaystack(row({ compound: { type: 'UNKNOWN', molWeight: {} } }))).toBe('p0');
  });
});
