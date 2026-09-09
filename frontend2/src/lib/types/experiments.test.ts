import { describe, expect, it } from 'vitest';

import {
  canRunWorkflow,
  EXPERIMENT_STATUS_COLOR,
  EXPERIMENT_STATUS_LABELS,
  EXPERIMENT_STATUSES,
  type ExperimentStatus,
  type WorkflowAction,
  workflowActionsFor,
} from '@/lib/types/experiments.ts';
import type { ApplicationPermission } from '@/lib/types/user.ts';

const ALL_STATUSES = Object.keys(EXPERIMENT_STATUS_LABELS) as ExperimentStatus[];

describe('EXPERIMENT_STATUS_LABELS', () => {
  it('labels CANCELLED with two Ls', () => {
    expect(EXPERIMENT_STATUS_LABELS.CANCELLED).toBe('Cancelled');
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

/**
 * `ExperimentWorkflowHandlers.doValidateStatus`, transposed — the handlers list statuses per
 * action, the header needs actions per status. Written out in full rather than derived from the
 * table under test, so a change to one has to be made deliberately in the other.
 */
const EXPECTED: Record<ExperimentStatus, WorkflowAction[]> = {
  OPEN: ['complete', 'completeAndSubmit', 'cancel'],
  REOPEN: ['complete', 'completeAndSubmit', 'cancel'],
  COMPLETED: ['submit', 'reopen'],
  REJECTED: ['submit', 'reopen'],
  SUBMITTED: ['reopen'],
  CANCELLED: ['reopen'],
  ARCHIVED: ['reopen'],
  SIGNING: [],
  SIGNED: [],
};

describe('workflowActionsFor', () => {
  it.each(ALL_STATUSES)('offers the transitions the backend allows from %s', (status) => {
    expect(workflowActionsFor({ status })).toEqual(EXPECTED[status]);
  });

  /**
   * An experiment out for signature is the signature service's to move: `ReopenExperimentHandler`
   * excludes both, and indigo-frontend offering Reopen there is a bug that 400s.
   */
  it('offers nothing while a signature is outstanding', () => {
    expect(workflowActionsFor({ status: 'SIGNING' })).toEqual([]);
    expect(workflowActionsFor({ status: 'SIGNED' })).toEqual([]);
  });

  /** One button, two statuses — resubmitting after a rejection is the same endpoint. */
  it('submits from REJECTED as well as COMPLETED', () => {
    expect(workflowActionsFor({ status: 'REJECTED' })).toContain('submit');
  });
});

describe('canRunWorkflow', () => {
  it('asks for SUBMIT_EXPERIMENTS', () => {
    expect(canRunWorkflow({ currentPermissions: ['SUBMIT_EXPERIMENTS'] })).toBe(true);
  });

  /**
   * The distinction the whole gate exists for: `AccessLevel.EDIT` grants `EDIT_EXPERIMENTS` and
   * not `SUBMIT_EXPERIMENTS`, so a collaborator who can fill in the stoichiometry table still
   * cannot complete the experiment.
   */
  it('is not implied by being able to edit', () => {
    const editor: ApplicationPermission[] = ['VIEW_EXPERIMENTS', 'EDIT_EXPERIMENTS'];
    expect(canRunWorkflow({ currentPermissions: editor })).toBe(false);
  });
});
