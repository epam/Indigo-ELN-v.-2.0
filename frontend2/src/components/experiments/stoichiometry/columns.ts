import type { EnteredValue } from '@/lib/types/reactions.ts';

/**
 * The padding, borders and typography of a cell and of a header cell. Shared by every table on
 * this screen — they are one grid to the reader, so a difference here would show up as the
 * products table sitting a pixel off the stoichiometry table above it.
 */
export const CELL_CLASS = 'border-b border-neutral-300 px-2 py-1 align-middle';
export const HEADER_CELL_CLASS =
  'border-y border-neutral-300 px-2 py-2 text-center text-[12px]/5 font-semibold whitespace-nowrap text-neutral-800';

/**
 * `saltEQ` is a bare `number` on the compound, not an `EnteredValue` like every other numeric
 * field, so it is lifted into one to reach the same cell component. Deliberately **without a
 * `source`**: it has no provenance, and claiming one would colour it as fixed or calculated.
 */
export function asEnteredValue(value: number | undefined): EnteredValue<string> | undefined {
  return value == null ? undefined : { value: String(value), unit: 'NO_UNIT' };
}
