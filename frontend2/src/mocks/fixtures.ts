import type { ACLEntry, Attachment, UserRef } from '@/lib/types/common.ts';
import type { BuiltInDictionary, DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { GlobalSearchResult } from '@/lib/types/search.ts';
import type { Experiment, ExperimentDetails, ExperimentRef } from '@/lib/types/experiments.ts';
import { EXPERIMENT_STATUSES } from '@/lib/types/experiments.ts';
import type { Notebook, NotebookDetails } from '@/lib/types/notebooks.ts';
import type { Project, ProjectDetails, TotalCounts } from '@/lib/types/projects.ts';
import type {
  CompoundRef,
  EnteredValue,
  Reaction,
  ReactionInput,
  ReactionInputSample,
  ReactionOutput,
  ReactionOutputSample,
} from '@/lib/types/reactions.ts';
import type { CurrentUser } from '@/lib/types/user.ts';
import type { TemplateDetails } from '@/lib/types/templates.ts';

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

export const TEMPLATE_ID = '33333333-3333-3333-3333-333333333333';

/**
 * The four tabs of the design, exercising all six component types and every stoichiometry flag —
 * so one fixture is enough to see the whole experiment screen.
 */
export function makeTemplateDetails(overrides: Partial<TemplateDetails> = {}): TemplateDetails {
  return {
    id: TEMPLATE_ID,
    name: 'Default',
    createdBy: ADMINISTRATOR,
    createdAt: '2026-01-05T09:00:00Z',
    modifiedBy: ADMINISTRATOR,
    modifiedAt: '2026-01-05T09:00:00Z',
    templateTabs: [
      {
        name: 'Experiment Info',
        components: [
          { type: 'experimentDetails' },
          {
            type: 'stoichiometryTable',
            reactionScheme: true,
            reactantsReagentsSolvents: true,
            intendedProducts: true,
          },
        ],
      },
      { name: 'Attachments', components: [{ type: 'experimentDescription' }, { type: 'attachments' }] },
      { name: 'Summary', components: [{ type: 'batches' }] },
      { name: 'Previous Versions', components: [{ type: 'versionHistory' }] },
    ],
    ...overrides,
  };
}

/** Mixed levels and an inherited entry, as NOTEBOOK_ACL is — an experiment inherits from both. */
export const EXPERIMENT_ACL: ACLEntry[] = [
  makeAclEntry('Administrator', { level: 'AUTHOR' }),
  makeAclEntry('Mark Liu', { level: 'ADMIN' }),
  makeAclEntry('Sofia Rossi', { level: 'EDIT', inherited: true }),
  makeAclEntry('Tom Becker', { level: 'VIEW', inherited: true }),
];

/**
 * What `/experiments/suggest` draws from. The ids match `EXPERIMENTS` where they overlap, so a
 * suggestion and a listed experiment are the same thing — including the one being edited, which
 * `ExperimentRefsCombobox` has to filter out itself.
 */
export const EXPERIMENT_REFS: ExperimentRef[] = [
  { id: '22222222-2222-2222-2222-222222222222', name: '00000001-0001' },
  { id: '55555555-5555-5555-5555-555555555555', name: '00000001-0012' },
  { id: '66666666-6666-6666-6666-666666666666', name: '12345678-0100' },
  { id: 'a0000000-0000-4000-8000-000000000001', name: '00000112-0006' },
  { id: 'a0000000-0000-4000-8000-000000000002', name: '00000112-0012' },
  { id: 'a0000000-0000-4000-8000-000000000003', name: '00000112-0031' },
];

/**
 * A one-step rxnfile. Structurally a real RXN V2000 — a `$RXN` header, a reactant count and
 * two `$MOL` blocks — rather than chemistry worth reading: Storybook aliases Ketcher to a stub
 * that answers with a canned benzene SVG, so what matters here is the shape, not the atoms.
 */
export const REACTION_RXNFILE = [
  '$RXN',
  '',
  '  Ketcher',
  '',
  '  1  1',
  '$MOL',
  '',
  '  Ketcher',
  '',
  '  1  0  0  0  0  0            999 V2000',
  '    0.0000    0.0000    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0',
  'M  END',
  '$MOL',
  '',
  '  Ketcher',
  '',
  '  1  0  0  0  0  0            999 V2000',
  '    0.0000    0.0000    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0',
  'M  END',
].join('\n');

function makeDictionary(names: string[]): DictionaryItemRef[] {
  return names.map((name, index) => ({ id: `d1c70000-0000-4000-8000-${String(index).padStart(12, '0')}`, name }));
}

/** Keyed by the same names the API takes as its `{dictionary}` path segment. */
export const DICTIONARIES: Partial<Record<BuiltInDictionary, DictionaryItemRef[]>> = {
  THERAPEUTIC_AREA: makeDictionary(['Obesity', 'Oncology', 'Cardiology', 'Immunology', 'Neurology']),
  PROJECT_CODE: makeDictionary(['Code 1', 'Code 2', 'Code 3', 'Code 4']),
  // Read by the stoichiometry table's Salt Code and Hazard Comments cells.
  SALT_CODE: makeDictionary(['HCl', 'Na', 'K', 'Free base']),
  HEALTH_HAZARD: makeDictionary(['Corrosive', 'Flammable', 'Irritant', 'Toxic', 'Oxidiser']),
};

/** A short helper for the `EnteredValue`s below — every numeric cell in the model is one. */
function entered<U extends string>(value: string, unit: U, source: EnteredValue<U>['source']): EnteredValue<U> {
  return { value, unit, source };
}

/**
 * A registry compound: molecular weight, formula and CAS all come from the compound service,
 * which is why so much of a `STORED` row is read-only.
 */
function storedCompound(overrides: Partial<Extract<CompoundRef, { type: 'STORED' }>> = {}): CompoundRef {
  return {
    type: 'STORED',
    compoundID: 'c0000000-0000-4000-8000-000000000001',
    formula: 'C<sub>4</sub>H<sub>6</sub>O<sub>3</sub>',
    molWeight: entered('102.09', 'G_PER_MOL', 'fixed'),
    exactMass: entered('102.0317', 'NO_UNIT', 'fixed'),
    calculatedBatchMF: 'C4H6O3',
    compoundKey: 'STR-00000000-89',
    casNumber: '108-24-7',
    ...overrides,
  };
}

export function makeReactionInputSample(
  anchor: string,
  overrides: Partial<ReactionInputSample> = {},
): ReactionInputSample {
  return {
    anchor,
    nbkBatchNumber: '20260101-0001-001',
    purity: entered('100', 'NO_UNIT', 'default'),
    healthHazards: [],
    ...overrides,
  };
}

export function makeReactionInput(anchor: string, overrides: Partial<ReactionInput> = {}): ReactionInput {
  return {
    anchor,
    role: 'REACTANT',
    compound: storedCompound(),
    eq: entered('1', 'NO_UNIT', 'default'),
    samples: [makeReactionInputSample(`${anchor.slice(0, -1)}a`)],
    ...overrides,
  };
}

const SALT_CODE = DICTIONARIES.SALT_CODE?.[0];
const HAZARDS = DICTIONARIES.HEALTH_HAZARD ?? [];

/**
 * The four `EnteredValueSource` cases plus the two flash triggers, one row each, so every branch
 * of `determineCellClasses` is reachable from a story — and a `VIRTUAL`, a `STORED` and an
 * `UNKNOWN` compound, which is what decides whether Mol. Weight, Salt Code and Salt EQ are
 * editable at all.
 *
 * Two of the rows carry several batches, with **differently-sized content**: that is what makes
 * the nested tables' fixed column widths visible. Sized from their own content they would come
 * out different, which is the bug the `<colgroup>` in `SampleTable` exists to prevent.
 */
export const REACTION_INPUTS: ReactionInput[] = [
  // Limiting reactant. A hand-entered weight, and a mol the backend calculated from it.
  makeReactionInput('d0000000-0000-4000-8000-000000000001', {
    limiting: true,
    chemicalName: 'Salicylic acid',
    mol: entered('4.9', 'MMOL', 'calculated'),
    samples: [
      makeReactionInputSample('e0000000-0000-4000-8000-00000000000a', {
        weight: entered('676.5', 'MG', 12),
        mol: entered('4.9', 'MMOL', 'calculated'),
        purity: entered('98.5', 'NO_UNIT', 12),
        healthHazards: HAZARDS.slice(0, 2),
      }),
    ],
  }),
  // A solvent, and the row with the most batches — four, with contents of very different
  // widths, so two of these tables side by side would visibly disagree without fixed columns.
  makeReactionInput('d0000000-0000-4000-8000-000000000002', {
    role: 'SOLVENT',
    chemicalName: 'Acetic anhydride',
    compound: storedCompound({
      compoundID: 'c0000000-0000-4000-8000-000000000002',
      compoundKey: 'STR-00000000-90',
      casNumber: '108-24-7',
      formula: 'C<sub>4</sub>H<sub>6</sub>O<sub>3</sub>',
    }),
    samples: [
      makeReactionInputSample('e0000000-0000-4000-8000-00000000000b', {
        nbkBatchNumber: '20260101-0001-002',
        volume: entered('4.5', 'ML', 8),
        density: entered('1.08', 'G_ML', 'fixed'),
        mol: entered('0.0049', 'MMOL', 'calculated'),
        comment: 'Dried over molecular sieves before use',
      }),
      makeReactionInputSample('e0000000-0000-4000-8000-00000000000c', {
        nbkBatchNumber: '20260101-0001-003',
        volume: entered('12', 'ML', 8),
        mol: entered('0.013', 'MMOL', 'calculated'),
      }),
      makeReactionInputSample('e0000000-0000-4000-8000-00000000000d', {
        nbkBatchNumber: '20260101-0001-004',
        // Overwritten by the backend — the row that flashes red once a patch lands.
        volume: { value: '0.75', unit: 'ML', source: 'calculated', overwritten: true },
        molarity: entered('0.5', 'M', 3),
      }),
      makeReactionInputSample('e0000000-0000-4000-8000-00000000000e', {
        nbkBatchNumber: '20260101-0001-006',
        healthHazards: HAZARDS.slice(1, 4),
      }),
    ],
  }),
  // Virtual compound: Salt Code is editable here, and so is Salt EQ because a code is set.
  makeReactionInput('d0000000-0000-4000-8000-000000000003', {
    role: 'REAGENT',
    chemicalName: 'Pyridine',
    compound: {
      type: 'VIRTUAL',
      compoundID: 'c0000000-0000-4000-8000-000000000003',
      formula: 'C<sub>5</sub>H<sub>5</sub>N',
      molWeight: entered('79.1', 'G_PER_MOL', 'fixed'),
      exactMass: entered('79.0422', 'NO_UNIT', 'fixed'),
      calculatedBatchMF: 'C5H5N',
      compoundKey: 'VIRT-000012',
      saltCode: SALT_CODE,
      saltEQ: 1,
    },
    eq: entered('2', 'NO_UNIT', 5),
    samples: [
      makeReactionInputSample('e0000000-0000-4000-8000-00000000000f', {
        nbkBatchNumber: '20260101-0001-007',
        weight: entered('790', 'MG', 5),
      }),
    ],
  }),
  // Stored compound *with* a salt code: both Salt Code and Salt EQ stay locked, because the
  // registry owns them. indigo-frontend let this row's Salt EQ be edited.
  makeReactionInput('d0000000-0000-4000-8000-000000000004', {
    role: 'CATALYST',
    chemicalName: 'DMAP hydrochloride',
    compound: storedCompound({
      compoundID: 'c0000000-0000-4000-8000-000000000004',
      compoundKey: 'STR-00000000-91',
      formula: 'C<sub>7</sub>H<sub>10</sub>N<sub>2</sub>',
      saltCode: SALT_CODE,
      saltEQ: 1,
    }),
    samples: [makeReactionInputSample('e0000000-0000-4000-8000-000000000010', { nbkBatchNumber: '20260101-0001-008' })],
  }),
  // An unidentified compound — the only case where Mol. Weight is the user's to enter, and a
  // row with nothing filled in at all.
  makeReactionInput('d0000000-0000-4000-8000-000000000005', {
    role: 'REAGENT',
    compound: { type: 'UNKNOWN', molWeight: {} },
    samples: [
      makeReactionInputSample('e0000000-0000-4000-8000-000000000011', {
        nbkBatchNumber: undefined,
        purity: {},
      }),
    ],
  }),
];

/**
 * One batch of a product. `shortNbkBatchNumber` is derived server-side from `nbkBatchNumber`, so
 * the two are kept in step here rather than being overridden independently.
 */
export function makeReactionOutputSample(
  anchor: string,
  overrides: Partial<ReactionOutputSample> = {},
): ReactionOutputSample {
  const nbkBatchNumber = overrides.nbkBatchNumber ?? '20260101-0001-001';
  return {
    anchor,
    nbkBatchNumber,
    shortNbkBatchNumber: nbkBatchNumber.slice(nbkBatchNumber.lastIndexOf('-') + 1),
    // A new batch starts at 100 % purity — `AddProductSampleHandler`.
    purity: entered('100', 'NO_UNIT', 'default'),
    healthHazards: [],
    ...overrides,
  };
}

/**
 * The products of a step. Every row here is `intended` except the last, which is what proves the
 * table filters them out.
 *
 * Between them they reach every branch the columns declare: the three `ReactionOutputType`s, a
 * `VIRTUAL` compound with a salt code (the only editable Salt Code and Salt EQ), a `STORED` one
 * with a salt code (both locked by the registry), and a row with no theoretical values at all —
 * what a reaction with no limiting reagent looks like.
 *
 * Their **batches** are what the Product Batch Summary renders, and they carry the four states
 * that table branches on: an unregistered batch with numbers, an empty one, a `REGISTERED` one
 * (Register and Delete frozen), and a `FAILED` one with a message (both actions live again). The
 * unintended row's batch is the only one whose Sync with Products is enabled.
 */
export function makeReactionOutput(anchor: string, overrides: Partial<ReactionOutput> = {}): ReactionOutput {
  return {
    anchor,
    outputName: 'P0',
    type: 'FINAL',
    intended: true,
    compound: storedCompound(),
    eq: entered('1', 'NO_UNIT', 'default'),
    theoMol: entered('4.9', 'MMOL', 'calculated'),
    theoWeight: entered('500.4', 'MG', 'calculated'),
    samples: [],
    ...overrides,
  };
}

export const REACTION_OUTPUTS: ReactionOutput[] = [
  // The wanted product.
  makeReactionOutput('f0000000-0000-4000-8000-000000000001', {
    outputName: 'P0',
    chemicalName: 'Acetylsalicylic acid',
    compound: storedCompound({
      compoundID: 'c0000000-0000-4000-8000-000000000010',
      compoundKey: 'STR-00000000-95',
      formula: 'C<sub>9</sub>H<sub>8</sub>O<sub>4</sub>',
      molWeight: entered('180.16', 'G_PER_MOL', 'fixed'),
      exactMass: entered('180.0423', 'NO_UNIT', 'fixed'),
    }),
    eq: entered('1', 'NO_UNIT', 7),
    samples: [
      makeReactionOutputSample('f1000000-0000-4000-8000-000000000001', {
        actualWeight: entered('246', 'MG', 12),
        actualMol: entered('1.35', 'MMOL', 'calculated'),
        molarity: entered('0.04', 'M', 'calculated'),
        yield: entered('27.5', 'NO_UNIT', 'calculated'),
        purity: entered('98.5', 'NO_UNIT', 12),
      }),
      // Nothing entered yet — the em-dash state of every numeric column.
      makeReactionOutputSample('f1000000-0000-4000-8000-000000000002', {
        nbkBatchNumber: '20260101-0001-002',
      }),
    ],
  }),
  // A by-product, on a virtual compound: Salt Code is editable here, and Salt EQ with it.
  makeReactionOutput('f0000000-0000-4000-8000-000000000002', {
    outputName: 'P1',
    chemicalName: 'Acetic acid',
    type: 'BY_PRODUCT',
    compound: {
      type: 'VIRTUAL',
      compoundID: 'c0000000-0000-4000-8000-000000000011',
      formula: 'C<sub>2</sub>H<sub>4</sub>O<sub>2</sub>',
      molWeight: entered('60.052', 'G_PER_MOL', 'fixed'),
      exactMass: entered('60.0211', 'NO_UNIT', 'fixed'),
      calculatedBatchMF: 'C2H4O2',
      compoundKey: 'VIRT-000031',
      saltCode: SALT_CODE,
      saltEQ: 1,
    },
    theoWeight: entered('294.3', 'MG', 'calculated'),
    samples: [
      makeReactionOutputSample('f1000000-0000-4000-8000-000000000003', {
        nbkBatchNumber: '20260101-0001-003',
        registrationStatus: 'REGISTERED',
        strCode: 'STR-00000031-01',
        actualWeight: entered('88', 'MG', 9),
      }),
    ],
  }),
  // An intermediate the next step consumes, and the row with nothing calculated on it — the
  // reaction has no limiting reagent, so `theoMol` and `theoWeight` never resolve.
  makeReactionOutput('f0000000-0000-4000-8000-000000000003', {
    outputName: 'P2',
    type: 'INTERMEDIATE',
    compound: storedCompound({
      compoundID: 'c0000000-0000-4000-8000-000000000012',
      compoundKey: 'STR-00000000-96',
      formula: 'C<sub>7</sub>H<sub>6</sub>O<sub>3</sub>',
      saltCode: SALT_CODE,
      saltEQ: 1,
    }),
    theoMol: {},
    theoWeight: {},
    samples: [
      makeReactionOutputSample('f1000000-0000-4000-8000-000000000004', {
        nbkBatchNumber: '20260101-0001-004',
        registrationStatus: 'FAILED',
        registrationStatusMessage: 'Compound registry rejected the structure',
      }),
    ],
  }),
  // Not drawn in the scheme, so the table must not show it.
  makeReactionOutput('f0000000-0000-4000-8000-000000000004', {
    outputName: 'P3',
    chemicalName: 'Unplanned by-product',
    type: 'BY_PRODUCT',
    intended: false,
    samples: [
      makeReactionOutputSample('f1000000-0000-4000-8000-000000000005', {
        nbkBatchNumber: '20260101-0001-005',
      }),
    ],
  }),
];

/** One reaction step, carrying the input rows above. */
export function makeReaction(overrides: Partial<Reaction> = {}): Reaction {
  return {
    anchor: 'b0000000-0000-4000-8000-000000000001',
    rxnfile: REACTION_RXNFILE,
    inputs: REACTION_INPUTS,
    limitingAnchor: 'd0000000-0000-4000-8000-000000000001',
    outputs: REACTION_OUTPUTS,
    precursorReactantIds: [],
    ...overrides,
  };
}

export function makeExperimentDetails(overrides: Partial<ExperimentDetails> = {}): ExperimentDetails {
  const base = makeExperiment();
  return {
    ...base,
    title: 'Acetic anhydride route',
    therapeuticArea: DICTIONARIES.THERAPEUTIC_AREA?.[0],
    projectCode: DICTIONARIES.PROJECT_CODE?.[0],
    description: '<p>Acetylation of salicylic acid, second pass.</p>',
    literature: 'J. Chem. Educ. 2019, 96, 4',
    templateId: TEMPLATE_ID,
    batchCreator: ADMINISTRATOR,
    linkedExperiments: [{ id: '66666666-6666-6666-6666-666666666666', name: '12345678-0100' }],
    continuedFrom: [],
    continuedTo: [],
    attachments: ATTACHMENTS,
    acl: EXPERIMENT_ACL,
    currentPermissions: ['VIEW_EXPERIMENTS', 'EDIT_EXPERIMENTS', 'MANAGE_EXPERIMENT_ACCESS'],
    // `ExperimentModel.reactions` is @NotEmpty on the backend, and the screen relies on it.
    model: { reactions: [makeReaction()], significantFigures: 5 },
    projectId: '11111111-1111-1111-1111-111111111111',
    projectName: 'Kinase Inhibitor Screening',
    notebookId: '77777777-7777-7777-7777-777777777777',
    notebookName: '00000001',
    ...overrides,
  };
}

/**
 * A stable 12-hex-digit id suffix from a name. This used to be `name.length`, which collided —
 * `protocol.docx` and `raw-trace.dat` are both 13 characters, as are `yields.xlsx` and
 * `spectra.png` at 11 — so `ATTACHMENTS` shipped two pairs of duplicate ids. React cannot
 * reconcile a removal from a keyed list whose keys repeat, which made a deleted row stay on
 * screen; every story run also logged a duplicate-key error.
 */
function fixtureSuffix(name: string): string {
  let hash = 0;
  for (const char of name) hash = (hash * 31 + char.charCodeAt(0)) >>> 0;
  return hash.toString(16).padStart(12, '0');
}

export function makeAttachment(name: string, overrides: Partial<Attachment> = {}): Attachment {
  return {
    id: `a77a0000-0000-4000-8000-${fixtureSuffix(name)}`,
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
