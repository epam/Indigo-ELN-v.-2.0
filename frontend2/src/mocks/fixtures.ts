import type {ACLEntry, Attachment, UserRef} from '@/lib/types/common.ts';
import type {BuiltInDictionary, DictionaryItemRef} from '@/lib/types/dictionaries.ts';
import type {GlobalSearchResult} from '@/lib/types/search.ts';
import type {Experiment} from '@/lib/types/experiments.ts';
import {EXPERIMENT_STATUSES} from '@/lib/types/experiments.ts';
import type {Notebook, NotebookDetails} from '@/lib/types/notebooks.ts';
import type {Project, ProjectDetails, TotalCounts} from '@/lib/types/projects.ts';
import type {CurrentUser} from '@/lib/types/user.ts';

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

export function makeAttachment(name: string, overrides: Partial<Attachment> = {}): Attachment {
  return {
    id: `a77a0000-0000-4000-8000-${name.length.toString().padStart(12, '0')}`,
    name,
    size: 16_384,
    createdBy: MARK,
    createdAt: '2026-01-22T17:51:00Z',
    modifiedBy: MARK,
    modifiedAt: '2026-01-22T17:51:00Z',
    ...overrides,
  };
}

/** One of each icon group, so the extension mapping in AttachmentList is visible at a glance. */
export const ATTACHMENTS: Attachment[] = [
  makeAttachment('protocol.docx'),
  makeAttachment('yields.xlsx', { size: 248_000 }),
  makeAttachment('spectra.png', { size: 3_400_000 }),
  makeAttachment('raw-trace.dat', { size: 512 }),
];

/** Mixed levels and one inherited entry, so every branch of a Team row is reachable. */
export const PROJECT_ACL: ACLEntry[] = [
  makeAclEntry('Administrator', { level: 'AUTHOR' }),
  makeAclEntry('Mark Liu', { level: 'ADMIN' }),
  makeAclEntry('Sofia Rossi', { level: 'EDIT', inherited: true }),
  makeAclEntry('Tom Becker', { level: 'VIEW' }),
  makeAclEntry('Anna Petrova', { level: 'IMPLICIT_VIEW', inherited: true }),
];

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
    attachments: ATTACHMENTS,
    acl: PROJECT_ACL,
    currentPermissions: ['VIEW_PROJECTS', 'EDIT_PROJECTS', 'MANAGE_PROJECT_ACCESS'],
    ...overrides,
  };
}

export function makeNotebook(overrides: Partial<Notebook> = {}): Notebook {
  return {
    id: '77777777-7777-7777-7777-777777777777',
    name: '00000001',
    createdBy: ADMINISTRATOR,
    createdAt: '2026-02-08T10:05:00Z',
    modifiedBy: MARK,
    modifiedAt: '2026-05-14T13:30:00Z',
    experimentCount: 14,
    experimentCountByStatus: { OPEN: 2, COMPLETED: 8, SIGNED: 4 },
    acl: [makeAclEntry('Administrator', { level: 'AUTHOR' }), makeAclEntry('Mark Liu')],
    aclCount: 12,
    ...overrides,
  };
}

/** Mixed levels and one inherited entry, so every branch of a Team row is reachable. */
export const NOTEBOOK_ACL: ACLEntry[] = [
  makeAclEntry('Mark Liu', { level: 'AUTHOR' }),
  makeAclEntry('Administrator', { level: 'ADMIN' }),
  // Inherited from the parent project, which is where a notebook's inherited entries come from.
  makeAclEntry('Sofia Rossi', { level: 'EDIT', inherited: true }),
  makeAclEntry('Tom Becker', { level: 'VIEW' }),
];

export function makeNotebookDetails(overrides: Partial<NotebookDetails> = {}): NotebookDetails {
  const base = makeNotebook();
  return {
    ...base,
    revision: 1,
    description: '<p>Aspirin synthesis strategies, route A.</p>',
    attachments: ATTACHMENTS,
    acl: NOTEBOOK_ACL,
    currentPermissions: ['VIEW_NOTEBOOKS', 'EDIT_NOTEBOOKS', 'MANAGE_NOTEBOOK_ACCESS'],
    projectId: '11111111-1111-1111-1111-111111111111',
    projectName: 'Kinase Inhibitor Screening',
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

function makeDictionary(names: string[]): DictionaryItemRef[] {
  return names.map((name, index) => ({ id: `d1c70000-0000-4000-8000-${String(index).padStart(12, '0')}`, name }));
}

/** Keyed by the same names the API takes as its `{dictionary}` path segment. */
export const DICTIONARIES: Partial<Record<BuiltInDictionary, DictionaryItemRef[]>> = {
  THERAPEUTIC_AREA: makeDictionary(['Obesity', 'Oncology', 'Cardiology', 'Immunology', 'Neurology']),
  PROJECT_CODE: makeDictionary(['Code 1', 'Code 2', 'Code 3', 'Code 4']),
};

/**
 * The pool `users/suggest` matches against. Nils and Priya are deliberately absent from
 * `PROJECT_ACL`, so the Team card has someone left to actually add.
 */
export const USERS: UserRef[] = [
  makeUserRef('Administrator'),
  makeUserRef('Mark Liu'),
  makeUserRef('Anna Petrova'),
  makeUserRef('Sofia Rossi'),
  makeUserRef('Tom Becker'),
  makeUserRef('Nils Berg'),
  makeUserRef('Priya Raman'),
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

/** A varied page of notebooks, enough to fill a grid without repeating one card. */
export const NOTEBOOKS: Notebook[] = [
  makeNotebook(),
  makeNotebook({
    id: '88888888-8888-8888-8888-888888888888',
    name: '00000002',
    experimentCount: 3,
    experimentCountByStatus: { OPEN: 1, SIGNING: 2 },
    acl: [makeAclEntry('Sofia Rossi', { level: 'AUTHOR' })],
    aclCount: 1,
  }),
  makeNotebook({
    id: '99999999-8888-7777-6666-555555555555',
    name: '00000003',
    experimentCount: 0,
    experimentCountByStatus: {},
    acl: [makeAclEntry('Mark Liu', { level: 'AUTHOR' }), makeAclEntry('Tom Becker', { level: 'VIEW' })],
    aclCount: 4,
  }),
];

/**
 * A varied page of experiments: every status has a card, and both marked states are present,
 * so the badge variants and the star's two forms are all reachable from one story.
 */
export const EXPERIMENTS: Experiment[] = EXPERIMENT_STATUSES.map((status, index) =>
  makeExperiment({
    id: `e0000000-0000-4000-8000-${String(index).padStart(12, '0')}`,
    name: `00000001-${String(index + 1).padStart(4, '0')}`,
    status,
    marked: index % 3 === 0,
    revision: index + 1,
    acl: [makeAclEntry('Administrator', { level: 'AUTHOR' }), makeAclEntry('Mark Liu')],
    aclCount: 6,
  }),
);

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

/** A tiny valid SVG, so the reaction-scheme <img> has something real to render. */
export const REACTION_SCHEME_SVG =
  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 140 88">' +
  '<rect width="140" height="88" fill="#fff"/>' +
  '<text x="70" y="48" text-anchor="middle" font-size="12" fill="#242424">A + B &#8594; C</text>' +
  '</svg>';

export function makeSearchResult(overrides: Partial<GlobalSearchResult> = {}): GlobalSearchResult {
  return {
    id: '11111111-1111-4111-8111-111111111111',
    createdBy: ADMINISTRATOR,
    createdAt: '2026-05-04T09:00:00Z',
    modifiedBy: ADMINISTRATOR,
    modifiedAt: '2026-05-04T09:00:00Z',
    type: 'EXPERIMENT',
    name: '00000001-0001',
    title: 'Suzuki coupling of aryl bromide',
    experimentStatus: 'OPEN',
    reactionRoles: null,
    revision: 3,
    notebookCount: null,
    experimentCount: null,
    fragment: null,
    ...overrides,
  };
}

/** One of each entity type, plus enough experiments to make a second page reachable. */
export const SEARCH_RESULTS: GlobalSearchResult[] = [
  makeSearchResult({
    id: '22222222-2222-4222-8222-222222222222',
    type: 'PROJECT',
    name: 'Kinase Inhibitor Screening',
    title: null,
    experimentStatus: null,
    revision: null,
    notebookCount: 4,
    experimentCount: 37,
  }),
  makeSearchResult({
    id: '33333333-3333-4333-8333-333333333333',
    type: 'NOTEBOOK',
    name: '00000001',
    title: null,
    experimentStatus: null,
    revision: null,
    experimentCount: 12,
    createdBy: MARK,
  }),
  makeSearchResult({ reactionRoles: ['REACTANT', 'OUTPUT'] }),
  ...Array.from({ length: 24 }, (_, index) =>
    makeSearchResult({
      id: `44444444-4444-4444-8444-${String(index).padStart(12, '0')}`,
      name: `00000001-${String(index + 2).padStart(4, '0')}`,
      title: index % 3 === 0 ? null : `Amide coupling step ${index + 2}`,
      experimentStatus: index % 2 === 0 ? 'COMPLETED' : 'SIGNED',
      createdBy: index % 2 === 0 ? MARK : ADMINISTRATOR,
    }),
  ),
];
