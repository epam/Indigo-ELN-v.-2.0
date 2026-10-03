import { asEnteredValue } from '@/components/experiments/stoichiometry/columns';

import type { NumericCellValue } from '@/components/experiments/stoichiometry/numeric-cell';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import { sortByName } from '@/lib/types/dictionaries.ts';
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
  isKnownCompound,
  MOL_UNITS,
  MOL_WEIGHT_UNITS,
  MOLARITY_UNITS,
  NO_UNITS,
  VOLUME_UNITS,
  WEIGHT_UNITS,
} from '@/lib/types/reactions.ts';

/**
 * Mirrors `ReactionInput.updateCompound`: a compound can be re-salted while every sample on the
 * row is `VIRTUAL`, and is fixed once a real one is attached. An unknown compound has no salt.
 * The stereoisomer code is set through the same call, so it is gated by this too.
 */
function saltEditable(input: ReactionInput): boolean {
  return isKnownCompound(input.compound) && input.samples.every((sample) => sample.sampleSource === 'VIRTUAL');
}

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
   * A **floor** in pixels, and only the number-with-unit columns state one. Every other column
   * is as wide as its widest value or its header, whichever is more — headers are
   * `whitespace-nowrap` and no cell truncates, so both are real minimums.
   *
   * These need more than that because what they show is not what they edit: an empty one is an
   * em-dash under a short header, and the number input that replaces it has to fit in the same
   * box, with the unit beside it.
   */
  minWidth?: number;
  /**
   * Sizes the column to its content and no wider — for a column that must not be widened by a
   * sample cell spanning it. A table has no grow factor to set to zero; this is the nearest thing.
   *
   * A spanning cell wider than the columns under it hands its excess to the ones with no width
   * of their own first — and only then to the rest, in proportion to their content. So a spacer
   * beside two unsized columns absorbs nothing: it is empty, and its share of "in proportion to
   * content" is zero. This states a width of 1px, which a cell cannot actually be squeezed to —
   * it stays as wide as its content and its header — but which makes the spacer the only
   * unsized column in the span, and so the one that grows. `ACTIONS_CELL_CLASS` uses the same
   * `w-px` for the same effect.
   *
   * It only holds for content that does not shrink: a cell that truncates has no minimum, and
   * would be clipped to its header's width.
   */
  fitContent?: boolean;
}

type Cell<Row> =
  /** 1-based position of the row. Not a field; the renderer counts. */
  | { kind: 'index' }
  /** Read-only text. `undefined` renders as an em-dash. */
  | { kind: 'readonly'; value: (row: Row) => string | undefined }
  /** Server-rendered HTML — a molecular formula, whose subscripts arrive as `<sub>` tags. */
  | { kind: 'html'; value: (row: Row) => string | undefined }
  /**
   * A calculated number, shown but not editable. Its own kind rather than a `numeric` with
   * `editable: () => false`, because a read-only cell has no mutation to name.
   */
  | {
      kind: 'readonlyNumeric';
      value: (row: Row) => EnteredValue<string> | undefined;
      units: readonly string[];
    }
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
      /** The fixed unit's text on a single-unit column, when `unitLabel` is not what to show. */
      suffix?: string;
      mutation: (row: Row, next: NumericCellValue) => ModelMutation;
      editable?: (row: Row) => boolean;
    }
  /** One item from a built-in dictionary. */
  | {
      kind: 'dictionary';
      dictionary: 'SALT_CODE' | 'STEREOISOMER_CODE';
      value: (row: Row) => DictionaryItemRef;
      mutation: (row: Row, next: DictionaryItemRef) => ModelMutation;
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
 * The host columns a sample row skips before its first cell: the chevron and every compound-only
 * column — `#`, Compound ID, CAS #, Chem. Name and Mol. Weight. This is what puts the sample
 * Batch # column under the compound Batch # column.
 */
export const SAMPLE_INDENT_SPAN = 6;

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

export const COMPOUND_COLUMNS: InputColumn[] = [
  { id: 'index', header: '#', kind: 'index' },
  {
    id: 'compoundId',
    header: 'Compound ID',
    kind: 'readonly',
    value: (input) => input.compound.compoundKey,
  },
  {
    id: 'casNumber',
    header: 'CAS #',
    kind: 'readonly',
    value: (input) => input.compound.casNumber,
  },
  {
    id: 'chemicalName',
    header: 'Chem. Name',
    kind: 'text',
    value: (input) => input.chemicalName,
    mutation: (input, chemicalName) => ({ type: 'SetInputRowChemicalName', anchor: input.anchor, chemicalName }),
  },
  {
    id: 'molWeight',
    header: 'Mol. Weight',
    kind: 'numeric',
    value: (input) => input.compound.molWeight,
    units: MOL_WEIGHT_UNITS,
    // Implied by the column — every molecular weight is g/mol.
    suffix: '',
    // A known compound's molecular weight comes from the registry; only an unidentified one
    // is the user's to state.
    editable: (input) => !isKnownCompound(input.compound),
    mutation: (input, next) => ({
      type: 'SetInputCompoundMolWeight',
      anchor: input.anchor,
      molWeight: next.value,
    }),
  },
  {
    id: 'batches',
    header: 'Batch #',
    kind: 'readonly',
    // Every batch under this compound at a glance, so a collapsed row still says what it holds.
    value: (input) =>
      input.samples
        .map((sample) => shortBatchNumber(sample.nbkBatchNumber))
        .filter((each) => each != null)
        .join(', ') || undefined,
  },
  {
    id: 'weight',
    header: 'Weight',
    minWidth: 110,
    kind: 'readonlyNumeric',
    // Calculated: `weight = ∑ sample.weight`. Empty until every batch has a weight.
    value: (input) => input.weight,
    units: WEIGHT_UNITS,
  },
  {
    id: 'volume',
    header: 'Volume',
    minWidth: 110,
    kind: 'readonlyNumeric',
    // Calculated: `volume = ∑ sample.volume`. Empty until every batch has a volume.
    value: (input) => input.volume,
    units: VOLUME_UNITS,
  },
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
    kind: 'numeric',
    value: (input) => input.eq,
    units: NO_UNITS,
    mutation: (input, next) => ({ type: 'SetInputRowEQ', anchor: input.anchor, eq: next.value }),
  },
  {
    id: 'role',
    header: 'Rxn Role',
    kind: 'role',
    value: (input) => input.role,
    mutation: (input, role) => ({ type: 'SetInputRowRole', anchor: input.anchor, role }),
  },
  {
    id: 'formula',
    header: 'Mol Form.',
    kind: 'html',
    value: (input) => input.compound.formula,
  },
  {
    id: 'limiting',
    header: 'Limiting',
    // So Hazard Comments below widens `hazardSpacer` rather than this.
    fitContent: true,
    kind: 'limiting',
    value: (input) => input.limiting === true,
    mutation: (input) => ({ type: 'SetInputRowLimiting', anchor: input.anchor }),
  },
  {
    id: 'saltCode',
    header: 'Salt Code',
    // For the same reason as Limiting's. The select does not truncate, so a longer salt code
    // still widens the column.
    fitContent: true,
    kind: 'dictionary',
    dictionary: 'SALT_CODE',
    value: (input) => input.compound.saltCode,
    // Once a real sample is attached, the compound is that sample's and its salt is registry data.
    editable: saltEditable,
    mutation: (input, saltCode) => ({ type: 'SetInputRowSaltCode', anchor: input.anchor, saltCode }),
  },
  /**
   * Absorbs the width Hazard Comments needs beyond Limiting + Salt Code. Empty in the compound
   * row, so it contributes no minimum of its own and collapses to nothing until a sample
   * actually carries hazards. It only absorbs because those two are `fitContent`.
   */
  { id: 'hazardSpacer', header: '', kind: 'spacer' },
  {
    id: 'saltEQ',
    header: 'Salt EQ',
    // So a long Comments below widens `commentSpacer` rather than this.
    fitContent: true,
    kind: 'numeric',
    value: (input) => asEnteredValue(input.compound.saltEQ),
    units: NO_UNITS,
    // Both gates, not just the salt code: a real sample's salt EQ is fixed by the registry even
    // when it has a code. (indigo-frontend checked only for the code, which let a registered
    // compound's salt EQ be edited.) The salt EQ is absent exactly when the code is the default
    // "00 - Parent Structure", i.e. no salt.
    editable: (input) => saltEditable(input) && input.compound.saltEQ != null,
    mutation: (input, next) => ({ type: 'SetInputRowSaltEQ', anchor: input.anchor, saltEQ: next.value }),
  },
  {
    id: 'stereoisomerCode',
    header: 'Stereoisomer Code',
    // For the same reason as Salt EQ's.
    fitContent: true,
    kind: 'dictionary',
    dictionary: 'STEREOISOMER_CODE',
    value: (input) => input.compound.stereoisomerCode,
    // The same gate as the salt code: the handler goes through the same `updateCompound`.
    editable: saltEditable,
    mutation: (input, stereoisomerCode) => ({
      type: 'SetInputCompoundStereoisomerCode',
      anchor: input.anchor,
      stereoisomerCode,
    }),
  },
  /** The same trick for Comments, which may need more than Salt EQ and Stereoisomer Code. */
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
 * it, and `SAMPLE_INDENT_SPAN` skips the six it starts after. That is what makes the two levels
 * line up without any arithmetic — a cell either starts on a grid boundary or it does not, and
 * the browser cannot render it half a pixel out. The spans below plus the indent total the
 * twenty host columns; see the diagram on `StoichiometryTable`.
 *
 * A nested table per expanded compound was the alternative, and it is the reason the spans are
 * worth the trouble: two of them side by side would size their columns from their own content
 * and read as two unrelated grids rather than one continued list.
 *
 * There is no `<colgroup>` anywhere in this table, and no stated widths: see `ColumnBase`.
 */
export const SAMPLE_COLUMNS: SampleColumn[] = [
  {
    id: 'batch',
    span: 1,
    header: 'Batch #',
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
    kind: 'numeric',
    value: (sample) => sample.purity,
    units: NO_UNITS,
    // A percentage, though the wire still carries `NO_UNIT`.
    suffix: '%',
    mutation: (sample, next) => ({ type: 'SetInputPurity', anchor: sample.anchor, purity: next.value }),
  },
  {
    id: 'hazards',
    span: 3,
    header: 'Hazard Comments',
    kind: 'multiDictionary',
    dictionary: 'HEALTH_HAZARD',
    value: (sample) => sortByName(sample.healthHazards),
    mutation: (sample, healthHazards) => ({ type: 'SetInputHealthHazards', anchor: sample.anchor, healthHazards }),
  },
  {
    id: 'comment',
    span: 3,
    header: 'Comments',
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
