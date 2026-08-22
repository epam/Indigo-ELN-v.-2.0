import type { ACLEntry, UserRef } from '@/lib/types/common.ts';
import type { Experiment } from '@/lib/types/experiments.ts';
import type { Project, ProjectDetails, TotalCounts } from '@/lib/types/projects.ts';
import type { CurrentUser } from '@/lib/types/user.ts';

export function makeUserRef(displayName: string): UserRef {
  return { username: displayName.toLowerCase().replace(/\s+/g, '.'), displayName };
}

export function makeAclEntry(displayName: string, overrides: Partial<ACLEntry> = {}): ACLEntry {
  return { ...makeUserRef(displayName), level: 'EDIT', inherited: false, ...overrides };
}

/** Every permission by default, so the sidebar renders its full menu. */
export function makeCurrentUser(overrides: Partial<CurrentUser> = {}): CurrentUser {
  return {
    id: '99999999-9999-9999-9999-999999999999',
    username: 'anna.petrova',
    displayName: 'Administrator',
    permissions: ['VIEW_PROJECTS', 'CREATE_PROJECTS', 'EDIT_PROJECTS', 'MANAGE_DICTIONARIES', 'SIGN_EXPERIMENTS'],
    ...overrides,
  };
}

const ADMINISTRATOR = makeUserRef('Administrator');
const MARK = makeUserRef('Mark Liu');

export function makeProject(overrides: Partial<Project> = {}): Project {
  return {
    id: '11111111-1111-1111-1111-111111111111',
    name: 'Kinase Inhibitor Screening',
    createdBy: ADMINISTRATOR,
    createdAt: '2026-01-14T09:20:00Z',
    modifiedBy: MARK,
    modifiedAt: '2026-06-02T16:45:00Z',
    notebookCount: 7,
    experimentCount: 42,
    experimentCountByStatus: { OPEN: 12, COMPLETED: 18, SIGNED: 9, REJECTED: 3 },
    acl: [makeAclEntry('Administrator', { level: 'AUTHOR' }), makeAclEntry('Mark Liu')],
    aclCount: 2,
    ...overrides,
  };
}

export function makeExperiment(overrides: Partial<Experiment> = {}): Experiment {
  return {
    id: '22222222-2222-2222-2222-222222222222',
    name: '00000001-0001',
    createdBy: ADMINISTRATOR,
    createdAt: '2026-03-01T11:00:00Z',
    modifiedBy: ADMINISTRATOR,
    modifiedAt: '2026-03-04T08:15:00Z',
    status: 'OPEN',
    marked: true,
    revision: 1,
    acl: [makeAclEntry('Administrator', { level: 'AUTHOR' })],
    aclCount: 1,
    ...overrides,
  };
}

export function makeProjectDetails(overrides: Partial<ProjectDetails> = {}): ProjectDetails {
  // ProjectDetailsDTO carries acl but no aclCount, unlike the list's ProjectDTO — which
  // is structurally harmless here, so the extra key is simply left in place.
  const base = makeProject();
  return {
    ...base,
    revision: 1,
    keywords: ['kinase', 'screening'],
    literature: '<p>Smith et al., <em>J. Med. Chem.</em> 2024</p>',
    description: '<p>Screening cascade for the kinase series.</p>',
    attachments: [],
    currentPermissions: ['VIEW_PROJECTS', 'EDIT_PROJECTS'],
    ...overrides,
  };
}

/**
 * Enough keywords to overflow one page of the suggestion list, so the PageUp/PageDown
 * behaviour in MultiCombobox is actually exercisable.
 */
export const KEYWORDS: string[] = [
  'kinase',
  'kinase-inhibitor',
  'kinetics',
  'screening',
  'scale-up',
  'selectivity',
  'solubility',
  'stability',
  'stereochemistry',
  'sulfonamide',
  'suzuki-coupling',
  'synthesis',
  'crystallisation',
  'chromatography',
  'catalysis',
];

export function makeTotalCounts(overrides: Partial<TotalCounts> = {}): TotalCounts {
  return {
    projects: 24,
    notebooks: 118,
    experiments: 903,
    experimentsByStatus: {
      OPEN: 210,
      REOPEN: 18,
      COMPLETED: 340,
      SIGNING: 27,
      SUBMITTED: 44,
      SIGNED: 190,
      REJECTED: 31,
      CANCELLED: 22,
      ARCHIVED: 21,
    },
    ...overrides,
  };
}

/** A varied page of projects, enough to fill a grid without repeating one card. */
export const PROJECTS: Project[] = [
  makeProject(),
  makeProject({
    id: '33333333-3333-3333-3333-333333333333',
    name: 'Fragment Library Expansion',
    notebookCount: 3,
    experimentCount: 11,
    experimentCountByStatus: { OPEN: 2, SIGNING: 4, ARCHIVED: 5 },
    acl: [
      makeAclEntry('Mark Liu', { level: 'AUTHOR' }),
      makeAclEntry('Sofia Rossi'),
      makeAclEntry('Tom Becker', { level: 'VIEW' }),
    ],
    aclCount: 9,
  }),
  makeProject({
    id: '44444444-4444-4444-4444-444444444444',
    name: 'Route Scouting — Intermediate B',
    notebookCount: 0,
    experimentCount: 0,
    experimentCountByStatus: {},
    acl: [makeAclEntry('Sofia Rossi', { level: 'AUTHOR' })],
    aclCount: 1,
  }),
];

export const MARKED_EXPERIMENTS: Experiment[] = [
  makeExperiment(),
  makeExperiment({
    id: '55555555-5555-5555-5555-555555555555',
    name: '00000001-0012',
    status: 'SIGNING',
  }),
  makeExperiment({
    id: '66666666-6666-6666-6666-666666666666',
    name: '12345678-0100',
    status: 'REJECTED',
  }),
];
