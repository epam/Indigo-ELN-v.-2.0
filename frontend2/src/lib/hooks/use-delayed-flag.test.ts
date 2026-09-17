import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { PENDING_SHOW_DELAY_MS, useDelayedFlag } from './use-delayed-flag';

beforeEach(() => vi.useFakeTimers({ shouldAdvanceTime: false }));
afterEach(() => vi.useRealTimers());

function setup(initial = false) {
  return renderHook(({ active }: { active: boolean }) => useDelayedFlag(active), {
    initialProps: { active: initial },
  });
}

function advance(ms: number) {
  act(() => void vi.advanceTimersByTime(ms));
}

describe('useDelayedFlag', () => {
  it('starts false', () => {
    expect(setup().result.current).toBe(false);
    expect(setup(true).result.current).toBe(false);
  });

  it('never shows for something that finishes before the delay', () => {
    const { result, rerender } = setup(true);

    advance(PENDING_SHOW_DELAY_MS - 1);
    rerender({ active: false });
    advance(1_000);

    expect(result.current).toBe(false);
  });

  it('shows once the delay elapses', () => {
    const { result } = setup(true);

    advance(PENDING_SHOW_DELAY_MS - 1);
    expect(result.current).toBe(false);

    advance(1);
    expect(result.current).toBe(true);
  });

  /**
   * **The behaviour this hook used to get wrong.** It held the flag for a minimum once shown, so
   * a spinner that appeared at 300 ms stayed until 700 ms whatever the server did. `SavingOverlay`
   * drives `inert` from this flag, so that hold also froze the control: a 350 ms save left the
   * field unusable for ~750 ms. Down is now immediate.
   */
  it('hides as soon as the wait ends, however briefly it was shown', () => {
    const { result, rerender } = setup(true);

    advance(PENDING_SHOW_DELAY_MS);
    expect(result.current).toBe(true);

    // 10 ms after appearing — the case the old minimum existed to smooth over.
    advance(10);
    rerender({ active: false });
    advance(0);
    expect(result.current).toBe(false);
  });

  it('hides at once after a long wait too', () => {
    const { result, rerender } = setup(true);

    advance(PENDING_SHOW_DELAY_MS + 5_000);
    rerender({ active: false });
    advance(0);

    expect(result.current).toBe(false);
  });

  it('starts the delay again for a second wait rather than showing at once', () => {
    const { result, rerender } = setup(true);

    advance(PENDING_SHOW_DELAY_MS);
    rerender({ active: false });
    advance(0);
    expect(result.current).toBe(false);

    rerender({ active: true });
    expect(result.current).toBe(false);

    advance(PENDING_SHOW_DELAY_MS);
    expect(result.current).toBe(true);
  });
});
