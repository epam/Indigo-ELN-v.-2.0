import type { DictionaryItemRef } from '@/lib/types/dictionaries.ts';
import type { ExperimentRef } from '@/lib/types/experiments.ts';

/**
 * What each Experiment Details field should send, or `null` when it did not really change.
 *
 * All three share the `{ value } | null` shape `richTextEdit` established in `src/lib/rich-text.ts`
 * — `null` meaning **send nothing**, so the field is left absent from the PATCH and cannot clobber
 * a value someone else changed meanwhile. A `value` of `null` inside the wrapper is the different,
 * deliberate thing: clear the column.
 *
 * The comparison rules are ported from indigo-frontend's `experiment-details.component.ts`, which
 * is the only place they were ever written down.
 */

/**
 * A plain-text field. The trimmed draft is what gets compared *and* what gets sent, so the two
 * agree and a second blur on the same text asks for nothing; comparing the raw draft would make
 * trailing whitespace a permanent "change". Trimmed to nothing clears the column rather than
 * storing `''`, which the backend would hand back as an empty title rather than no title.
 */
export function titleEdit(draft: string, baseline: string | undefined): { value: string | null } | null {
  const trimmed = draft.trim();
  if (trimmed === (baseline ?? '')) return null;
  return { value: trimmed === '' ? null : trimmed };
}

/**
 * A dictionary single-select. Compared by `id`: the picker's items come from a separate request
 * to `/dictionaries/{name}`, so the object identifying the current value is never the same object
 * the experiment payload carried, and `===` would report a change on every render.
 */
export function dictionaryEdit(
  next: DictionaryItemRef | null,
  baseline: DictionaryItemRef | undefined,
): { value: DictionaryItemRef | null } | null {
  if (next?.id === baseline?.id) return null;
  return { value: next };
}

/** Ids in a stable order, so two lists holding the same refs compare equal however they are ordered. */
function refKey(refs: ExperimentRef[]): string {
  return refs
    .map((ref) => ref.id)
    .sort()
    .join(',');
}

/**
 * A list of experiment references. Order carries no meaning — the backend field is a `Set` — so
 * reordering is not a change, and neither is the same list arriving as fresh objects.
 */
export function experimentRefsEdit(
  next: ExperimentRef[],
  baseline: ExperimentRef[],
): { value: ExperimentRef[] } | null {
  if (refKey(next) === refKey(baseline)) return null;
  return { value: next };
}
