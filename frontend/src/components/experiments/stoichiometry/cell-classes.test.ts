import { describe, expect, it } from 'vitest';

import { determineCellClasses } from '@/components/experiments/stoichiometry/cell-classes';

import type { EnteredValue } from '@/lib/types/reactions.ts';

/** No patch since mount: the caller passes the value as its own previous. */
function unpatched(value: EnteredValue): string[] {
  return determineCellClasses(value, value);
}

describe('determineCellClasses', () => {
  it('returns nothing for an absent value', () => {
    expect(determineCellClasses(undefined, undefined)).toEqual([]);
  });

  describe('provenance', () => {
    it('marks a user-entered value', () => {
      expect(unpatched({ value: '1', source: 'U', revision: 7 })).toEqual(['text-blue-400', 'font-semibold']);
    });

    it('marks a default value', () => {
      expect(unpatched({ value: '1', source: 'D' })).toEqual(['text-neutral-700', 'italic']);
    });

    it('leaves a fixed value uncoloured, the same as a calculated one', () => {
      expect(unpatched({ value: '1', source: 'F' })).toEqual([]);
    });

    it('leaves a calculated value uncoloured, so it reads as the table default', () => {
      expect(unpatched({ value: '1', source: 'C' })).toEqual([]);
    });
  });

  describe('flashes', () => {
    it('flashes green when a calculated value changed', () => {
      const value: EnteredValue = { value: '2', source: 'C' };
      expect(determineCellClasses(value, { value: '1', source: 'C' })).toEqual([
        'animate-[flash-green_500ms_ease-in-out]',
      ]);
    });

    it('flashes green when a default value is now calculated', () => {
      const value: EnteredValue = { value: '1', source: 'C' };
      expect(determineCellClasses(value, { value: '1', source: 'D' })).toEqual([
        'animate-[flash-green_500ms_ease-in-out]',
      ]);
    });

    it('flashes green when a calculated value appears where there was none', () => {
      // The row is new, so the model before the patch has nothing for this cell.
      const value: EnteredValue = { value: '1', source: 'C' };
      expect(determineCellClasses(value, undefined)).toEqual(['animate-[flash-green_500ms_ease-in-out]']);
    });

    it('does not flash a calculated value that came back unchanged', () => {
      const value: EnteredValue = { value: '1', source: 'C' };
      expect(determineCellClasses(value, { value: '1', source: 'C' })).toEqual([]);
    });

    it('flashes red when a user-entered value is now calculated — the backend overwrote it', () => {
      const value: EnteredValue = { value: '2', source: 'C' };
      expect(determineCellClasses(value, { value: '1', source: 'U', revision: 3 })).toEqual([
        'animate-[flash-red_500ms_ease-in-out]',
      ]);
    });

    it('flashes red even when the calculated value equals what the user had entered', () => {
      const value: EnteredValue = { value: '1', source: 'C' };
      expect(determineCellClasses(value, { value: '1', source: 'U', revision: 3 })).toEqual([
        'animate-[flash-red_500ms_ease-in-out]',
      ]);
    });

    it('flashes red when a user-entered value is now empty', () => {
      expect(determineCellClasses({}, { value: '1', source: 'U', revision: 3 })).toEqual([
        'animate-[flash-red_500ms_ease-in-out]',
      ]);
      expect(determineCellClasses(undefined, { value: '1', source: 'U', revision: 3 })).toEqual([
        'animate-[flash-red_500ms_ease-in-out]',
      ]);
    });

    it('does not flash red when a calculated value is now empty', () => {
      expect(determineCellClasses(undefined, { value: '1', source: 'C' })).toEqual([]);
    });
  });

  describe('validity', () => {
    it.each(['0', '-1'])('writes %s in red', (number) => {
      expect(unpatched({ value: number, source: 'C' })).toEqual(['text-red-200']);
      expect(unpatched({ value: number, source: 'F' })).toEqual(['text-red-200']);
    });

    it('puts red after the provenance colour, so it wins', () => {
      expect(unpatched({ value: '0', source: 'D' })).toEqual(['text-neutral-700', 'italic', 'text-red-200']);
      expect(unpatched({ value: '-2', source: 'U', revision: 7 })).toEqual([
        'text-blue-400',
        'font-semibold',
        'text-red-200',
      ]);
    });

    it('leaves a small positive value alone', () => {
      expect(unpatched({ value: '0.001', source: 'C' })).toEqual([]);
    });

    it('does not mark an empty value', () => {
      expect(unpatched({})).toEqual([]);
      expect(unpatched({ source: 'L', revision: 4 })).toEqual([]);
    });
  });

  describe('suppression', () => {
    it('animates nothing on first load, when no patch has been applied yet', () => {
      expect(unpatched({ value: '1', source: 'C' })).toEqual([]);
    });

    it('never animates a user-entered value, however the patch changed it', () => {
      // The user knows what they just typed; the flash exists to point out changes they did not
      // make. This is the early return, and dropping it would flash every cell
      // the user is editing.
      const value: EnteredValue = { value: '2', source: 'U', revision: 9 };
      expect(determineCellClasses(value, { value: '1', source: 'U', revision: 4 })).toEqual([
        'text-blue-400',
        'font-semibold',
      ]);
    });

    it('does not flash an overwritten value the patch left alone', () => {
      // Overwritten by an earlier patch, and still so: the previous value is already calculated.
      const value: EnteredValue = { value: '1', source: 'C' };
      expect(determineCellClasses(value, { ...value })).toEqual([]);
    });
  });
});
