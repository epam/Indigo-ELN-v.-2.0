import { useEffect, useState } from 'react';

/**
 * Whether `value` has held still for `delayMs`. The value a component starts with counts
 * as settled, so gating a query on this never delays its first load.
 *
 * Written to gate a query's `enabled` while its key tracks the live value: the key changes
 * on the first keystroke, so `isPending` covers the wait *and* the request that follows it
 * as one "we don't know yet" window. Lagging the value itself instead — the older approach
 * here — leaves the UI showing the previous term's results with nothing to say so.
 *
 * Derived during render rather than in the effect, so there is no frame where a changed
 * value still looks settled — that frame would fire a request per keystroke.
 *
 * Both state calls pass `value` through a function on purpose. React reads a bare function
 * as a lazy initialiser or an updater and *calls* it, so for a function-typed `T` the hook
 * would store the return value instead of the value itself — `settled === value` could then
 * never be true, and a query gated on it would stay disabled forever. The wrappers make the
 * generic honest; every call site today passes a string.
 */
export function useSettled<T>(value: T, delayMs: number): boolean {
  const [settled, setSettled] = useState<T>(() => value);

  useEffect(() => {
    const timer = setTimeout(() => setSettled(() => value), delayMs);
    return () => clearTimeout(timer);
  }, [value, delayMs]);

  return settled === value;
}
