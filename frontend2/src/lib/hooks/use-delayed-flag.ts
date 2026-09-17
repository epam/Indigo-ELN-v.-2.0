import { useEffect, useState } from 'react';

/** A save that beats this never shows a spinner at all. */
export const PENDING_SHOW_DELAY_MS = 300;

/**
 * `active`, but debounced on the way up — what a spinner should be driven by rather than a raw
 * `isPending`.
 *
 * Blur-to-save fires constantly (every field left, every table cell), and most saves are quick.
 * Wired straight to `isPending`, the indicator would flash for 80 ms on nearly all of them, which
 * reads as a glitch rather than as progress. So nothing is shown until `showDelayMs` has passed.
 *
 * **Down is immediate, deliberately.** This used to hold the flag for a minimum once shown, so a
 * spinner appearing at 300 ms would not blink out at 310 ms. The cost was paid in the wrong
 * currency: `SavingOverlay` drives `inert` from this flag, so an anti-flicker measure also froze
 * the control — a 350 ms save left the field unusable for ~750 ms, nearly doubling the wait, and
 * the worst case sat just over the threshold. A brief spinner is a cosmetic complaint; a field
 * that will not accept typing after the server has already answered is a real one.
 *
 * One timer covers both directions: turning on waits out the delay, turning off is scheduled at
 * zero. The hide goes through a timer rather than being set straight from the effect because a
 * synchronous `setState` there is what triggers cascading renders — the same reason the previous
 * version clamped its remaining time to zero rather than branching.
 */
export function useDelayedFlag(active: boolean, { showDelayMs = PENDING_SHOW_DELAY_MS } = {}): boolean {
  const [shown, setShown] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => setShown(active), active ? showDelayMs : 0);
    return () => clearTimeout(timer);
  }, [active, showDelayMs]);

  return shown;
}
