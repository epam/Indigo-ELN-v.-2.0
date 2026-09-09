import type { EnteredValue } from '@/lib/types/reactions.ts';

/**
 * What a numeric cell looks like, given where its value came from and what the last patch did
 * to it. Port of indigo-frontend's `ExperimentDetailService.determineCellClasses`.
 *
 * Two independent things are encoded, which is why the result is a list rather than one class:
 *
 * - **Provenance**, a steady state. `EnteredValue.source` is `'fixed' | 'default' |
 *   'calculated'`, or a **number** — the revision a user entered the value in. So "was this
 *   typed by a person" is `typeof source === 'number'`, not a named enum member. (There *is* an
 *   `EnteredValueSource` enum in the old frontend with names like `USER_ENTERED`; it is dead
 *   code and is not what the wire carries.)
 * - **What just happened to it**, a one-shot flash. Green when the backend recalculated the
 *   value, red when it overwrote one the user had entered.
 *
 * Both flashes are suppressed while `updatedNodes` is empty, which is what keeps the whole
 * table from flashing on first load — nothing has been patched yet.
 */
export function determineCellClasses(
  value: EnteredValue<unknown> | undefined,
  updatedNodes: ReadonlyMap<unknown, unknown>,
): string[] {
  if (value == null) return [];

  // A user-entered value never animates: the user knows what they just typed, and the whole
  // point of the flash is to point out a change they did *not* make. Returns early, exactly
  // as the original does.
  if (typeof value.source === 'number') return ['text-blue-400', 'font-semibold'];

  const previous = updatedNodes.get(value) as EnteredValue<unknown> | undefined;
  const hasAnyUpdates = updatedNodes.size !== 0;
  const classes: string[] = [];

  if (value.source === 'default') {
    classes.push('text-neutral-700', 'italic');
  } else if (value.source === 'fixed') {
    classes.push('text-violet-400');
  }

  if (hasAnyUpdates && value.overwritten) {
    classes.push('animate-[flash-red_500ms_ease-in-out]');
  } else if (
    hasAnyUpdates &&
    value.source === 'calculated' &&
    (value.source !== previous?.source || value.value !== previous?.value)
  ) {
    classes.push('animate-[flash-green_500ms_ease-in-out]');
  }

  return classes;
}
