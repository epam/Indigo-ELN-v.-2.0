import { asEnteredValue } from '@/components/experiments/stoichiometry/columns';

import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import { sortByName } from '@/lib/types/dictionaries.ts';
import type { ModelMutation } from '@/lib/types/mutations.ts';
import type {
  ColumnUnit,
  EnteredValue,
  ReactionInput,
  ReactionInputSample,
  ReactionRole,
} from '@/lib/types/reactions.ts';
import { isKnownCompound, shortNbkBatchNumber } from '@/lib/types/reactions.ts';

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
 * are genuinely different column sets rather than one flattened row.
 *
 * `kind` is a discriminated union so the renderer's `switch` is exhaustive — a new kind is a
 * compile error rather than a blank cell, the same guarantee `TemplateComponentView` gets.
 */

/** What every column carries, whichever level it belongs to. */
interface ColumnBase {
  id: string;
  header: string;
  /**
   * What the column's values are measured in, written after the header — `Weight, g`. Read off
   * the experiment, since every value of a quantity is in its one unit; no cell shows a unit.
   */
  unit?: ColumnUnit;
  /**
   * A **floor** in pixels, and only the number columns state one. Every other column
   * is as wide as its widest value or its header, whichever is more — headers are
   * `whitespace-nowrap` and no cell truncates, so both are real minimums.
   *
   * These need more than that because what they show is not what they edit: an empty one is an
   * em-dash under a short header, and the number input that replaces it has to fit in the same
   * box.
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
    }
  /** Free text, saved on blur. */
  | {
      kind: 'text';
      value: (row: Row) => string | undefined;
      mutation: (row: Row, next: string | null) => ModelMutation;
    }
  /** A number, in the unit the column's header names. */
  | {
      kind: 'numeric';
      value: (row: Row) => EnteredValue<string> | undefined;
      mutation: (row: Row, next: string | null) => ModelMutation;
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
export type SampleColumn = (SamplePart & { span: number }) | SampleGroup;

/** One sample field: what a sample column is, less its place in the grid. */
export type SamplePart = ColumnBase &
  Cell<ReactionInputSample> & {
    /**
     * As wide as the widest cell of this part in any sample row, measured — see
     * `useEqualWidths`. A part without it is as wide as its own content.
     */
    equalWidth?: true;
  };

/**
 * Several sample fields sharing **one** cell — everything a batch has to the right of Mol.
 *
 * As cells of their own they each had to end on a grid line, so every boundary between them was
 * a boundary of the compound row too: Density sat under EQ and each widened the other, though
 * the two have nothing to do with one another. As one cell they have no line between them to
 * hold anything to, and the compound columns above stay packed.
 *
 * What a grid line gave for free is then done by hand for the parts that need it: an
 * `equalWidth` part is measured, so Density is as wide in one batch as in the next. The last
 * part sits at the cell's right edge, and the group's own slack is the gap before it.
 */
type SampleGroup = ColumnBase & { kind: 'group'; span: number; parts: SamplePart[] };

/**
 * The host columns a sample row skips before its first cell: the chevron and `#`. This is what
 * puts the sample Batch # column under the compound's Compound ID, which it continues.
 */
export const SAMPLE_INDENT_SPAN = 2;

/**
 * The trailing ordinal of an NBK batch number, without its padding: `20240101-0001-003` reads
 * as `3`. What a sample cell's accessible label names its batch by — the full number is unique
 * across the notebook and far too long to be read out before every field.
 */
export function shortBatchNumber(nbkBatchNumber: string | undefined): string | undefined {
  if (nbkBatchNumber == null) return undefined;
  return String(Number(shortNbkBatchNumber(nbkBatchNumber)));
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
    id: 'weight',
    header: 'Weight',
    minWidth: 90,
    kind: 'readonlyNumeric',
    // Calculated: `weight = ∑ sample.weight`. Empty until every batch has a weight.
    value: (input) => input.weight,
    unit: 'weight',
  },
  {
    id: 'volume',
    header: 'Volume',
    minWidth: 90,
    kind: 'readonlyNumeric',
    // Calculated: `volume = ∑ sample.volume`. Empty until every batch has a volume.
    value: (input) => input.volume,
    unit: 'volume',
  },
  {
    id: 'mol',
    header: 'Mol',
    minWidth: 90,
    kind: 'numeric',
    value: (input) => input.mol,
    unit: 'mol',
    mutation: (input, next) => ({
      type: 'SetInputRowMol',
      anchor: input.anchor,
      mol: next,
    }),
  },
  {
    id: 'eq',
    header: 'EQ',
    // Room to type a digit or two and a decimal point into a cell that mostly shows `1`.
    minWidth: 56,
    // So the sample details below widen `sampleSpacer` rather than this.
    fitContent: true,
    kind: 'numeric',
    value: (input) => input.eq,
    mutation: (input, next) => ({ type: 'SetInputRowEQ', anchor: input.anchor, eq: next }),
  },
  {
    id: 'role',
    header: 'Rxn Role',
    // For the same reason as EQ's.
    fitContent: true,
    kind: 'role',
    value: (input) => input.role,
    mutation: (input, role) => ({ type: 'SetInputRowRole', anchor: input.anchor, role }),
  },
  {
    id: 'formula',
    header: 'Mol Form.',
    // For the same reason as EQ's.
    fitContent: true,
    kind: 'html',
    value: (input) => input.compound.formula,
  },
  {
    id: 'molWeight',
    header: 'Mol. Weight',
    // For the same reason as EQ's.
    fitContent: true,
    kind: 'numeric',
    value: (input) => input.compound.molWeight,
    unit: 'molWeight',
    // A known compound's molecular weight comes from the registry; only an unidentified one
    // is the user's to state.
    editable: (input) => !isKnownCompound(input.compound),
    mutation: (input, next) => ({
      type: 'SetInputCompoundMolWeight',
      anchor: input.anchor,
      molWeight: next,
    }),
  },
  {
    id: 'casNumber',
    header: 'CAS #',
    // For the same reason as EQ's.
    fitContent: true,
    kind: 'readonly',
    value: (input) => input.compound.casNumber,
  },
  {
    id: 'chemicalName',
    header: 'Chem. Name',
    // For the same reason as EQ's.
    fitContent: true,
    kind: 'text',
    value: (input) => input.chemicalName,
    mutation: (input, chemicalName) => ({ type: 'SetInputRowChemicalName', anchor: input.anchor, chemicalName }),
  },
  {
    id: 'limiting',
    header: 'Limiting',
    // For the same reason as EQ's.
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
  {
    id: 'saltEQ',
    header: 'Salt EQ',
    // For the same reason as Limiting's.
    fitContent: true,
    kind: 'numeric',
    value: (input) => asEnteredValue(input.compound.saltEQ),
    // Both gates, not just the salt code: a real sample's salt EQ is fixed by the registry even
    // when it has a code. The salt EQ is absent exactly when the code is the default
    // "00 - Parent Structure", i.e. no salt.
    editable: (input) => saltEditable(input) && input.compound.saltEQ != null,
    mutation: (input, next) => ({ type: 'SetInputRowSaltEQ', anchor: input.anchor, saltEQ: next }),
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
  /**
   * Absorbs the width a sample's details need beyond the ten columns before it.
   * Empty in the compound row, so it contributes no minimum of its own and collapses to nothing
   * until a sample actually needs the room. It only absorbs because those ten are `fitContent`.
   */
  { id: 'sampleSpacer', header: '', kind: 'spacer' },
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
 * it, and `SAMPLE_INDENT_SPAN` skips the two it starts after. That is what makes the two levels
 * line up without any arithmetic — a cell either starts on a grid boundary or it does not, and
 * the browser cannot render it half a pixel out. The spans below plus the indent total the
 * eighteen host columns; see the diagram on `StoichiometryTable`.
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
    value: (sample) => sample.sampleKey,
  },
  {
    id: 'weight',
    span: 1,
    header: 'Weight',
    minWidth: 90,
    kind: 'numeric',
    value: (sample) => sample.weight,
    unit: 'weight',
    mutation: (sample, next) => ({
      type: 'SetInputWeight',
      anchor: sample.anchor,
      weight: next,
    }),
  },
  {
    id: 'volume',
    span: 1,
    header: 'Volume',
    minWidth: 90,
    kind: 'numeric',
    value: (sample) => sample.volume,
    unit: 'volume',
    mutation: (sample, next) => ({
      type: 'SetInputVolume',
      anchor: sample.anchor,
      volume: next,
    }),
  },
  {
    id: 'mol',
    span: 1,
    header: 'Mol',
    minWidth: 90,
    kind: 'numeric',
    value: (sample) => sample.mol,
    unit: 'mol',
    mutation: (sample, next) => ({
      type: 'SetInputMol',
      anchor: sample.anchor,
      mol: next,
    }),
  },
  {
    id: 'details',
    // Every compound column from EQ to the spacer.
    span: 11,
    header: '',
    kind: 'group',
    parts: [
      {
        id: 'density',
        header: 'Density',
        minWidth: 90,
        equalWidth: true,
        kind: 'numeric',
        value: (sample) => sample.density,
        unit: 'density',
        mutation: (sample, next) => ({
          type: 'SetInputDensity',
          anchor: sample.anchor,
          density: next,
        }),
      },
      {
        id: 'molarity',
        header: 'Molarity',
        minWidth: 90,
        equalWidth: true,
        kind: 'numeric',
        value: (sample) => sample.molarity,
        unit: 'molarity',
        mutation: (sample, next) => ({
          type: 'SetInputMolarity',
          anchor: sample.anchor,
          molarity: next,
        }),
      },
      {
        id: 'purity',
        header: 'Purity',
        minWidth: 90,
        equalWidth: true,
        kind: 'numeric',
        value: (sample) => sample.purity,
        unit: 'percent',
        mutation: (sample, next) => ({ type: 'SetInputPurity', anchor: sample.anchor, purity: next }),
      },
      {
        id: 'hazards',
        header: 'Hazard Comments',
        kind: 'multiDictionary',
        dictionary: 'HEALTH_HAZARD',
        value: (sample) => sortByName(sample.healthHazards),
        mutation: (sample, healthHazards) => ({
          type: 'SetInputHealthHazards',
          anchor: sample.anchor,
          healthHazards,
        }),
      },
      {
        id: 'comment',
        header: 'Comments',
        kind: 'text',
        value: (sample) => sample.comment,
        mutation: (sample, comment) => ({ type: 'SetInputComment', anchor: sample.anchor, comment }),
      },
    ],
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
