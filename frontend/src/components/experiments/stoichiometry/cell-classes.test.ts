import { describe, expect, it } from 'vitest';

import { determineCellClasses } from '@/components/experiments/stoichiometry/cell-classes';

import type { EnteredValue } from '@/lib/types/reactions.ts';

/** No patch since mount: the caller passes the value as its own previous. */
function unpatched(value: EnteredValue<string>): string[] {
  return determineCellClasses(value, value);
}

describe('determineCellClasses', () => {
  it('returns nothing for an absent value', () => {
    expect(determineCellClasses(undefined, undefined)).toEqual([]);
  });

  describe('provenance', () => {
    it('marks a user-entered value, whose source is the revision number rather than a name', () => {
      expect(unpatched({ value: '1', source: 7 })).toEqual(['text-blue-400', 'font-semibold']);
    });

    it('marks a default value', () => {
      expect(unpatched({ value: '1', source: 'default' })).toEqual(['text-neutral-700', 'italic']);
    });

    it('leaves a fixed value uncoloured, the same as a calculated one', () => {
      expect(unpatched({ value: '1', source: 'fixed' })).toEqual([]);
    });

    it('leaves a calculated value uncoloured, so it reads as the table default', () => {
      expect(unpatched({ value: '1', source: 'calculated' })).toEqual([]);
    });
  });

  describe('flashes', () => {
    it('flashes green when a calculated value changed', () => {
      const value: EnteredValue<string> = { value: '2', source: 'calculated' };
      expect(determineCellClasses(value, { value: '1', source: 'calculated' })).toEqual([
        'animate-[flash-green_500ms_ease-in-out]',
      ]);
    });

    it('flashes green when a value that was entered by hand is now calculated', () => {
      const value: EnteredValue<string> = { value: '1', source: 'calculated' };
      expect(determineCellClasses(value, { value: '1', source: 3 })).toEqual([
        'animate-[flash-green_500ms_ease-in-out]',
      ]);
    });

    it('flashes green when a calculated value appears where there was none', () => {
      // The row is new, so the model before the patch has nothing for this cell.
      const value: EnteredValue<string> = { value: '1', source: 'calculated' };
      expect(determineCellClasses(value, undefined)).toEqual(['animate-[flash-green_500ms_ease-in-out]']);
    });

    it('does not flash a calculated value that came back unchanged', () => {
      const value: EnteredValue<string> = { value: '1', source: 'calculated' };
      expect(determineCellClasses(value, { value: '1', source: 'calculated' })).toEqual([]);
    });

    it('flashes red when the backend overwrote a value', () => {
      const value: EnteredValue<string> = { value: '1', source: 'calculated', overwritten: true };
      expect(determineCellClasses(value, undefined)).toEqual(['animate-[flash-red_500ms_ease-in-out]']);
    });

    it('keeps the provenance colour alongside a flash', () => {
      const value: EnteredValue<string> = { value: '1', source: 'default', overwritten: true };
      expect(determineCellClasses(value, undefined)).toEqual([
        'text-neutral-700',
        'italic',
        'animate-[flash-red_500ms_ease-in-out]',
      ]);
    });

    it('prefers red over green — an overwrite is the more important of the two', () => {
      const value: EnteredValue<string> = { value: '2', source: 'calculated', overwritten: true };
      expect(determineCellClasses(value, { value: '1', source: 'calculated' })).toEqual([
        'animate-[flash-red_500ms_ease-in-out]',
      ]);
    });
  });

  describe('validity', () => {
    it.each(['0', '-1'])('writes %s in red', (number) => {
      expect(unpatched({ value: number, source: 'calculated' })).toEqual(['text-red-200']);
      expect(unpatched({ value: number, source: 'fixed' })).toEqual(['text-red-200']);
    });

    it('puts red after the provenance colour, so it wins', () => {
      expect(unpatched({ value: '0', source: 'default' })).toEqual(['text-neutral-700', 'italic', 'text-red-200']);
      expect(unpatched({ value: '-2', source: 7 })).toEqual(['text-blue-400', 'font-semibold', 'text-red-200']);
    });

    it('leaves a small positive value alone', () => {
      expect(unpatched({ value: '0.001', source: 'calculated' })).toEqual([]);
    });

    it('does not mark an empty value', () => {
      expect(unpatched({ source: 'calculated' })).toEqual([]);
    });
  });

  describe('suppression', () => {
    it('animates nothing on first load, when no patch has been applied yet', () => {
      expect(unpatched({ value: '1', source: 'calculated', overwritten: true })).toEqual([]);
    });

    it('never animates a user-entered value, however the patch changed it', () => {
      // The user knows what they just typed; the flash exists to point out changes they did not
      // make. This is the early return, and dropping it would flash every cell
      // the user is editing.
      const value: EnteredValue<string> = { value: '2', source: 9, overwritten: true };
      expect(determineCellClasses(value, { value: '1', source: 4 })).toEqual(['text-blue-400', 'font-semibold']);
    });

    it('does not flash an overwritten value the patch left alone', () => {
      // Overwritten by an earlier patch, and still so: the flag is state, the flash is news.
      const value: EnteredValue<string> = { value: '1', source: 'calculated', overwritten: true };
      expect(determineCellClasses(value, { ...value })).toEqual([]);
    });

    it('flashes red when the patch only set the overwritten flag', () => {
      const value: EnteredValue<string> = { value: '1', source: 'calculated', overwritten: true };
      expect(determineCellClasses(value, { value: '1', source: 'calculated' })).toEqual([
        'animate-[flash-red_500ms_ease-in-out]',
      ]);
    });
  });
});
