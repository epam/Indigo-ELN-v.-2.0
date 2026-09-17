import { asEnteredValue } from '@/components/experiments/stoichiometry/columns';

import type { Align } from '@/components/experiments/stoichiometry/columns';

import type { NumericCellValue } from '@/components/experiments/stoichiometry/numeric-cell';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { ModelMutation } from '@/lib/types/mutations.ts';
import type {
  DensityUnit,
  EnteredValue,
  MolarityUnit,
  MolUnit,
  ReactionInput,
  ReactionInputSample,
  ReactionRole,
  VolumeUnit,
  WeightUnit,
} from '@/lib/types/reactions.ts';
import {
  DENSITY_UNITS,
  MOL_UNITS,
  MOL_WEIGHT_UNITS,
  MOLARITY_UNITS,
  NO_UNITS,
  VOLUME_UNITS,
  WEIGHT_UNITS,
} from '@/lib/types/reactions.ts';

/**
 * The table's columns, as data.
 *
 * Two lists, because the model is a tree and this table shows it as one: a `ReactionInput` is
 * a compound and owns one or more `ReactionInputSample`s, so compound fields and sample fields
 * are genuinely different column sets rather than one flattened row. (indigo-frontend flattened
 * them into one row per pair, which is why its header mixes `Mol. Weight` with `Density`.)
 *
 * `kind` is a discriminated union so the renderer's `switch` is exhaustive — a new kind is a
 * compile error rather than a blank cell, the same guarantee `TemplateComponentView` gets.
 */

/** What every column carries, whichever level it belongs to. */
interface ColumnBase {
  id: string;
  header: string;
  /**
   * Overrides the alignment `alignOf` would derive from the cell's kind.
   *
   * For a column that stands in for one whose kind it does not yet have: Weight and Volume have
   * no compound-level field, so their cells are `readonly` em-dashes — but the column *is* a
   * numeric one, and the samples below it right-align. Left to the default they would read left
   * under a left header while the batch rows beneath read right, which is two grids rather than
   * one column.
   */
  align?: Align;
  /**
   * A **floor** in pixels, not a fixed size: the table is auto-layout, so a column sizes itself
   * to its content and to the space available, and this only stops it collapsing.
   *
   * It has to stay a real floor rather than being dropped. A numeric cell showing an em-dash is
   * a few pixels wide, and swapping in the editor's number input plus its unit menu would jump
   * the layout on every click. Headers are `whitespace-nowrap`, which supplies a second floor.
   *
   * Spacer columns declare none — having no minimum of their own is exactly their purpose.
   */
  minWidth?: number;
}

type Cell<Row> =
  /** 1-based position of the row. Not a field; the renderer counts. */
  | { kind: 'index' }
  /** Read-only text. `undefined` renders as an em-dash. */
  | { kind: 'readonly'; value: (row: Row) => string | undefined }
  /** Server-rendered HTML — a molecular formula, whose subscripts arrive as `<sub>` tags. */
  | { kind: 'html'; value: (row: Row) => string | undefined }
  /** Free text, saved on blur. */
  | {
      kind: 'text';
      value: (row: Row) => string | undefined;
      mutation: (row: Row, next: string | null) => ModelMutation;
    }
  /**
   * A number with a unit, or a unitless one when `units` has a single member.
   *
   * `units` is `readonly string[]` and the committed unit comes back as a `string`, so each
   * `mutation` casts it to the enum its own field takes. The cast is safe by construction —
   * the value can only be one of the members of the `units` list two lines above it — and the
   * alternative, threading a unit type parameter through `Cell`, `InputColumn`, `SampleColumn`
   * and the renderer, buys nothing a reader of one column definition can see.
   */
  | {
      kind: 'numeric';
      value: (row: Row) => EnteredValue<string> | undefined;
      units: readonly string[];
      mutation: (row: Row, next: NumericCellValue) => ModelMutation;
      editable?: (row: Row) => boolean;
    }
  /** One item from a built-in dictionary. */
  | {
      kind: 'dictionary';
      dictionary: 'SALT_CODE';
      value: (row: Row) => DictionaryItemRef | undefined;
      mutation: (row: Row, next: DictionaryItemRef | null) => ModelMutation;
      editable?: (row: Row) => boolean;
    }
  /** Several items from a built-in dictionary. */
  | {
      kind: 'multiDictionary';
      dictionary: 'HEALTH_HAZARD';
      value: (row: Row) => DictionaryItemRef[];
      mutation: (row: Row, next: DictionaryItemRef[]) => ModelMutation;
    }
  /** The reaction role picker — a fixed enum, not a dictionary. */
  | { kind: 'role'; value: (row: Row) => ReactionRole; mutation: (row: Row, next: ReactionRole) => ModelMutation }
  /** The limiting-reagent radio. */
  | { kind: 'limiting'; value: (row: Row) => boolean; mutation: (row: Row) => ModelMutation }
  /** A destructive icon button. */
  | { kind: 'delete'; label: string; mutation: (row: Row) => ModelMutation }
  /**
   * A host column the compound row leaves empty, so that a wider sample column has somewhere to
   * grow that costs nothing. See `COMPOUND_COLUMNS` for which two, and why.
   */
  | { kind: 'spacer' };

export type InputColumn = ColumnBase & Cell<ReactionInput>;

/**
 * A sample column additionally says how many host columns it covers. There is one table, so a
 * sample cell reaches its place in the grid by spanning compound columns — see the picture in
 * `StoichiometryTable`.
 */
export type SampleColumn = ColumnBase & Cell<ReactionInputSample> & { span: number };

/**
 * The host columns a sample row skips before its first cell: the chevron, `#` and Compound ID.
 * This is what puts the sample Batch # column under the compound Batch # column.
 */
export const SAMPLE_INDENT_SPAN = 3;

/**
 * The trailing ordinal of an NBK batch number, without its padding: `20240101-0001-003` reads
 * as `3`. The full number is unique across the notebook and far too long for a table cell;
 * within one compound the ordinal alone is what distinguishes the batches.
 */
export function shortBatchNumber(nbkBatchNumber: string | undefined): string | undefined {
  if (nbkBatchNumber == null) return undefined;
  const ordinal = nbkBatchNumber.slice(nbkBatchNumber.lastIndexOf('-') + 1);
  return String(Number(ordinal));
}

/**
 * Weight and Volume have no compound-level field — they belong to a sample, and a compound may
 * own several. They are shown as an em-dash for now; the backend is to expose them as a sum
 * over the row's samples, at which point these become ordinary numeric columns.
 */
const COMPOUND_TOTAL_PLACEHOLDER = { kind: 'readonly', value: () => undefined, align: 'right' } as const;

export const COMPOUND_COLUMNS: InputColumn[] = [
  { id: 'index', header: '#', minWidth: 48, kind: 'index' },
  {
    id: 'compoundId',
    header: 'Compound ID',
    minWidth: 150,
    kind: 'readonly',
    value: (input) => (input.compound.type === 'UNKNOWN' ? undefined : input.compound.compoundKey),
  },
  {
    id: 'batches',
    header: 'Batch #',
    minWidth: 110,
    kind: 'readonly',
    // Every batch under this compound at a glance, so a collapsed row still says what it holds.
    value: (input) =>
      input.samples
        .map((sample) => shortBatchNumber(sample.nbkBatchNumber))
        .filter((each) => each != null)
        .join(', ') || undefined,
  },
  {
    id: 'casNumber',
    header: 'CAS #',
    minWidth: 110,
    kind: 'readonly',
    value: (input) => (input.compound.type === 'UNKNOWN' ? undefined : input.compound.casNumber),
  },
  {
    id: 'chemicalName',
    header: 'Chem. Name',
    minWidth: 160,
    kind: 'text',
    value: (input) => input.chemicalName,
    mutation: (input, chemicalName) => ({ type: 'SetInputRowChemicalName', anchor: input.anchor, chemicalName }),
  },
  {
    id: 'molWeight',
    header: 'Mol. Weight',
    minWidth: 110,
    kind: 'numeric',
    value: (input) => input.compound.molWeight,
    units: MOL_WEIGHT_UNITS,
    // A stored or virtual compound's molecular weight comes from the registry; only an
    // unidentified one is the user's to state.
    editable: (input) => input.compound.type === 'UNKNOWN',
    mutation: (input, next) => ({
      type: 'SetInputCompoundMolWeight',
      anchor: input.anchor,
      molWeight: next.value,
    }),
  },
  { id: 'weight', header: 'Weight', minWidth: 110, ...COMPOUND_TOTAL_PLACEHOLDER },
  { id: 'volume', header: 'Volume', minWidth: 110, ...COMPOUND_TOTAL_PLACEHOLDER },
  {
    id: 'mol',
    header: 'Mol',
    minWidth: 120,
    kind: 'numeric',
    value: (input) => input.mol,
    units: MOL_UNITS,
    // Note `molUnit`, not `unit` — the sample-level SetInputMol spells it the other way.
    mutation: (input, next) => ({
      type: 'SetInputRowMol',
      anchor: input.anchor,
      mol: next.value,
      molUnit: next.unit as MolUnit | null,
    }),
  },
  {
    id: 'eq',
    header: 'EQ',
    minWidth: 90,
    kind: 'numeric',
    value: (input) => input.eq,
    units: NO_UNITS,
    mutation: (input, next) => ({ type: 'SetInputRowEQ', anchor: input.anchor, eq: next.value }),
  },
  {
    id: 'role',
    header: 'Rxn Role',
    minWidth: 140,
    kind: 'role',
    value: (input) => input.role,
    mutation: (input, role) => ({ type: 'SetInputRowRole', anchor: input.anchor, role }),
  },
  {
    id: 'formula',
    header: 'Mol Form.',
    minWidth: 130,
    kind: 'html',
    value: (input) => input.compound.formula,
  },
  {
    id: 'limiting',
    header: 'Limiting',
    minWidth: 90,
    kind: 'limiting',
    value: (input) => input.limiting === true,
    mutation: (input) => ({ type: 'SetInputRowLimiting', anchor: input.anchor }),
  },
  {
    id: 'saltCode',
    header: 'Salt Code',
    minWidth: 150,
    kind: 'dictionary',
    dictionary: 'SALT_CODE',
    value: (input) => (input.compound.type === 'UNKNOWN' ? undefined : input.compound.saltCode),
    // A stored compound's salt code is registry data.
    editable: (input) => input.compound.type === 'VIRTUAL',
    mutation: (input, saltCode) => ({ type: 'SetInputRowSaltCode', anchor: input.anchor, saltCode }),
  },
  /**
   * Absorbs the width Hazard Comments needs beyond Limiting + Salt Code. Empty in the compound
   * row, so it contributes no minimum of its own and collapses to nothing until a sample
   * actually carries hazards.
   */
  { id: 'hazardSpacer', header: '', kind: 'spacer' },
  {
    id: 'saltEQ',
    header: 'Salt EQ',
    minWidth: 100,
    kind: 'numeric',
    value: (input) => (input.compound.type === 'UNKNOWN' ? undefined : asEnteredValue(input.compound.saltEQ)),
    units: NO_UNITS,
    // Both gates, not just the salt code: a stored compound's salt EQ is fixed by the registry
    // even when it has a code. (indigo-frontend checked only for the code, which let a stored
    // compound's salt EQ be edited.)
    editable: (input) => input.compound.type === 'VIRTUAL' && input.compound.saltCode != null,
    mutation: (input, next) => ({ type: 'SetInputRowSaltEQ', anchor: input.anchor, saltEQ: next.value }),
  },
  /** The same trick for Comments, which needs more than Salt EQ alone. */
  { id: 'commentSpacer', header: '', kind: 'spacer' },
  {
    id: 'delete',
    header: '',
    kind: 'delete',
    label: 'Delete compound',
    mutation: (input) => ({ type: 'RemoveInputRow', anchor: input.anchor }),
  },
];

/**
 * The sample columns — the batches nested under one compound.
 *
 * **`span` is how a sample cell reaches its place in the grid.** There is one `<table>` for both
 * levels, so a sample row does not draw a table of its own: it spans the compound columns above
 * it, and `SAMPLE_INDENT_SPAN` skips the three it starts after. That is what makes the two levels
 * line up without any arithmetic — a cell either starts on a grid boundary or it does not, and
 * the browser cannot render it half a pixel out. The spans below plus the indent total the
 * nineteen host columns; see the diagram on `StoichiometryTable`.
 *
 * A nested table per expanded compound was the alternative, and it is the reason the spans are
 * worth the trouble: two of them side by side would size their columns from their own content
 * and read as two unrelated grids rather than one continued list.
 *
 * `minWidth` is a floor like every other column's, applied by the renderer to the header cell —
 * not a fixed width, and there is no `<colgroup>` anywhere in this table.
 */
export const SAMPLE_COLUMNS: SampleColumn[] = [
  {
    id: 'batch',
    span: 4,
    header: 'Batch #',
    // Same width as the compound-level Batch # column above, so the two line up across their
    // whole width rather than only at their left edge.
    minWidth: 110,
    kind: 'readonly',
    value: (sample) => shortBatchNumber(sample.nbkBatchNumber),
  },
  {
    id: 'weight',
    span: 1,
    header: 'Weight',
    minWidth: 130,
    kind: 'numeric',
    value: (sample) => sample.weight,
    units: WEIGHT_UNITS,
    mutation: (sample, next) => ({
      type: 'SetInputWeight',
      anchor: sample.anchor,
      weight: next.value,
      unit: next.unit as WeightUnit | null,
    }),
  },
  {
    id: 'volume',
    span: 1,
    header: 'Volume',
    minWidth: 130,
    kind: 'numeric',
    value: (sample) => sample.volume,
    units: VOLUME_UNITS,
    mutation: (sample, next) => ({
      type: 'SetInputVolume',
      anchor: sample.anchor,
      volume: next.value,
      unit: next.unit as VolumeUnit | null,
    }),
  },
  {
    id: 'mol',
    span: 1,
    header: 'Mol',
    minWidth: 130,
    kind: 'numeric',
    value: (sample) => sample.mol,
    units: MOL_UNITS,
    mutation: (sample, next) => ({
      type: 'SetInputMol',
      anchor: sample.anchor,
      mol: next.value,
      unit: next.unit as MolUnit | null,
    }),
  },
  {
    id: 'density',
    span: 1,
    header: 'Density',
    minWidth: 130,
    kind: 'numeric',
    value: (sample) => sample.density,
    units: DENSITY_UNITS,
    mutation: (sample, next) => ({
      type: 'SetInputDensity',
      anchor: sample.anchor,
      density: next.value,
      unit: next.unit as DensityUnit | null,
    }),
  },
  {
    id: 'molarity',
    span: 1,
    header: 'Molarity',
    minWidth: 130,
    kind: 'numeric',
    value: (sample) => sample.molarity,
    units: MOLARITY_UNITS,
    mutation: (sample, next) => ({
      type: 'SetInputMolarity',
      anchor: sample.anchor,
      molarity: next.value,
      unit: next.unit as MolarityUnit | null,
    }),
  },
  {
    id: 'purity',
    span: 1,
    header: 'Purity',
    minWidth: 100,
    kind: 'numeric',
    value: (sample) => sample.purity,
    units: NO_UNITS,
    mutation: (sample, next) => ({ type: 'SetInputPurity', anchor: sample.anchor, purity: next.value }),
  },
  {
    id: 'hazards',
    span: 3,
    header: 'Hazard Comments',
    minWidth: 200,
    kind: 'multiDictionary',
    dictionary: 'HEALTH_HAZARD',
    value: (sample) => sample.healthHazards,
    mutation: (sample, healthHazards) => ({ type: 'SetInputHealthHazards', anchor: sample.anchor, healthHazards }),
  },
  {
    id: 'comment',
    span: 2,
    header: 'Comments',
    minWidth: 200,
    kind: 'text',
    value: (sample) => sample.comment,
    mutation: (sample, comment) => ({ type: 'SetInputComment', anchor: sample.anchor, comment }),
  },
  {
    id: 'delete',
    span: 1,
    header: '',
    kind: 'delete',
    label: 'Delete batch',
    mutation: (sample) => ({ type: 'RemoveInput', anchor: sample.anchor }),
  },
];
