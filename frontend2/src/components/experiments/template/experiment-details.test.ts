import { describe, expect, it } from 'vitest';

import { dictionaryEdit, experimentRefsEdit, titleEdit } from '@/components/experiments/template/experiment-details';

import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { ExperimentRef } from '@/lib/types/experiments.ts';

const ONCOLOGY: DictionaryItemRef = { id: 'd1', name: 'Oncology' };
const CARDIO: DictionaryItemRef = { id: 'd2', name: 'Cardiology' };

function ref(id: string): ExperimentRef {
  return { id, name: `0000000${id}` };
}

describe('titleEdit', () => {
  it('sends nothing when the title is unchanged', () => {
    expect(titleEdit('Acetic anhydride route', 'Acetic anhydride route')).toBeNull();
  });

  it('sends nothing when a missing title is still missing', () => {
    expect(titleEdit('', undefined)).toBeNull();
  });

  it('sends the trimmed title when it changed', () => {
    expect(titleEdit('  New route  ', 'Old route')).toEqual({ value: 'New route' });
  });

  /** Trimming is what makes this converge: the trimmed value is sent, so a second blur agrees. */
  it('sends nothing when only surrounding whitespace was added', () => {
    expect(titleEdit('  Same  ', 'Same')).toBeNull();
  });

  it('clears the column when the title was emptied', () => {
    expect(titleEdit('   ', 'Old route')).toEqual({ value: null });
  });
});

describe('dictionaryEdit', () => {
  /**
   * The picker's items come from `/dictionaries/{name}`, so the object standing for the current
   * value is never the one the experiment payload carried — `===` would report a change forever.
   */
  it('sends nothing for the same item arriving as a different object', () => {
    expect(dictionaryEdit({ ...ONCOLOGY }, { ...ONCOLOGY })).toBeNull();
  });

  it('sends nothing when both sides are empty', () => {
    expect(dictionaryEdit(null, undefined)).toBeNull();
  });

  it('sends the whole ref when a different item was picked', () => {
    expect(dictionaryEdit(CARDIO, ONCOLOGY)).toEqual({ value: CARDIO });
  });

  it('sends the first pick against nothing stored', () => {
    expect(dictionaryEdit(ONCOLOGY, undefined)).toEqual({ value: ONCOLOGY });
  });

  it('clears the column when the pick was removed', () => {
    expect(dictionaryEdit(null, ONCOLOGY)).toEqual({ value: null });
  });
});

describe('experimentRefsEdit', () => {
  it('sends nothing for the same refs in the same order', () => {
    expect(experimentRefsEdit([ref('a'), ref('b')], [ref('a'), ref('b')])).toBeNull();
  });

  /** The backend field is a Set, so order carries no meaning and reordering is not an edit. */
  it('sends nothing for the same refs in a different order', () => {
    expect(experimentRefsEdit([ref('b'), ref('a')], [ref('a'), ref('b')])).toBeNull();
  });

  it('sends nothing when both lists are empty', () => {
    expect(experimentRefsEdit([], [])).toBeNull();
  });

  it('sends the whole list when one was added', () => {
    expect(experimentRefsEdit([ref('a'), ref('b')], [ref('a')])).toEqual({ value: [ref('a'), ref('b')] });
  });

  /** Emptying sends `[]`, not null: the list fields have no null state. */
  it('sends an empty list when the last one was removed', () => {
    expect(experimentRefsEdit([], [ref('a')])).toEqual({ value: [] });
  });
});
