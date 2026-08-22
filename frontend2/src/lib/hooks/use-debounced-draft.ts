import { useEffect, useRef, useState } from 'react';

/**
 * Local draft of a value that lives elsewhere (URL search params, a parent store),
 * committed upstream only after the user stops changing it.
 *
 * `value` stays authoritative: if it changes from the outside — a link with filters,
 * a reset button — the draft resyncs and any pending commit is dropped, so a stale
 * keystroke can never overwrite the new value.
 */
export function useDebouncedDraft<T>(
  value: T,
  onCommit: (next: T) => void,
  delayMs: number,
): [T, (next: T) => void] {
  const [draft, setDraft] = useState(value);
  // The last value the two sides agreed on: what we committed, or what arrived from outside.
  const settled = useRef(value);

  const onCommitRef = useRef(onCommit);
  useEffect(() => {
    onCommitRef.current = onCommit;
  });

  useEffect(() => {
    if (value === settled.current) return;
    settled.current = value;
    setDraft(value);
  }, [value]);

  useEffect(() => {
    if (draft === settled.current) return;
    const timer = setTimeout(() => {
      settled.current = draft;
      onCommitRef.current(draft);
    }, delayMs);
    return () => clearTimeout(timer);
  }, [draft, delayMs]);

  return [draft, setDraft];
}
