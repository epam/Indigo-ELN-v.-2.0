import '@testing-library/jest-dom/vitest';

// jsdom leaves `window.localStorage` undefined here, so anything that persists — the
// query cache in particular — has nowhere to write. A plain in-memory Storage keeps
// those paths testable; it is per worker, so tests that use it should clear it.
if (typeof window !== 'undefined' && !window.localStorage) {
  const entries = new Map<string, string>();
  const storage: Storage = {
    get length() {
      return entries.size;
    },
    key: (index) => [...entries.keys()][index] ?? null,
    getItem: (key) => entries.get(key) ?? null,
    setItem: (key, value) => void entries.set(key, String(value)),
    removeItem: (key) => void entries.delete(key),
    clear: () => entries.clear(),
  };
  Object.defineProperty(window, 'localStorage', { value: storage, configurable: true });
}
