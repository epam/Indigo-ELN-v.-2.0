import { useEffect, useState } from 'react';

/** A save that beats this never shows a spinner at all. */
export const PENDING_SHOW_DELAY_MS = 300;
/** Once shown, the spinner holds this long, so a save landing just after the delay does not blink. */
export const PENDING_MIN_VISIBLE_MS = 400;

/**
 * `active`, but debounced on the way up and held on the way down — what a spinner should be
 * driven by rather than a raw `isPending`.
 *
 * Blur-to-save fires constantly (every field left, every table cell), and most saves are quick.
 * Wired straight to `isPending`, the indicator would flash for 80 ms on nearly all of them, which
 * reads as a glitch rather than as progress. Two timers fix both halves of that:
 *
 * - nothing is shown until `showDelayMs` has passed, so a fast save is silent;
 * - once shown it stays for `minVisibleMs`, so a save landing at 310 ms does not blink out.
 *
 * The clock starts when `active` goes true. `shownAt` is the timestamp rather than a boolean
 * because the hold has to be measured from when the spinner appeared, not from when the save
 * ended — a `setTimeout(minVisible)` started on the way down would hold a long-shown spinner for
 * another 400 ms after its save had finished.
 */
export function useDelayedFlag(
  active: boolean,
  { showDelayMs = PENDING_SHOW_DELAY_MS, minVisibleMs = PENDING_MIN_VISIBLE_MS } = {},
): boolean {
  const [shownAt, setShownAt] = useState<number | null>(null);

  useEffect(() => {
    if (active) {
      if (shownAt !== null) return;
      const timer = setTimeout(() => setShownAt(Date.now()), showDelayMs);
      return () => clearTimeout(timer);
    }

    if (shownAt === null) return;
    // Clamped rather than branched: a spinner already up longer than the minimum hides on the
    // next tick, which keeps this a single scheduled update instead of a setState in an effect.
    const remaining = Math.max(shownAt + minVisibleMs - Date.now(), 0);
    const timer = setTimeout(() => setShownAt(null), remaining);
    return () => clearTimeout(timer);
  }, [active, shownAt, showDelayMs, minVisibleMs]);

  return shownAt !== null;
}
