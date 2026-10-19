import type { ModelMutation } from '@/lib/types/mutations.ts';
import type {
  ColumnUnit,
  EnteredValue,
  ReactionOutput,
  ReactionOutputSample,
  ReactionOutputType,
} from '@/lib/types/reactions.ts';
import { plainFormula, REGISTRATION_STATUS_LABELS, shortNbkBatchNumber } from '@/lib/types/reactions.ts';

/**
 * The Product Batch Summary's columns, as data — the same shape `inputs/columns.ts` and
 * `products/columns.ts` use, read by the same kind of exhaustive `switch`.
 *
 * A **third** union rather than a widening of either of theirs, for the reason stated at the top
 * of `products/columns.ts`: the kinds only this table needs (`outputTypeBadge`, `sync`,
 * `register`) would otherwise turn up as unhandled cases in switches that can never be given
 * them.
 *
 * The row is a flattened **(output, sample) pair** — one row per *batch*, not per product. And
 * unlike the products table it does **not** filter on `intended`: an output that was never drawn
 * in the scheme is exactly the thing that belongs here, and the Sync with Products action is what
 * promotes it into the other table.
 */

/** One batch, its parent product, and which step it came from. */
export interface BatchRow {
  output: ReactionOutput;
  sample: ReactionOutputSample;
  /** Index into `ExperimentModel.reactions`. Shown 1-based. */
  step: number;
}

interface ColumnBase {
  id: string;
  header: string;
  /**
   * What the column's values are measured in, written after the header — `Weight, g`. Read off
   * the experiment, since every value of a quantity is in its one unit; no cell shows a unit.
   */
  unit?: ColumnUnit;
  /**
   * A floor in pixels, stated only where the cell edits a number — see the note on
   * the inputs table's own `minWidth`. Every other column is as wide as its content or header.
   */
  minWidth?: number;
}

type Cell =
  /** Read-only text. `undefined` renders as an em-dash. */
  | { kind: 'readonly'; value: (row: BatchRow) => string | undefined; title?: (row: BatchRow) => string | undefined }
  /** The product type as a static coloured pill. Read-only here; the products table edits it. */
  | { kind: 'outputTypeBadge'; value: (row: BatchRow) => ReactionOutputType }
  /**
   * A calculated number, shown but not editable. Its own kind rather than a `numeric` with
   * `editable: () => false`, because a read-only cell has no mutation to name.
   */
  | {
      kind: 'readonlyNumeric';
      value: (row: BatchRow) => EnteredValue<string> | undefined;
    }
  /** An editable number, in the unit the column's header names. */
  | {
      kind: 'numeric';
      value: (row: BatchRow) => EnteredValue<string> | undefined;
      mutation: (row: BatchRow, next: string | null) => ModelMutation;
    }
  /** The row's icon buttons, as one column — see `BatchAction`. */
  | { kind: 'actions'; actions: BatchAction[] };

/**
 * One icon button in the row's trailing action group.
 *
 * The three used to be three columns of their own, which is how they came to be spread across
 * ~192px of a table that already scrolls. They are one column now, so their difference has to
 * live somewhere narrower than `kind` — hence this second union, read by `BatchActionButton`.
 *
 * `id` is what `cellId` addresses, so it is also what keys a spinner: two actions on one row are
 * saving independently, and the ids are unchanged from when each was a column of its own.
 */
export type BatchAction =
  /**
   * Promotes an output that was not drawn in the scheme into the Reaction Products table.
   *
   * The one action keyed by the **output** anchor rather than the sample's — `SetOutputRowIntended`
   * names the product row, and the wrong level resolves to nothing and 400s.
   */
  | { id: 'sync'; mutation: (row: BatchRow) => ModelMutation }
  /** Sends the batch to the compound registry. */
  | { id: 'register'; mutation: (row: BatchRow) => ModelMutation }
  /** Deletes one batch. The product row survives. */
  | { id: 'delete'; mutation: (row: BatchRow) => ModelMutation };

export type BatchColumn = ColumnBase & Cell;

/**
 * A batch that is in flight or already registered.
 * `FAILED` is deliberately *not* protected: a failed registration is meant
 * to be retried, and the batch is still the user's to delete.
 *
 * This gates the Register and Delete actions only. It does **not** freeze the numeric cells:
 * `CompoundHandlers.java` rejects a registered sample's *compound* mutations (salt code, salt EQ,
 * stereoisomer, molfile) and nothing rejects `SetOutputActualWeight` and its siblings, so
 * disabling them here would forbid an edit the backend accepts.
 */
export function isSampleProtected(sample: ReactionOutputSample): boolean {
  return sample.registrationStatus != null && sample.registrationStatus !== 'FAILED';
}

export const BATCH_COLUMNS: BatchColumn[] = [
  {
    id: 'batchNo',
    header: 'Batch #',
    kind: 'readonly',
    // Zero-padded to three, as `NbkBatchNumber.getShortForm()` has it.
    value: (row) => shortNbkBatchNumber(row.sample.nbkBatchNumber),
  },
  {
    id: 'productName',
    header: 'Product Name',
    kind: 'readonly',
    // The product's name (`P0`, `P1`, …), not its chemical name — editing it belongs to the
    // products table, which owns `SetOutputRowName`.
    value: (row) => row.output.outputName,
  },
  {
    id: 'reactionStep',
    header: 'Reaction Step',
    kind: 'readonly',
    // Derived from the position in `model.reactions`. There is only ever one step today.
    value: (row) => String(row.step + 1),
  },
  {
    id: 'productType',
    header: 'Product Type',
    kind: 'outputTypeBadge',
    value: (row) => row.output.type,
  },
  {
    id: 'regStatus',
    header: 'Reg. Status',
    kind: 'readonly',
    value: (row) =>
      row.sample.registrationStatus == null ? 'None' : REGISTRATION_STATUS_LABELS[row.sample.registrationStatus],
    // The only place `registrationStatusMessage` is surfaced at all — without it a failed
    // registration has no stated reason.
    title: (row) => row.sample.registrationStatusMessage,
  },
  {
    id: 'actualWeight',
    header: 'Total Weight',
    minWidth: 100,
    kind: 'numeric',
    value: (row) => row.sample.actualWeight,
    unit: 'weight',
    mutation: (row, next) => ({
      type: 'SetOutputActualWeight',
      anchor: row.sample.anchor,
      actualWeight: next,
    }),
  },
  {
    id: 'volume',
    header: 'Total Volume',
    minWidth: 100,
    kind: 'numeric',
    value: (row) => row.sample.volume,
    unit: 'volume',
    mutation: (row, next) => ({
      type: 'SetOutputVolume',
      anchor: row.sample.anchor,
      volume: next,
    }),
  },
  {
    id: 'actualMol',
    header: 'Total Moles',
    minWidth: 100,
    kind: 'numeric',
    value: (row) => row.sample.actualMol,
    unit: 'mol',
    mutation: (row, next) => ({
      type: 'SetOutputActualMol',
      anchor: row.sample.anchor,
      actualMol: next,
    }),
  },
  {
    id: 'molarity',
    header: 'Molarity',
    minWidth: 100,
    kind: 'numeric',
    // Never calculated, like purity — it is an input to `actualMol = molarity * volume` (F4.4),
    // not an output. The backend used to derive it from the two columns beside it.
    value: (row) => row.sample.molarity,
    unit: 'molarity',
    mutation: (row, next) => ({
      type: 'SetOutputMolarity',
      anchor: row.sample.anchor,
      molarity: next,
    }),
  },
  {
    id: 'yield',
    header: 'Yield',
    // Calculated, and a percentage: `yield = actualMol / output.theoMol * 100` (F8.1), or from
    // the weights when those are what is known (F9.1). There is no mutation that sets it.
    kind: 'readonlyNumeric',
    value: (row) => row.sample.yield,
    unit: 'percent',
  },
  {
    id: 'purity',
    header: 'Purity',
    kind: 'numeric',
    // A percentage, defaulting to 100. Never calculated — it is an input to every other formula
    // on the row rather than an output of one.
    value: (row) => row.sample.purity,
    unit: 'percent',
    mutation: (row, next) => ({ type: 'SetOutputPurity', anchor: row.sample.anchor, purity: next }),
  },
  /**
   * No `minWidth`: the column takes exactly the width of its buttons. See `ACTIONS_CELL_CLASS`
   * for the rest of its geometry, and why it is pinned to the right edge.
   */
  {
    id: 'actions',
    header: '',
    kind: 'actions',
    actions: [
      { id: 'sync', mutation: (row) => ({ type: 'SetOutputRowIntended', anchor: row.output.anchor, intended: true }) },
      { id: 'register', mutation: (row) => ({ type: 'RegisterSample', anchor: row.sample.anchor }) },
      { id: 'delete', mutation: (row) => ({ type: 'RemoveProductSample', anchor: row.sample.anchor }) },
    ],
  },
];

/**
 * Every field the search box looks at.
 *
 * The formula goes in as **text**, not as the HTML it arrives as — see `plainFormula`. Searching
 * the raw string means `C6H6` matches nothing while `sub` matches every row that has a formula.
 */
export function batchHaystack(row: BatchRow): string {
  const compound = row.output.compound;
  return [
    shortNbkBatchNumber(row.sample.nbkBatchNumber),
    row.sample.nbkBatchNumber,
    row.output.outputName,
    row.output.chemicalName,
    compound.formula == null ? undefined : plainFormula(compound.formula),
    compound.compoundKey,
    row.sample.registrationStatus == null ? 'None' : REGISTRATION_STATUS_LABELS[row.sample.registrationStatus],
  ]
    .filter((each) => each != null)
    .join(' ')
    .toLowerCase();
}
