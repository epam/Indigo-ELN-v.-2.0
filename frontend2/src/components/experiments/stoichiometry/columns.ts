import { cn } from '@/lib/utils';

import type { EnteredValue } from '@/lib/types/reactions.ts';

/**
 * The padding, borders and typography of a cell and of a header cell. Shared by every table on
 * this screen — they are one grid to the reader, so a difference here would show up as the
 * products table sitting a pixel off the stoichiometry table above it.
 *
 * Neither carries a `text-align`: alignment is a property of the **column**, so that a header
 * and the cells under it cannot disagree. See `alignOf`.
 */
export const CELL_CLASS = 'border-b border-neutral-300 px-2 py-1 align-middle';

/**
 * `px-[17px]`, not `px-2`, and the number is not arbitrary: it is the `<td>`'s own `px-2` plus
 * the 9px a cell's content box insets its text by (`CONTENT_BOX` — a 1px transparent border and
 * `px-2`). A header has no such box, so it has to carry that inset as padding instead.
 *
 * Without it every boxed column's text sat 9px inside its own header while the read-only columns
 * beside it sat flush, and the resulting stagger is most of what made a value look like it
 * belonged to the column next door.
 */
export const HEADER_CELL_CLASS =
  'border-y border-neutral-300 px-[17px] py-2 text-[12px]/5 font-semibold whitespace-nowrap text-neutral-800';

/**
 * The box a cell's content sits in, worn by **every** cell so that all of them inset their text
 * by the same 9px.
 *
 * The transparent border is load-bearing on the editable cells — it is what `EDITABLE_CELL_CLASS`
 * turns grey on hover and blue on focus without the cell resizing — and the read-only cells wear
 * it for no reason except that geometry. A column of read-only text beside a column of editable
 * text has to line up with it, and that is only true if both reserve the same border.
 */
export const CONTENT_BOX = 'border border-transparent px-2';

export type Align = 'left' | 'right' | 'center';

export const ALIGN_CLASS: Record<Align, string> = {
  left: 'text-left',
  right: 'text-right',
  center: 'text-center',
};

/** Numbers, and the row actions that sit at the right edge of the table. */
const RIGHT: ReadonlySet<string> = new Set(['numeric', 'readonlyNumeric', 'actions', 'delete', 'addBatch']);

/**
 * Content that reads as one object of roughly fixed width rather than as a line of text: the
 * pill-shaped selects, the static type badge, and the Limiting radio.
 *
 * `multiDictionary` is deliberately absent. It is the one member of the select family that is a
 * text-entry field — it filters against what is typed and holds a variable number of chips — so
 * it keeps the cell's full width, and a centred header over a full-width input would reproduce
 * exactly the mismatch this rule exists to remove.
 */
const CENTER: ReadonlySet<string> = new Set(['limiting', 'role', 'dictionary', 'outputType', 'outputTypeBadge']);

/**
 * Which way a column reads, from its cell kind.
 *
 * Keyed by the `kind` string rather than by a union, because the three tables deliberately keep
 * three separate `Cell` unions — the vocabulary of kind names is what they share. Anything not
 * named above is left-aligned, which is right for text, formulas, chip inputs and the row
 * ordinal: right-aligning a one- or two-digit ordinal only opens a gap next to the chevron.
 *
 * Derived rather than declared per column so that the rule cannot drift out of sync with what a
 * cell actually renders — a new kind picks up an alignment by being named here, in one place,
 * rather than by ~40 column literals agreeing with each other.
 */
export function alignOf(kind: string): Align {
  if (RIGHT.has(kind)) return 'right';
  if (CENTER.has(kind)) return 'center';
  return 'left';
}

/**
 * The trailing column of row actions, on every table that has one.
 *
 * Three properties, none of which the ordinary `CELL_CLASS` can give it:
 *
 * - **It shrinks to its buttons.** `w-px` in an auto-layout table is a floor of nothing, so the
 *   column takes exactly the width of its content instead of the 48px-per-icon it used to claim.
 * - **`pl-4` is the offset** that separates the group from the last data column, and the left
 *   border marks where the pinned edge begins.
 * - **It is pinned to the right.** These tables scroll horizontally and a delete button that has
 *   scrolled off is a delete button that is not there. A sticky cell paints over the row beneath
 *   it, so it needs its own background — and therefore its own hover, off the row's `group/row`,
 *   since it cannot inherit the one on the `<tr>`.
 *
 * Pinning is why the tables are `border-separate`: under `border-collapse` the borders of a
 * sticky cell are owned by the table and are left behind when it moves.
 */
export const ACTIONS_CELL_CLASS =
  'sticky right-0 z-10 w-px border-l border-neutral-300 bg-card pr-2 pl-4 whitespace-nowrap group-hover/row:bg-neutral-100';

/**
 * `saltEQ` is a bare `number` on the compound, not an `EnteredValue` like every other numeric
 * field, so it is lifted into one to reach the same cell component. Deliberately **without a
 * `source`**: it has no provenance, and claiming one would colour it as fixed or calculated.
 */
export function asEnteredValue(value: number | undefined): EnteredValue<string> | undefined {
  return value == null ? undefined : { value: String(value), unit: 'NO_UNIT' };
}

/**
 * The box an editable text cell draws: transparent until hovered, blue while focused, and no
 * taller than a read-only cell so a column of them does not step.
 *
 * Here rather than in `cells.tsx` because it is shared by something that is not a cell: the
 * dictionary admin table's draft row wears the same box around a different behaviour — it has no
 * saved value to fall back to, it autofocuses, and Escape discards the whole row.
 */
export const EDITABLE_CELL_CLASS = cn(
  CONTENT_BOX,
  'w-full rounded-2 py-1 text-[13px]/5 text-neutral-1000 outline-none',
  'placeholder:text-neutral-700 hover:border-neutral-300',
  'focus:border-blue-400 focus:placeholder:text-transparent',
);
