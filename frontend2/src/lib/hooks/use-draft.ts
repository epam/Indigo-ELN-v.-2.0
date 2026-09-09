import type { Dispatch, SetStateAction } from 'react';
import { useState } from 'react';

/**
 * A local draft of a saved value, reseeded whenever the server's copy moves.
 *
 * For a control that **renders its draft unconditionally** — a text input showing what was typed
 * while the request is in flight. The reseed is what keeps that draft from outliving the value it
 * was taken from: a field the backend normalises (`1.50` stored as the `Double` 1.5, `01` as 1)
 * otherwise disagrees with the saved value for the life of the mount, so the "did this change?"
 * test on the next blur is true again and fires an identical mutation, once per focus/blur cycle.
 * Without a normalising backend the same gap is quieter but still there: a value edited in another
 * session and refetched here would never reach the screen.
 *
 * A *failed* save moves nothing, so the draft survives it — which is what every field on these
 * screens promises: the user's input is not snapped back to the server's on an error `apiFetch`
 * has already toasted.
 *
 * **Not for a control that only shows its draft while focused.** `NumericCell` renders the saved
 * value and swaps the editor in under `group-focus-within`, reseeding on entry, so a stale draft
 * is never on screen *and* a refetch landing mid-edit cannot overwrite what is being typed. That
 * is the better pattern where the design affords it; this is the cheap approximation for controls
 * that cannot.
 *
 * **Never for rich text.** Tiptap rewrites stored HTML into its own canonical form as it loads, so
 * `draft` and the server's string differ from the first render and this would reseed forever,
 * fighting the caret. Those fields snapshot a baseline on focus instead — see `richTextEdit`.
 *
 * Adjusted during render, the pattern React documents for reacting to a changed prop: an effect
 * would leave a frame showing the stale draft.
 */
export function useDraft<T>(
  saved: T,
  /**
   * How to tell two saved values apart. The default is `Object.is`, which is right for the
   * strings these mostly are; anything arriving as fresh objects per response needs its own —
   * a dictionary ref by `id`, a list of experiment refs by its sorted ids — or the comparison is
   * false on every render and the reseed never stops.
   */
  identity: (value: T) => unknown = (value) => value,
): [draft: T, setDraft: Dispatch<SetStateAction<T>>, reseeded: boolean] {
  // Both seeds pass `saved` through a function, as `useSettled` does and for the same reason:
  // React reads a bare function as a lazy initialiser or an updater and calls it.
  const [draft, setDraft] = useState<T>(() => saved);
  const [seeded, setSeeded] = useState<T>(() => saved);

  const reseeded = !Object.is(identity(saved), identity(seeded));
  if (reseeded) {
    setSeeded(() => saved);
    setDraft(() => saved);
  }

  // True on the one render the draft was reseeded, for state that has to be dropped alongside it
  // — a validation message describing a draft that is no longer there. Ignore it otherwise.
  return [draft, setDraft, reseeded];
}
