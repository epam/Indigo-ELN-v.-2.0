import type { NumericCellValue } from '@/components/experiments/stoichiometry/numeric-cell';
import type { ModelMutation } from '@/lib/types/mutations.ts';
import type {
  EnteredValue,
  MolUnit,
  ReactionOutput,
  ReactionOutputSample,
  ReactionOutputType,
  VolumeUnit,
  WeightUnit,
} from '@/lib/types/reactions.ts';
import {
  MOL_UNITS,
  MOLARITY_UNITS,
  NO_UNITS,
  REGISTRATION_STATUS_LABELS,
  VOLUME_UNITS,
  WEIGHT_UNITS,
} from '@/lib/types/reactions.ts';

/**
 * The Product Batch Summary's columns, as data — the same shape `columns.ts` and
 * `product-columns.ts` use, read by the same kind of exhaustive `switch`.
 *
 * A **third** union rather than a widening of either of theirs, for the reason stated at the top
 * of `product-columns.ts`: the kinds only this table needs (`outputTypeBadge`, `sync`,
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
  /** A floor in pixels, not a fixed size — see the note on `columns.ts`'s own `minWidth`. */
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
  | { kind: 'readonlyNumeric'; value: (row: BatchRow) => EnteredValue<string> | undefined; units: readonly string[] }
  /** An editable number with a unit, or a unitless one when `units` has a single member. */
  | {
      kind: 'numeric';
      value: (row: BatchRow) => EnteredValue<string> | undefined;
      units: readonly string[];
      mutation: (row: BatchRow, next: NumericCellValue) => ModelMutation;
    }
  /**
   * Promotes an output that was not drawn in the scheme into the Reaction Products table.
   *
   * The one column keyed by the **output** anchor rather than the sample's — `SetOutputRowIntended`
   * names the product row, and the wrong level resolves to nothing and 400s.
   */
  | { kind: 'sync'; mutation: (row: BatchRow) => ModelMutation }
  /** Sends the batch to the compound registry. */
  | { kind: 'register'; mutation: (row: BatchRow) => ModelMutation }
  /** Deletes one batch. The product row survives. */
  | { kind: 'delete'; mutation: (row: BatchRow) => ModelMutation };

export type BatchColumn = ColumnBase & Cell;

/**
 * A batch that is in flight or already registered — port of indigo-frontend's
 * `isSampleProtected`. `FAILED` is deliberately *not* protected: a failed registration is meant
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
    minWidth: 90,
    kind: 'readonly',
    // The server's own derived field — `NbkBatchNumber.getShortForm()`, zero-padded to three.
    // indigo-frontend re-split the full number by hand for the same result.
    value: (row) => row.sample.shortNbkBatchNumber,
  },
  {
    id: 'productName',
    header: 'Product Name',
    minWidth: 130,
    kind: 'readonly',
    // The product's name (`P0`, `P1`, …), not its chemical name — editing it belongs to the
    // products table, which owns `SetOutputRowName`.
    value: (row) => row.output.outputName,
  },
  {
    id: 'reactionStep',
    header: 'Reaction Step',
    minWidth: 110,
    kind: 'readonly',
    // Derived from the position in `model.reactions`; indigo-frontend returns a literal '1'. There is only ever one
    // step today, so the two agree.
    value: (row) => String(row.step + 1),
  },
  {
    id: 'productType',
    header: 'Products Type',
    minWidth: 130,
    kind: 'outputTypeBadge',
    value: (row) => row.output.type,
  },
  {
    id: 'regStatus',
    header: 'Reg. Status',
    minWidth: 110,
    kind: 'readonly',
    value: (row) =>
      row.sample.registrationStatus == null ? 'None' : REGISTRATION_STATUS_LABELS[row.sample.registrationStatus],
    // The only place `registrationStatusMessage` is surfaced at all — indigo-frontend carries the
    // field and shows it nowhere, which leaves a failed registration with no stated reason.
    title: (row) => row.sample.registrationStatusMessage,
  },
  {
    id: 'actualWeight',
    header: 'Total Weight',
    minWidth: 130,
    kind: 'numeric',
    value: (row) => row.sample.actualWeight,
    units: WEIGHT_UNITS,
    mutation: (row, next) => ({
      type: 'SetOutputActualWeight',
      anchor: row.sample.anchor,
      actualWeight: next.value,
      unit: next.unit as WeightUnit | null,
    }),
  },
  {
    id: 'volume',
    header: 'Total Volume',
    minWidth: 130,
    kind: 'numeric',
    value: (row) => row.sample.volume,
    units: VOLUME_UNITS,
    mutation: (row, next) => ({
      type: 'SetOutputVolume',
      anchor: row.sample.anchor,
      volume: next.value,
      unit: next.unit as VolumeUnit | null,
    }),
  },
  {
    id: 'actualMol',
    header: 'Total Moles',
    minWidth: 130,
    kind: 'numeric',
    // The whole `EnteredValue`, not `.value`: indigo-frontend passes the bare string here — and
    // only here — so its unit picker never shows the saved unit.
    value: (row) => row.sample.actualMol,
    units: MOL_UNITS,
    mutation: (row, next) => ({
      type: 'SetOutputActualMol',
      anchor: row.sample.anchor,
      actualMol: next.value,
      unit: next.unit as MolUnit | null,
    }),
  },
  {
    id: 'molarity',
    header: 'Molarity',
    minWidth: 110,
    // Calculated: `molarity = actualMol / volume` (F4.5). `SetOutputMolarity` exists, but the
    // batch summary has never offered it — molarity is derived from the two columns beside it.
    kind: 'readonlyNumeric',
    value: (row) => row.sample.molarity,
    units: MOLARITY_UNITS,
  },
  {
    id: 'yield',
    header: 'Yield',
    minWidth: 100,
    // Calculated, and a percentage: `yield = actualMol / output.theoMol * 100` (F8.1), or from
    // the weights when those are what is known (F9.1). There is no mutation that sets it.
    kind: 'readonlyNumeric',
    value: (row) => row.sample.yield,
    units: NO_UNITS,
  },
  {
    id: 'purity',
    header: 'Purity',
    minWidth: 100,
    kind: 'numeric',
    // A percentage, defaulting to 100. Never calculated — it is an input to every other formula
    // on the row rather than an output of one.
    value: (row) => row.sample.purity,
    units: NO_UNITS,
    mutation: (row, next) => ({ type: 'SetOutputPurity', anchor: row.sample.anchor, purity: next.value }),
  },
  {
    id: 'sync',
    header: '',
    minWidth: 48,
    kind: 'sync',
    mutation: (row) => ({ type: 'SetOutputRowIntended', anchor: row.output.anchor, intended: true }),
  },
  {
    id: 'register',
    header: '',
    minWidth: 48,
    kind: 'register',
    mutation: (row) => ({ type: 'RegisterSample', anchor: row.sample.anchor }),
  },
  {
    id: 'delete',
    header: '',
    minWidth: 48,
    kind: 'delete',
    mutation: (row) => ({ type: 'RemoveProductSample', anchor: row.sample.anchor }),
  },
];

/** Every field the search box looks at. */
export function batchHaystack(row: BatchRow): string {
  const compound = row.output.compound;
  return [
    row.sample.shortNbkBatchNumber,
    row.sample.nbkBatchNumber,
    row.output.outputName,
    row.output.chemicalName,
    compound.formula,
    compound.type === 'UNKNOWN' ? undefined : compound.compoundKey,
    row.sample.registrationStatus == null ? 'None' : REGISTRATION_STATUS_LABELS[row.sample.registrationStatus],
  ]
    .filter((each) => each != null)
    .join(' ')
    .toLowerCase();
}
