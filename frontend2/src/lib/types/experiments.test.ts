import { describe, expect, it } from 'vitest';

import {
  EXPERIMENT_STATUS_COLOR,
  EXPERIMENT_STATUS_DISPLAY,
  EXPERIMENT_STATUSES,
  type ExperimentStatus,
} from '@/lib/types/experiments.ts';

const ALL_STATUSES = Object.keys(EXPERIMENT_STATUS_DISPLAY) as ExperimentStatus[];

describe('EXPERIMENT_STATUS_DISPLAY', () => {
  it('labels CANCELLED with two Ls', () => {
    expect(EXPERIMENT_STATUS_DISPLAY.CANCELLED).toBe('Cancelled');
  });
});

describe('EXPERIMENT_STATUSES', () => {
  it('lists all nine statuses exactly once', () => {
    expect(EXPERIMENT_STATUSES).toHaveLength(9);
    expect(new Set(EXPERIMENT_STATUSES).size).toBe(9);
    expect(new Set(EXPERIMENT_STATUSES)).toEqual(new Set(ALL_STATUSES));
  });
});

describe('EXPERIMENT_STATUS_COLOR', () => {
  it('colours every status, so a strip cell is never unstyled', () => {
    for (const status of ALL_STATUSES) {
      expect(EXPERIMENT_STATUS_COLOR[status]).toBeTruthy();
    }
  });
});
