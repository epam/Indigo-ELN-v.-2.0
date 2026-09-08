import { asEnteredValue } from '@/components/experiments/stoichiometry/columns';

import type { NumericCellValue } from '@/components/experiments/stoichiometry/numeric-cell';
import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { ModelMutation } from '@/lib/types/mutations.ts';
import type { EnteredValue, ReactionOutput, ReactionOutputType } from '@/lib/types/reactions.ts';
import { MOL_UNITS, MOL_WEIGHT_UNITS, NO_UNITS, WEIGHT_UNITS } from '@/lib/types/reactions.ts';

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
export interface ProductRow {
  output: ReactionOutput;
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
  | { kind: 'readonlyNumeric'; value: (row: ProductRow) => EnteredValue<string> | undefined; units: readonly string[] }
  /** An editable number with a unit, or a unitless one when `units` has a single member. */
  | {
      kind: 'numeric';
      value: (row: ProductRow) => EnteredValue<string> | undefined;
      units: readonly string[];
      mutation: (row: ProductRow, next: NumericCellValue) => ModelMutation;
      editable?: (row: ProductRow) => boolean;
    }
  /** One item from a built-in dictionary. */
  | {
      kind: 'dictionary';
      dictionary: 'SALT_CODE';
      value: (row: ProductRow) => DictionaryItemRef | undefined;
      mutation: (row: ProductRow, next: DictionaryItemRef | null) => ModelMutation;
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
  { id: 'index', header: '#', minWidth: 48, kind: 'index' },
  {
    id: 'outputName',
    header: 'Output Name',
    minWidth: 140,
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
    minWidth: 110,
    kind: 'readonly',
    // Derived from the position in `model.reactions`, not hardcoded — indigo-frontend's batch
    // summary returns a literal '1' with a TODO. There is only ever one step today, so the two
    // agree; this one keeps agreeing once `AddReaction` exists.
    value: (row) => String(row.step + 1),
  },
  {
    id: 'type',
    header: 'Products Type',
    minWidth: 150,
    kind: 'outputType',
    value: (row) => row.output.type,
    // Note `outputType`, not `type` — that name is the mutation union's own discriminator.
    mutation: (row, outputType) => ({ type: 'SetOutputRowType', anchor: row.output.anchor, outputType }),
  },
  {
    id: 'formula',
    header: 'Formula',
    minWidth: 130,
    kind: 'html',
    value: (row) => row.output.compound.formula,
  },
  {
    id: 'molWeight',
    header: 'Mol. Weight',
    minWidth: 110,
    // Read-only, unlike the inputs table's column of the same name. `SetOutputCompoundMolWeight`
    // only accepts an `UNKNOWN` compound, and every row here came from the drawn scheme, so it
    // is stored or virtual by construction.
    kind: 'readonlyNumeric',
    value: (row) => row.output.compound.molWeight,
    units: MOL_WEIGHT_UNITS,
  },
  {
    id: 'exactMass',
    header: 'Exact Mass',
    minWidth: 110,
    kind: 'readonlyNumeric',
    value: (row) => (row.output.compound.type === 'UNKNOWN' ? undefined : row.output.compound.exactMass),
    units: NO_UNITS,
  },
  {
    id: 'theoWeight',
    header: 'Theo. Weight',
    minWidth: 130,
    kind: 'readonlyNumeric',
    // Calculated: `theoWeight = theoMol * molWeight`. No limiting reagent means no `theoMol`,
    // which means no `theoWeight` — both cells then show an em-dash rather than a zero.
    value: (row) => row.output.theoWeight,
    units: WEIGHT_UNITS,
  },
  {
    id: 'theoMol',
    header: 'Theo. Moles',
    minWidth: 130,
    kind: 'readonlyNumeric',
    value: (row) => row.output.theoMol,
    units: MOL_UNITS,
  },
  {
    id: 'saltCode',
    header: 'Salt Code',
    minWidth: 150,
    kind: 'dictionary',
    dictionary: 'SALT_CODE',
    value: (row) => (row.output.compound.type === 'UNKNOWN' ? undefined : row.output.compound.saltCode),
    // A stored compound's salt code is registry data.
    editable: (row) => row.output.compound.type === 'VIRTUAL',
    mutation: (row, saltCode) => ({ type: 'SetOutputRowSaltCode', anchor: row.output.anchor, saltCode }),
  },
  {
    id: 'saltEQ',
    header: 'Salt EQ',
    minWidth: 100,
    kind: 'numeric',
    value: (row) => (row.output.compound.type === 'UNKNOWN' ? undefined : asEnteredValue(row.output.compound.saltEQ)),
    units: NO_UNITS,
    // Both gates, matching the inputs table: a stored compound's salt EQ is fixed by the registry
    // even when it has a code. (indigo-frontend's products table checked only for the code.)
    editable: (row) => row.output.compound.type === 'VIRTUAL' && row.output.compound.saltCode != null,
    mutation: (row, next) => ({ type: 'SetOutputRowSaltEQ', anchor: row.output.anchor, saltEQ: next.value }),
  },
  {
    id: 'eq',
    header: 'EQ',
    minWidth: 90,
    kind: 'numeric',
    // The one editable number on the row, and what the two theoretical columns are computed
    // from: `theoMol = limiting.mol / limiting.eq * eq`.
    value: (row) => row.output.eq,
    units: NO_UNITS,
    mutation: (row, next) => ({ type: 'SetOutputRowEQ', anchor: row.output.anchor, eq: next.value }),
  },
  {
    id: 'addBatch',
    header: '',
    minWidth: 48,
    kind: 'addBatch',
    mutation: (row) => ({ type: 'AddProductSample', anchor: row.output.anchor }),
  },
];

/** Every field the search box looks at. */
export function productHaystack(row: ProductRow): string {
  const compound = row.output.compound;
  return [
    row.output.outputName,
    row.output.chemicalName,
    compound.formula,
    compound.type === 'UNKNOWN' ? undefined : compound.compoundKey,
    compound.type === 'UNKNOWN' ? undefined : compound.saltCode?.name,
  ]
    .filter((each) => each != null)
    .join(' ')
    .toLowerCase();
}
