import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { PENDING_MIN_VISIBLE_MS, PENDING_SHOW_DELAY_MS, useDelayedFlag } from './use-delayed-flag';

// Date.now() drives the hold, so the fake clock has to move the wall clock too.
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

  /** The common case: most saves beat the delay, and should cost no visible chrome at all. */
  it('never shows for something that finishes before the delay', () => {
    const view = setup();
    view.rerender({ active: true });

    advance(PENDING_SHOW_DELAY_MS - 50);
    view.rerender({ active: false });

    advance(PENDING_SHOW_DELAY_MS + PENDING_MIN_VISIBLE_MS);
    expect(view.result.current).toBe(false);
  });

  it('shows once the delay elapses', () => {
    const view = setup();
    view.rerender({ active: true });

    advance(PENDING_SHOW_DELAY_MS - 1);
    expect(view.result.current).toBe(false);

    advance(1);
    expect(view.result.current).toBe(true);
  });

  /** Without the hold this would blink: shown at 300 ms, hidden at 310 ms. */
  it('holds for the minimum when the wait ends just after it appeared', () => {
    const view = setup();
    view.rerender({ active: true });
    advance(PENDING_SHOW_DELAY_MS);
    expect(view.result.current).toBe(true);

    advance(10);
    view.rerender({ active: false });
    expect(view.result.current).toBe(true);

    advance(PENDING_MIN_VISIBLE_MS - 10 - 1);
    expect(view.result.current).toBe(true);

    advance(1);
    expect(view.result.current).toBe(false);
  });

  /**
   * The hold is measured from when the spinner appeared, not from when the wait ended — a
   * long save must not leave it up for another `minVisibleMs` after finishing.
   */
  it('hides at once when it has already been shown longer than the minimum', () => {
    const view = setup();
    view.rerender({ active: true });
    advance(PENDING_SHOW_DELAY_MS + PENDING_MIN_VISIBLE_MS + 1_000);
    expect(view.result.current).toBe(true);

    view.rerender({ active: false });
    advance(0);
    expect(view.result.current).toBe(false);
  });

  /** A second save starting while the first is still shown keeps it up rather than restarting. */
  it('stays shown across a back-to-back second wait', () => {
    const view = setup();
    view.rerender({ active: true });
    advance(PENDING_SHOW_DELAY_MS);
    expect(view.result.current).toBe(true);

    view.rerender({ active: false });
    view.rerender({ active: true });
    advance(PENDING_MIN_VISIBLE_MS + 100);
    expect(view.result.current).toBe(true);
  });
});
