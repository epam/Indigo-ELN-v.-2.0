import { useCallback, useState } from 'react';

function read<T>(key: string | undefined, isValid: (raw: unknown) => raw is T): T | undefined {
  if (key == null) return undefined;
  try {
    const raw = localStorage.getItem(key);
    if (raw == null) return undefined;
    const parsed: unknown = JSON.parse(raw);
    return isValid(parsed) ? parsed : undefined;
  } catch {
    // Unreadable or blocked storage is the same as nothing stored.
    return undefined;
  }
}

/**
 * `useState`, remembered in localStorage under `key` — for a per-browser preference such as which
 * columns a table shows, never for anything that has to reach the server or another device.
 *
 * `key` may be `undefined` while whatever names it is still loading (the signed-in user, say):
 * the state then lives in memory only, and is read from storage once the key arrives. A changed
 * key reads its own value during render rather than in an effect, so there is no frame showing
 * the previous key's value. What is read back must pass `isValid`, since anything — an older
 * build, another tab, a hand edit — may have written it.
 */
export function useStoredState<T>(key: string | undefined, fallback: T, isValid: (raw: unknown) => raw is T) {
  const [entry, setEntry] = useState(() => ({ key, value: read(key, isValid) ?? fallback }));

  let current = entry;
  if (entry.key !== key) {
    current = { key, value: read(key, isValid) ?? fallback };
    setEntry(current);
  }

  const set = useCallback(
    (value: T) => {
      setEntry({ key, value });
      if (key == null) return;
      try {
        localStorage.setItem(key, JSON.stringify(value));
      } catch {
        // Full or blocked storage: the choice still holds for this page, it just is not kept.
      }
    },
    [key],
  );

  return [current.value, set] as const;
}
