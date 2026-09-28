import { describe, expect, it } from 'vitest';

import { hasDiff, isGroup, revisionDateLabel, revisionLabel } from '@/components/experiments/template/version-history';
import type { RevisionSummary } from '@/lib/types/revisions.ts';

const USER = { username: 'achen', displayName: 'A. Chen' };

function leaf(revision: number, overrides: Partial<RevisionSummary> = {}): RevisionSummary {
  return { user: USER, summary: 'Add empty input', date: '2026-09-03T14:02:00Z', revision, ...overrides };
}

/** What `ExperimentMapper.revisionGroupToSummary` builds: a span plus the revisions it covers. */
function group(): RevisionSummary {
  return {
    user: USER,
    summary: 'Edited experiment',
    date: '2026-09-03T14:02:00Z',
    dateTo: '2026-09-03T14:31:00Z',
    revision: 8,
    revisionTo: 12,
    details: [leaf(8), leaf(12, { date: '2026-09-03T14:31:00Z' })],
  };
}

describe('isGroup', () => {
  it('is true only for an entry that carries children', () => {
    expect(isGroup(group())).toBe(true);
    expect(isGroup(leaf(3))).toBe(false);
  });

  /** `details` is absent rather than empty on a single revision, but neither is a group. */
  it('treats an empty details array as no group', () => {
    expect(isGroup(leaf(3, { details: [] }))).toBe(false);
  });
});

describe('revisionLabel', () => {
  it('shows a single revision as its number', () => {
    expect(revisionLabel(leaf(12))).toBe('12');
  });

  it('shows a grouped edit session as a range', () => {
    expect(revisionLabel(group())).toBe('8–12');
  });
});

describe('revisionDateLabel', () => {
  it('shows one instant for a single revision', () => {
    expect(revisionDateLabel(leaf(12))).toBe(formatted('2026-09-03T14:02:00Z'));
  });

  it('shows the span a grouped edit session covers', () => {
    expect(revisionDateLabel(group())).toBe(
      `${formatted('2026-09-03T14:02:00Z')} → ${formatted('2026-09-03T14:31:00Z')}`,
    );
  });
});

describe('hasDiff', () => {
  /** Revision 1 is the experiment being created; the endpoint refuses it with `@Min(2)`. */
  it('is false for revision 1', () => {
    expect(hasDiff(leaf(1, { summary: 'Experiment created' }))).toBe(false);
  });

  it('is false for a group row, whose diffs belong to its children', () => {
    expect(hasDiff(group())).toBe(false);
  });

  it('is true for any other leaf', () => {
    expect(hasDiff(leaf(2))).toBe(true);
  });
});

/**
 * The expected text, built the way the helper does rather than written out: the format is
 * `Intl`'s, so hard-coding it would pin the test to whichever timezone the runner is in.
 */
function formatted(iso: string): string {
  return new Intl.DateTimeFormat('en-GB', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(iso));
}
