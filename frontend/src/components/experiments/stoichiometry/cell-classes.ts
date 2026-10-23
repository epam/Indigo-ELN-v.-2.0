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
 * Whether the last patch moved this value: `previous` is the same cell in the model before it,
 * undefined when the cell did not exist there. With no patch the caller passes the value itself,
 * so nothing has changed.
 */
export function hasChanged(value: EnteredValue<unknown>, previous: EnteredValue<unknown> | undefined): boolean {
  return (
    value.value !== previous?.value || value.source !== previous?.source || value.overwritten !== previous?.overwritten
  );
}

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
 * Both flashes compare the value with `previous`, the same cell before the last patch — see
 * `hasChanged`. That keeps the table still on first load, when there is no patch and the caller
 * passes the value itself, and leaves alone every cell a patch did not move.
 */
export function determineCellClasses(
  value: EnteredValue<unknown> | undefined,
  previous: EnteredValue<unknown> | undefined,
): string[] {
  if (value == null) return [];

  const nonPositive = value.value != null && Number(value.value) <= 0;

  // A user-entered value never animates: the user knows what they just typed, and the whole
  // point of the flash is to point out a change they did *not* make.
  if (typeof value.source === 'number') {
    return nonPositive ? [...USER_ENTERED_CLASSES, ...NON_POSITIVE_CLASSES] : USER_ENTERED_CLASSES;
  }

  const classes: string[] = [];

  if (value.source === 'default') {
    classes.push('text-neutral-700', 'italic');
  }

  if (value.overwritten && hasChanged(value, previous)) {
    classes.push('animate-[flash-red_500ms_ease-in-out]');
  } else if (value.source === 'calculated' && (value.source !== previous?.source || value.value !== previous?.value)) {
    classes.push('animate-[flash-green_500ms_ease-in-out]');
  }

  if (nonPositive) {
    classes.push(...NON_POSITIVE_CLASSES);
  }

  return classes;
}
