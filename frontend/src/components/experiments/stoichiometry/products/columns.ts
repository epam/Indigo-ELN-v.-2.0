import { asEnteredValue } from '@/components/experiments/stoichiometry/columns';

import type { HideableColumn } from '@/lib/hooks/experiments/use-hidden-columns';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import { newAnchor } from '@/lib/utils';
import type { ModelMutation } from '@/lib/types/mutations.ts';
import type { ColumnUnit, EnteredValue, ReactionOutput, ReactionOutputType } from '@/lib/types/reactions.ts';
import { isKnownCompound } from '@/lib/types/reactions.ts';

/**
 * The Reaction Products table's columns, as data — the same shape `columns.ts` uses for the
 * inputs, and read by the same kind of exhaustive `switch`.
 *
 * A **separate** union rather than a widening of that file's `Cell<Row>`: the two kinds only
 * this table needs (`outputType`, `addBatch`) would otherwise turn up as unhandled cases in the
 * inputs table's two switches, which would have to answer for columns they can never be given.
 *
 * The row shape is flat. A `ReactionOutput` owns samples exactly as a `ReactionInput` does, but
 * they are the *Product Batch Summary*'s subject, not this table's — so there is no nesting
 * here, and none of the column spans and spacer columns the inputs table needs to line two
 * levels up.
 */

/** One product, plus which step it came from — the Reaction Step column and Show All Steps. */
/**
 * Mirrors `ReactionOutput.updateCompound`: a known compound can be re-salted until one of its
 * batches has gone to registration. An unknown compound has no salt.
 */
function saltEditable(output: ReactionOutput): boolean {
  return isKnownCompound(output.compound) && output.samples.every((sample) => sample.registrationStatus == null);
}

export interface ProductRow {
  output: ReactionOutput;
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
  /** Offered by the Column Selector — every column but #, Output Name and Add Batch. */
  hideable?: true;
}

type Cell =
  /** 1-based position of the row. Not a field; the renderer counts. */
  | { kind: 'index' }
  /** Read-only text. `undefined` renders as an em-dash. */
  | { kind: 'readonly'; value: (row: ProductRow) => string | undefined }
  /** Server-rendered HTML — a molecular formula, whose subscripts arrive as `<sub>` tags. */
  | { kind: 'html'; value: (row: ProductRow) => string | undefined }
  /** Free text, saved on blur. */
  | {
      kind: 'text';
      value: (row: ProductRow) => string | undefined;
      mutation: (row: ProductRow, next: string | null) => ModelMutation;
    }
  /**
   * A calculated or registry-owned number, shown but not editable. Its own kind rather than a
   * `numeric` with `editable: () => false`, because a read-only cell has no mutation to name and
   * inventing one that can never fire is worse than not having the field.
   */
  | {
      kind: 'readonlyNumeric';
      value: (row: ProductRow) => EnteredValue | undefined;
    }
  /** An editable number, in the unit the column's header names. */
  | {
      kind: 'numeric';
      value: (row: ProductRow) => EnteredValue | undefined;
      mutation: (row: ProductRow, next: string | null) => ModelMutation;
      editable?: (row: ProductRow) => boolean;
    }
  /** One item from a built-in dictionary. */
  | {
      kind: 'dictionary';
      dictionary: 'SALT_CODE' | 'STEREOISOMER_CODE';
      value: (row: ProductRow) => DictionaryItemRef;
      mutation: (row: ProductRow, next: DictionaryItemRef) => ModelMutation;
      editable?: (row: ProductRow) => boolean;
    }
  /** The product-type picker — a fixed enum, not a dictionary. */
  | {
      kind: 'outputType';
      value: (row: ProductRow) => ReactionOutputType;
      mutation: (row: ProductRow, next: ReactionOutputType) => ModelMutation;
    }
  /** An icon button that adds a batch to this product. */
  | { kind: 'addBatch'; mutation: (row: ProductRow) => ModelMutation };

export type ProductColumn = ColumnBase & Cell;

export const PRODUCT_COLUMNS: ProductColumn[] = [
  { id: 'index', header: '#', kind: 'index' },
  {
    id: 'outputName',
    header: 'Output Name',
    kind: 'text',
    value: (row) => row.output.outputName,
    // `@NotNull` on the record, so a cleared cell sends `''` rather than null. The backend then
    // rejects it as a duplicate of any other blank name and `apiFetch` toasts; the cell keeps
    // the draft, exactly as it does for every other rejected edit on this screen.
    mutation: (row, next) => ({ type: 'SetOutputRowName', anchor: row.output.anchor, name: next ?? '' }),
  },
  {
    id: 'reactionStep',
    header: 'Reaction Step',
    hideable: true,
    kind: 'readonly',
    // Derived from the position in `model.reactions`, not hardcoded. There is only ever one step today; this
    // keeps being right once `AddReaction` exists.
    value: (row) => String(row.step + 1),
  },
  {
    id: 'type',
    header: 'Product Type',
    hideable: true,
    kind: 'outputType',
    value: (row) => row.output.type,
    // Note `outputType`, not `type` — that name is the mutation union's own discriminator.
    mutation: (row, outputType) => ({ type: 'SetOutputRowType', anchor: row.output.anchor, outputType }),
  },
  {
    id: 'formula',
    header: 'Formula',
    hideable: true,
    kind: 'html',
    value: (row) => row.output.compound.formula,
  },
  {
    id: 'molWeight',
    header: 'Mol. Weight',
    hideable: true,
    // Read-only, unlike the inputs table's column of the same name. `SetOutputCompoundMolWeight`
    // only accepts an unknown compound, and every row here came from the drawn scheme, so it is
    // known by construction.
    kind: 'readonlyNumeric',
    value: (row) => row.output.compound.molWeight,
    unit: 'molWeight',
  },
  {
    id: 'exactMass',
    header: 'Exact Mass',
    hideable: true,
    kind: 'readonlyNumeric',
    value: (row) => row.output.compound.exactMass,
  },
  {
    id: 'theoWeight',
    header: 'Theo. Weight',
    hideable: true,
    minWidth: 100,
    kind: 'readonlyNumeric',
    // Calculated: `theoWeight = theoMol * molWeight`. No limiting reagent means no `theoMol`,
    // which means no `theoWeight` — both cells then show an em-dash rather than a zero.
    value: (row) => row.output.theoWeight,
    unit: 'weight',
  },
  {
    id: 'theoMol',
    header: 'Theo. Moles',
    hideable: true,
    minWidth: 100,
    kind: 'readonlyNumeric',
    value: (row) => row.output.theoMol,
    unit: 'mol',
  },
  {
    id: 'saltCode',
    header: 'Salt Code',
    hideable: true,
    kind: 'dictionary',
    dictionary: 'SALT_CODE',
    value: (row) => row.output.compound.saltCode,
    // Fixed once a batch has gone to registration.
    editable: (row) => saltEditable(row.output),
    mutation: (row, saltCode) => ({ type: 'SetOutputRowSaltCode', anchor: row.output.anchor, saltCode }),
  },
  {
    id: 'saltEQ',
    header: 'Salt EQ',
    hideable: true,
    kind: 'numeric',
    value: (row) => asEnteredValue(row.output.compound.saltEQ),
    // Both gates, matching the inputs table: a registered compound's salt EQ is fixed even when
    // it has a code. No salt EQ means the default "00 - Parent Structure".
    editable: (row) => saltEditable(row.output) && row.output.compound.saltEQ != null,
    mutation: (row, next) => ({ type: 'SetOutputRowSaltEQ', anchor: row.output.anchor, saltEQ: next }),
  },
  {
    id: 'stereoisomerCode',
    header: 'Stereoisomer Code',
    hideable: true,
    kind: 'dictionary',
    dictionary: 'STEREOISOMER_CODE',
    value: (row) => row.output.compound.stereoisomerCode,
    // The same gate as the salt code: the handler goes through the same `updateCompound`.
    editable: (row) => saltEditable(row.output),
    mutation: (row, stereoisomerCode) => ({
      type: 'SetOutputCompoundStereoisomerCode',
      anchor: row.output.anchor,
      stereoisomerCode,
    }),
  },
  {
    id: 'eq',
    header: 'EQ',
    hideable: true,
    kind: 'numeric',
    // The one editable number on the row, and what the two theoretical columns are computed
    // from: `theoMol = limiting.mol / limiting.eq * eq`.
    value: (row) => row.output.eq,
    mutation: (row, next) => ({ type: 'SetOutputRowEQ', anchor: row.output.anchor, eq: next }),
  },
  {
    id: 'addBatch',
    header: '',
    kind: 'addBatch',
    mutation: (row) => ({
      type: 'AddProductSample',
      anchor: row.output.anchor,
      createdSampleAnchor: newAnchor(),
    }),
  },
];

/** Every field the search box looks at. */
export function productHaystack(row: ProductRow): string {
  const compound = row.output.compound;
  return [
    row.output.outputName,
    row.output.chemicalName,
    compound.formula,
    compound.compoundKey,
    compound.saltCode.name,
  ]
    .filter((each) => each != null)
    .join(' ')
    .toLowerCase();
}

/** What the Column Selector offers, in table order — the columns marked `hideable`. */
export const HIDEABLE_PRODUCT_COLUMNS: readonly HideableColumn[] = PRODUCT_COLUMNS.filter(
  (column) => column.hideable,
).map(({ id, header }) => ({ id, header }));
