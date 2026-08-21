import { describe, expect, it } from 'vitest';

import { STATUS_GROUPS, statusGroupOf, sumStatuses } from '@/lib/types/common.ts';
import {
  EXPERIMENT_STATUS_DISPLAY,
  type ExperimentStatus,
  type ExperimentStatusCounts,
} from '@/lib/types/experiments.ts';

describe('EXPERIMENT_STATUS_DISPLAY', () => {
  it('labels CANCELLED with two Ls, unlike the design cell below', () => {
    expect(EXPERIMENT_STATUS_DISPLAY.CANCELLED).toBe('Cancelled');
  });
});

describe('sumStatuses', () => {
  const counts: ExperimentStatusCounts = { OPEN: 2, REOPEN: 3, SIGNED: 1 };

  it('sums the statuses that share a design cell', () => {
    const open = STATUS_GROUPS.find((group) => group.key === 'open');
    expect(sumStatuses(counts, open!.statuses)).toBe(5);
  });

  it('treats omitted buckets as zero', () => {
    const rejected = STATUS_GROUPS.find((group) => group.key === 'rejected');
    expect(sumStatuses(counts, rejected!.statuses)).toBe(0);
  });

  it('returns zero when the whole map is missing', () => {
    expect(sumStatuses(undefined, ['OPEN'])).toBe(0);
  });
});

describe('statusGroupOf', () => {
  it('maps every status to a group, so the badge variant is never undefined', () => {
    for (const status of Object.keys(EXPERIMENT_STATUS_DISPLAY) as ExperimentStatus[]) {
      expect(statusGroupOf(status)).toBeDefined();
    }
  });

  it('keeps the design cell spelling of Canceled', () => {
    expect(statusGroupOf('CANCELLED').label).toBe('Canceled');
  });
});

describe('STATUS_GROUPS', () => {
  it('covers all nine API statuses across the six design cells', () => {
    const covered = STATUS_GROUPS.flatMap((group) => group.statuses);
    expect(covered).toHaveLength(9);
    expect(new Set(covered).size).toBe(9);
    expect(new Set(covered)).toEqual(new Set(Object.keys(EXPERIMENT_STATUS_DISPLAY)));
  });
});
