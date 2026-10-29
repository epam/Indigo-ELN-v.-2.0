import { act, renderHook } from '@testing-library/react';
import { afterEach, describe, expect, it } from 'vitest';

import { useStoredState } from './use-stored-state';

function isNumber(raw: unknown): raw is number {
  return typeof raw === 'number';
}

afterEach(() => localStorage.clear());

describe('useStoredState', () => {
  it('starts from the fallback and remembers what is set', () => {
    const { result } = renderHook(() => useStoredState('k', 1, isNumber));
    expect(result.current[0]).toBe(1);

    act(() => result.current[1](5));

    expect(result.current[0]).toBe(5);
    expect(localStorage.getItem('k')).toBe('5');
  });

  it('reads back a stored value, and ignores one that fails validation', () => {
    localStorage.setItem('good', '7');
    localStorage.setItem('bad', '"seven"');

    expect(renderHook(() => useStoredState('good', 1, isNumber)).result.current[0]).toBe(7);
    expect(renderHook(() => useStoredState('bad', 1, isNumber)).result.current[0]).toBe(1);
  });

  /** The user loads after the first render — the table must not keep the anonymous default. */
  it('keeps state in memory without a key, and reads the stored value once the key arrives', () => {
    localStorage.setItem('user.k', '9');
    const { result, rerender } = renderHook(({ key }) => useStoredState(key, 1, isNumber), {
      initialProps: { key: undefined as string | undefined },
    });
    expect(result.current[0]).toBe(1);

    rerender({ key: 'user.k' });

    expect(result.current[0]).toBe(9);
  });
});
