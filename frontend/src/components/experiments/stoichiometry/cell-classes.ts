import type { EnteredValue } from '@/lib/types/reactions.ts';

/**
 * How a value a person typed is written: the cell's own claim that this number came from the
 * user rather than from the registry or a formula. Exported because a value still on its way to
 * the server looks the same — see `NumericCell`, which shows what it sent while it saves.
 */
export const USER_ENTERED_CLASSES = ['text-blue-400', 'font-semibold'];

/**
 * How an invalid value is written: every quantity in the table must be positive, so zero or less
 * is an error whoever produced it. Last in the list, so `cn` lets it win over a provenance colour.
 */
const NON_POSITIVE_CLASSES = ['text-red-200'];

/**
 * What a numeric cell looks like, given where its value came from and what the last patch did
 * to it.
 *
 * Three independent things are encoded, which is why the result is a list rather than one class:
 *
 * - **Provenance**, a steady state. `EnteredValue.source` is `'fixed' | 'default' |
 *   'calculated'`, or a **number** — the revision a user entered the value in. So "was this
 *   typed by a person" is `typeof source === 'number'`, not a named enum member.
 * - **What just happened to it**, a one-shot flash. Green when the backend recalculated the
 *   value, red when it overwrote one the user had entered.
 * - **Validity**: a value of zero or less is written in red.
 *
 * Both flashes are limited to nodes the last patch rebuilt — the keys of `updatedNodes`. That
 * keeps the table still on first load, when nothing has been patched yet, and leaves alone every
 * cell a patch did not touch: `JSON_PATCHER.apply` clones the whole detail, so an untouched node
 * is a fresh object too, merely absent from the map.
 */
export function determineCellClasses(
  value: EnteredValue<unknown> | undefined,
  updatedNodes: ReadonlyMap<unknown, unknown>,
): string[] {
  if (value == null) return [];

  const nonPositive = value.value != null && Number(value.value) <= 0;

  // A user-entered value never animates: the user knows what they just typed, and the whole
  // point of the flash is to point out a change they did *not* make.
  if (typeof value.source === 'number') {
    return nonPositive ? [...USER_ENTERED_CLASSES, ...NON_POSITIVE_CLASSES] : USER_ENTERED_CLASSES;
  }

  const rebuilt = updatedNodes.has(value);
  // Null for a node the patch created — a value appearing where there was none.
  const previous = updatedNodes.get(value) as EnteredValue<unknown> | null | undefined;
  const classes: string[] = [];

  if (value.source === 'default') {
    classes.push('text-neutral-700', 'italic');
  }

  if (rebuilt && value.overwritten) {
    classes.push('animate-[flash-red_500ms_ease-in-out]');
  } else if (
    rebuilt &&
    value.source === 'calculated' &&
    (value.source !== previous?.source || value.value !== previous?.value)
  ) {
    classes.push('animate-[flash-green_500ms_ease-in-out]');
  }

  if (nonPositive) {
    classes.push(...NON_POSITIVE_CLASSES);
  }

  return classes;
}
