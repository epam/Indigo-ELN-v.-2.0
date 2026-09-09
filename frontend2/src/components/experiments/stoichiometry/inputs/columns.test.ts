import { describe, expect, it } from 'vitest';

import {
  COMPOUND_COLUMNS,
  SAMPLE_COLUMNS,
  SAMPLE_INDENT_SPAN,
  shortBatchNumber,
} from '@/components/experiments/stoichiometry/inputs/columns';

describe('the host grid', () => {
  /**
   * The invariant the whole layout rests on. Both levels live in one table, so a sample row has
   * to cover exactly as many host columns as a compound row does — the compound columns plus the
   * chevron column, which has no entry in the list.
   *
   * Worth its own test because getting it wrong does not throw: the browser silently pads the
   * short row or drops the overflow, and the result looks like a styling bug rather than an
   * arithmetic one. It also fails the moment a column is added to one list and not the other,
   * which is exactly when this is easy to get wrong.
   */
  it('has a sample row covering exactly the compound columns plus the chevron', () => {
    const spanned = SAMPLE_INDENT_SPAN + SAMPLE_COLUMNS.reduce((sum, column) => sum + column.span, 0);
    expect(spanned).toBe(COMPOUND_COLUMNS.length + 1);
  });

  /** Constraint 1: the indent ends where Batch # begins, so the two Batch # columns line up. */
  it('indents a sample row past exactly the columns before Batch #', () => {
    // +1 for the chevron column, which precedes every entry in the list.
    expect(COMPOUND_COLUMNS.findIndex((column) => column.id === 'batches') + 1).toBe(SAMPLE_INDENT_SPAN);
  });

  /** Constraint 2: both levels end on the same host column, so the right borders align. */
  it('ends both levels on a delete column', () => {
    expect(COMPOUND_COLUMNS.at(-1)?.id).toBe('delete');
    expect(SAMPLE_COLUMNS.at(-1)?.id).toBe('delete');
    expect(SAMPLE_COLUMNS.at(-1)?.span).toBe(1);
  });

  /**
   * The spacers only work by being empty — a spacer that acquired content would have a minimum
   * width of its own and stop absorbing its neighbour's overflow, which is the one thing it is
   * there to do.
   */
  it('leaves the spacer columns with nothing in them', () => {
    const spacers = COMPOUND_COLUMNS.filter((column) => column.kind === 'spacer');
    expect(spacers.map((column) => column.id)).toEqual(['hazardSpacer', 'commentSpacer']);
    for (const spacer of spacers) {
      expect(spacer.header).toBe('');
      expect(spacer.minWidth).toBeUndefined();
    }
  });
});

describe('shortBatchNumber', () => {
  it('takes the trailing ordinal without its padding', () => {
    expect(shortBatchNumber('20260101-0001-003')).toBe('3');
    expect(shortBatchNumber('20260101-0001-012')).toBe('12');
  });

  it('has nothing to show for a batch with no number yet', () => {
    expect(shortBatchNumber(undefined)).toBeUndefined();
  });
});
