import { describe, expect, it } from 'vitest';

import {
  BATCH_COLUMNS,
  batchHaystack,
  isSampleProtected,
} from '@/components/experiments/stoichiometry/batches/columns';
import { makeReactionOutput, makeReactionOutputSample } from '@/mocks/fixtures';

import type { BatchRow } from '@/components/experiments/stoichiometry/batches/columns';
import type { ReactionOutput, ReactionOutputSample, SampleRegistrationStatus } from '@/lib/types/reactions.ts';
import { REGISTRATION_STATUS_LABELS } from '@/lib/types/reactions.ts';

const STATUSES: SampleRegistrationStatus[] = ['IN_PROGRESS', 'FAILED', 'REGISTERED'];

function row(sample: Partial<ReactionOutputSample> = {}, output: Partial<ReactionOutput> = {}): BatchRow {
  return {
    output: makeReactionOutput('f0000000-0000-4000-8000-0000000000ff', output),
    sample: makeReactionOutputSample('f1000000-0000-4000-8000-0000000000ff', sample),
    step: 0,
  };
}

describe('the batch columns', () => {
  it('gives every column a distinct id, since the id is what addresses a cell', () => {
    const ids = BATCH_COLUMNS.map((column) => column.id);
    expect(new Set(ids).size).toBe(ids.length);
  });

  /** The three row actions, in the order the mockup puts them, and nothing after them. */
  it('ends on the three action columns', () => {
    expect(BATCH_COLUMNS.slice(-3).map((column) => column.kind)).toEqual(['sync', 'register', 'delete']);
  });

  /**
   * Sync is the one column keyed by the **output** anchor — `SetOutputRowIntended` names the
   * product row, and a sample anchor there resolves to nothing and the call 400s.
   */
  it('keys Sync with Products on the output anchor and everything else on the batch', () => {
    const batch = row();
    const sync = BATCH_COLUMNS.find((column) => column.kind === 'sync');
    const remove = BATCH_COLUMNS.find((column) => column.kind === 'delete');

    expect(sync?.kind === 'sync' && sync.mutation(batch)).toEqual({
      type: 'SetOutputRowIntended',
      anchor: batch.output.anchor,
      intended: true,
    });
    expect(remove?.kind === 'delete' && remove.mutation(batch)).toEqual({
      type: 'RemoveProductSample',
      anchor: batch.sample.anchor,
    });
  });

  /** A missing label would render as `undefined`, which throws nowhere and shows everywhere. */
  it('labels every registration status', () => {
    for (const status of STATUSES) expect(REGISTRATION_STATUS_LABELS[status]).toBeTruthy();
    expect(Object.keys(REGISTRATION_STATUS_LABELS)).toHaveLength(STATUSES.length);
  });

  /** A batch that was never sent reads "None" — an empty cell would look like a missing value. */
  it('reads an unsent batch as None', () => {
    const column = BATCH_COLUMNS.find((each) => each.id === 'regStatus');
    expect(column?.kind === 'readonly' && column.value(row())).toBe('None');
    expect(column?.kind === 'readonly' && column.value(row({ registrationStatus: 'IN_PROGRESS' }))).toBe('In Progress');
  });

  /** Reaction Step is shown 1-based, off a 0-based index into `model.reactions`. */
  it('shows the reaction step 1-based', () => {
    const column = BATCH_COLUMNS.find((each) => each.id === 'reactionStep');
    expect(column?.kind === 'readonly' && column.value({ ...row(), step: 1 })).toBe('2');
  });

  /**
   * Total Moles carries the whole `EnteredValue`, not its `value` — indigo-frontend passes the
   * bare string here and only here, which is why its unit picker never shows the saved unit.
   */
  it('gives Total Moles the whole entered value, unit included', () => {
    const column = BATCH_COLUMNS.find((each) => each.id === 'actualMol');
    const actualMol = { value: '1.35', unit: 'MMOL' as const, source: 'calculated' as const };
    expect(column?.kind === 'numeric' && column.value(row({ actualMol }))).toEqual(actualMol);
  });
});

describe('isSampleProtected', () => {
  /**
   * `FAILED` is deliberately not protected: a failed registration is meant to be retried, and the
   * batch is still the user's to delete.
   */
  it('protects a batch that is registered or in flight, but not one that failed', () => {
    expect(isSampleProtected(makeReactionOutputSample('a'))).toBe(false);
    expect(isSampleProtected(makeReactionOutputSample('a', { registrationStatus: 'FAILED' }))).toBe(false);
    expect(isSampleProtected(makeReactionOutputSample('a', { registrationStatus: 'IN_PROGRESS' }))).toBe(true);
    expect(isSampleProtected(makeReactionOutputSample('a', { registrationStatus: 'REGISTERED' }))).toBe(true);
  });
});

describe('batchHaystack', () => {
  it('covers the batch number, the product name, the compound key and the status', () => {
    const haystack = batchHaystack(
      row({ nbkBatchNumber: '20260101-0001-007', registrationStatus: 'REGISTERED' }, { outputName: 'P7' }),
    );
    expect(haystack).toContain('007');
    expect(haystack).toContain('p7');
    expect(haystack).toContain('str-00000000-89');
    expect(haystack).toContain('registered');
  });

  /** An unknown compound has no registry fields at all — reading them would throw. */
  it('skips the registry fields of an unknown compound', () => {
    const haystack = batchHaystack(row({}, { compound: { type: 'UNKNOWN', molWeight: {} } }));
    expect(haystack).not.toContain('str-');
  });
});
