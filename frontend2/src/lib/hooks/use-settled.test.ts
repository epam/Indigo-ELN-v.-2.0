import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { useSettled } from './use-settled';

const DELAY = 300;

beforeEach(() => vi.useFakeTimers());
afterEach(() => vi.useRealTimers());

function setup(initial = '') {
  return renderHook(({ value }: { value: string }) => useSettled(value, DELAY), {
    initialProps: { value: initial },
  });
}

describe('useSettled', () => {
  it('treats the value it starts with as settled, so a first load is never delayed', () => {
    expect(setup('kin').result.current).toBe(true);
  });

  it('settles a changed value once the delay elapses', () => {
    const view = setup();
    view.rerender({ value: 'k' });
    expect(view.result.current).toBe(false);

    act(() => void vi.advanceTimersByTime(DELAY));
    expect(view.result.current).toBe(true);
  });

  it('reports unsettled on the very render the value changes', () => {
    const view = setup();

    // No frame where the new value still looks settled — that frame would fire a request
    // on every keystroke, defeating the debounce.
    view.rerender({ value: 'k' });
    expect(view.result.current).toBe(false);
  });

  it('restarts the wait each time the value changes', () => {
    const view = setup();

    view.rerender({ value: 'k' });
    act(() => void vi.advanceTimersByTime(DELAY - 50));
    view.rerender({ value: 'ki' });
    act(() => void vi.advanceTimersByTime(DELAY - 50));
    expect(view.result.current).toBe(false);

    act(() => void vi.advanceTimersByTime(50));
    expect(view.result.current).toBe(true);
  });

  it('treats a function-typed value as a value, not as an initialiser or an updater', () => {
    // React calls a bare function passed to useState/setState. Without the wrappers the
    // hook would store what `first`/`second` *return*, so `settled === value` could never
    // hold and anything gated on it — a query's `enabled` — would stay off for good.
    const first = () => 'first';
    const second = () => 'second';

    const view = renderHook(({ value }: { value: () => string }) => useSettled(value, DELAY), {
      initialProps: { value: first },
    });
    expect(view.result.current).toBe(true);

    view.rerender({ value: second });
    expect(view.result.current).toBe(false);

    act(() => void vi.advanceTimersByTime(DELAY));
    expect(view.result.current).toBe(true);
  });

  it('settles immediately when the value returns to one already settled', () => {
    const view = setup();
    view.rerender({ value: 'kin' });
    act(() => void vi.advanceTimersByTime(DELAY));
    expect(view.result.current).toBe(true);

    // Typing on and then deleting back inside the window: the term is already known good,
    // so there is nothing to wait for.
    view.rerender({ value: 'kina' });
    expect(view.result.current).toBe(false);
    view.rerender({ value: 'kin' });
    expect(view.result.current).toBe(true);
  });
});
