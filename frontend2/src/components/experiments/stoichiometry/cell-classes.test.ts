import { describe, expect, it } from 'vitest';

import { determineCellClasses } from '@/components/experiments/stoichiometry/cell-classes';

import type { EnteredValue } from '@/lib/types/reactions.ts';

const EMPTY: ReadonlyMap<unknown, unknown> = new Map();

/** A patch landed, and this value is one of the nodes it rebuilt. */
function patched(value: EnteredValue<string>, previous: EnteredValue<string> | null) {
  return new Map<unknown, unknown>(previous ? [[value, previous]] : [[value, {}]]);
}

describe('determineCellClasses', () => {
  it('returns nothing for an absent value', () => {
    expect(determineCellClasses(undefined, EMPTY)).toEqual([]);
  });

  describe('provenance', () => {
    it('marks a user-entered value, whose source is the revision number rather than a name', () => {
      expect(determineCellClasses({ value: '1', source: 7 }, EMPTY)).toEqual(['text-blue-400', 'font-semibold']);
    });

    it('marks a default value', () => {
      expect(determineCellClasses({ value: '1', source: 'default' }, EMPTY)).toEqual(['text-neutral-700', 'italic']);
    });

    it('marks a fixed value', () => {
      expect(determineCellClasses({ value: '1', source: 'fixed' }, EMPTY)).toEqual(['text-violet-400']);
    });

    it('leaves a calculated value uncoloured, so it reads as the table default', () => {
      expect(determineCellClasses({ value: '1', source: 'calculated' }, EMPTY)).toEqual([]);
    });
  });

  describe('flashes', () => {
    it('flashes green when a calculated value changed', () => {
      const value: EnteredValue<string> = { value: '2', source: 'calculated' };
      expect(determineCellClasses(value, patched(value, { value: '1', source: 'calculated' }))).toEqual([
        'animate-[flash-green_500ms_ease-in-out]',
      ]);
    });

    it('flashes green when a value that was entered by hand is now calculated', () => {
      const value: EnteredValue<string> = { value: '1', source: 'calculated' };
      expect(determineCellClasses(value, patched(value, { value: '1', source: 3 }))).toEqual([
        'animate-[flash-green_500ms_ease-in-out]',
      ]);
    });

    it('does not flash a calculated value that came back unchanged', () => {
      const value: EnteredValue<string> = { value: '1', source: 'calculated' };
      expect(determineCellClasses(value, patched(value, { value: '1', source: 'calculated' }))).toEqual([]);
    });

    it('flashes red when the backend overwrote a value', () => {
      const value: EnteredValue<string> = { value: '1', source: 'calculated', overwritten: true };
      expect(determineCellClasses(value, patched(value, null))).toEqual(['animate-[flash-red_500ms_ease-in-out]']);
    });

    it('keeps the provenance colour alongside a flash', () => {
      const value: EnteredValue<string> = { value: '1', source: 'default', overwritten: true };
      expect(determineCellClasses(value, patched(value, null))).toEqual([
        'text-neutral-700',
        'italic',
        'animate-[flash-red_500ms_ease-in-out]',
      ]);
    });

    it('prefers red over green — an overwrite is the more important of the two', () => {
      const value: EnteredValue<string> = { value: '2', source: 'calculated', overwritten: true };
      expect(determineCellClasses(value, patched(value, { value: '1', source: 'calculated' }))).toEqual([
        'animate-[flash-red_500ms_ease-in-out]',
      ]);
    });
  });

  describe('suppression', () => {
    it('animates nothing on first load, when no patch has been applied yet', () => {
      expect(determineCellClasses({ value: '1', source: 'calculated', overwritten: true }, EMPTY)).toEqual([]);
    });

    it('never animates a user-entered value, however the patch changed it', () => {
      // The user knows what they just typed; the flash exists to point out changes they did not
      // make. This is the early return in the original, and dropping it would flash every cell
      // the user is editing.
      const value: EnteredValue<string> = { value: '2', source: 9, overwritten: true };
      expect(determineCellClasses(value, patched(value, { value: '1', source: 4 }))).toEqual([
        'text-blue-400',
        'font-semibold',
      ]);
    });

    it('does not flash a node the patch left alone, even while other nodes changed', () => {
      const untouched: EnteredValue<string> = { value: '1', source: 'calculated' };
      const other: EnteredValue<string> = { value: '9', source: 'calculated' };
      expect(determineCellClasses(untouched, patched(other, { value: '8', source: 'calculated' }))).toEqual([
        'animate-[flash-green_500ms_ease-in-out]',
      ]);
    });
  });
});
